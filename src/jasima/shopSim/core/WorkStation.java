/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.core;

import jasima.core.simulation.Event;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierAdapter;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.IndividualMachine.MachineState;
import jasima.shopSim.core.WorkStation.WorkStationEvent;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to represent a workstation. A workstation is a collection of identical
 * {@link IndividualMachine}s sharing a common queue.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class WorkStation implements Notifier<WorkStation, WorkStationEvent> {

	/** Base class for workstation events. */
	public static class WorkStationEvent {
	}

	// constants for default events thrown by a workstation

	public static final WorkStationEvent WS_ACTIVATED = new WorkStationEvent();
	public static final WorkStationEvent WS_DEACTIVATED = new WorkStationEvent();
	public static final WorkStationEvent WS_JOB_ARRIVAL = new WorkStationEvent();
	public static final WorkStationEvent WS_JOB_SELECTED = new WorkStationEvent();
	public static final WorkStationEvent WS_JOB_COMPLETED = new WorkStationEvent();
	public static final WorkStationEvent WS_DONE = new WorkStationEvent();
	public static final WorkStationEvent WS_COLLECT_RESULTS = new WorkStationEvent();
	public static final WorkStationEvent WS_INIT = new WorkStationEvent();

	public static final String DEF_SETUP_STR = "DEF_SETUP";
	public static final int DEF_SETUP = 0;

	/**
	 * Constant to denote the batch family of a job, which is not compatible to
	 * any other.
	 */
	public static final String BATCH_INCOMPATIBLE = "BATCH_INCOMPATIBLE";

	// constants to deterministically sequence concurrent events

	// lookahead-arrivals after selections
	public static final int LOOKAHEAD_PRIO = Event.EVENT_PRIO_LOW;
	public static final int SELECT_PRIO = Event.EVENT_PRIO_NORMAL;
	public static final int DEPART_PRIO = Event.EVENT_PRIO_HIGHER;
	public static final int TAKE_DOWN_PRIO = DEPART_PRIO + 1000; // after depart
	public static final int ACTIVATE_PRIO = DEPART_PRIO - 1000; // before depart

	// parameters

	private String name;
	private final int numInGroup;
	private final IndividualMachine[] machDat;
	private double[][] setupMatrix = { { 0.0 } };

	private BatchForming batchForming;
	private PR batchSequencingRule;

	public final PriorityQueue<Job> queue;

	private HashMap<Object, Object> valueStore;

	JobShop shop;
	int index; // in shop.machines

	private int numBusy;
	private int numFutures; // number of future arrivals currently in queue

	private boolean batchingUsed;

	// which machine in this group currently selects its next batch? This
	// information is important if, e.g., dispatching rules have to determine a
	// specific machine's setup state
	public IndividualMachine currMachine;

	private ArrayDeque<IndividualMachine> freeMachines;
	// reuse select event objects
	private SelectEvent selectEventCache;

	private final class SelectEvent extends Event {
		private SelectEvent(double time) {
			super(time, SELECT_PRIO);
		}

		@Override
		public void handle() {
			// are there jobs that could be started and is there at
			// least one free machine
			if (numBusy < numInGroup && numJobsWaiting() > 0) {
				// at least 1 machine idle, start job selection
				selectAndStart0();
			}

			// reuseEvent(this);
			this.next = selectEventCache;
			selectEventCache = this;
		}

		protected SelectEvent next = null; // linked list, see selectEventCache
	}

	protected double workContentReal, workContentFuture;
	private ArrayList<String> setupStateTranslate;
	private Map<String, List<Job>> jobsPerBatchFamily;

	// the following fields temporarily contain parameters used by listeners
	public Job justArrived;
	public PrioRuleTarget justStarted;
	public PrioRuleTarget justCompleted;
	public int oldSetupState;
	public int newSetupState;
	public double setupTime;
	public Map<String, Object> resultMap;

	public WorkStation() {
		this(1);
	}

	public WorkStation(int numInGroup) {
		super();
		this.valueStore = null;
		this.numInGroup = numInGroup;

		numBusy = 0;
		queue = new PriorityQueue<Job>(this);

		PR sr = new FCFS();
		sr.setTieBreaker(new TieBreakerFASFS());
		sr.setOwner(this);
		queue.setSequencingRule(new IgnoreFutureJobs(sr));

		batchForming = new HighestJobBatchingMBS();
		batchForming.setOwner(this);

		machDat = new IndividualMachine[numInGroup];
		for (int i = 0; i < machDat.length; i++) {
			machDat[i] = new IndividualMachine(this, i);
		}
	}

	public void init() {
		assert translateSetupState(DEF_SETUP_STR) == DEF_SETUP;

		batchingUsed = false;
		jobsPerBatchFamily = null;

		workContentFuture = workContentReal = 0.0d;

		freeMachines = new ArrayDeque<IndividualMachine>(numInGroup);
		numBusy = 0;

		queue.clear();

		for (int i = machDat.length - 1; i >= 0; i--) {
			IndividualMachine imd = machDat[i];
			imd.setupState = imd.initialSetup;

			imd.procFinished = imd.relDate;
			imd.procStarted = 0.0;
			imd.state = MachineState.DOWN;
			numBusy++;

			imd.activateEvent.setTime(imd.relDate);
			shop.schedule(imd.activateEvent);
		}

		selectEventCache = null;

		currMachine = null;

		queue.getSequencingRule().init();

		if (getBatchSequencingRule() != null)
			getBatchSequencingRule().init();

		if (numListener() > 0) {
			fire(WS_INIT);
		}
	}

	/** Activation from DOWN state. */
	protected void activate() {
		assert currMachine.state == MachineState.DOWN;
		assert currMachine.curJob == null;

		currMachine.state = MachineState.IDLE;
		currMachine.procFinished = -1.0d;
		currMachine.procStarted = -1.0d;
		freeMachines.addFirst(currMachine);

		numBusy--;
		assert numBusy >= 0 && numBusy <= numInGroup;

		// start a job on this machine
		if (numJobsWaiting() > 0)
			selectAndStart();

		if (numListener() > 0) {
			fire(WS_ACTIVATED);
		}

		if (currMachine.timeBetweenFailures != null) {
			// schedule next down time
			double nextFailure = shop.simTime()
					+ currMachine.timeBetweenFailures.nextDbl();
			currMachine.takeDownEvent.setTime(nextFailure);
			shop.schedule(currMachine.takeDownEvent);
		}
	}

	/** Machine going down. */
	protected void takeDown() {
		if (currMachine.state != MachineState.IDLE) {
			// don't interrupt ongoing operation / down time, postpone takeDown
			// instead
			assert currMachine.procFinished > shop.simTime();
			assert currMachine.curJob != null;
			currMachine.takeDownEvent.setTime(currMachine.procFinished);
			shop.schedule(currMachine.takeDownEvent);
		} else {
			assert currMachine.state == MachineState.IDLE;

			double whenReactivated = shop.simTime()
					+ currMachine.timeToRepair.nextDbl();

			currMachine.procStarted = shop.simTime();
			currMachine.procFinished = whenReactivated;
			currMachine.state = MachineState.DOWN;
			currMachine.curJob = null;
			freeMachines.remove(currMachine);
			numBusy++;

			if (numListener() > 0) {
				fire(WS_DEACTIVATED);
			}

			// schedule reactivation
			assert currMachine.activateEvent.getTime() <= shop.simTime();
			currMachine.activateEvent.setTime(whenReactivated);
			shop.schedule(currMachine.activateEvent);
		}
	}

	public void done() {
		selectEventCache = null;

		if (numListener() > 0) {
			fire(WS_DONE);
		}
	}

	public void produceResults(Map<String, Object> res) {
		resultMap = res;
		fire(WS_COLLECT_RESULTS);
		resultMap = null;
	}

	/**
	 * Job 'j' arrives at a machine.
	 */
	public void enqueueOrProcess(Job j) {
		assert this == j.getCurrentOperation().machine;
		assert !j.isFuture();

		// remove the job's future from the queue if present
		if (numFutures > 0) {
			removeFromQueue(j.getMyFuture());
		}

		addToQueue(j, shop.simTime());
	}

	/**
	 * The machine is notified of the future arrival of the job {@code f} at a
	 * certain time. Note that f is not the job itself but a clone of this job
	 * with current operation advanced to this machine obtained with
	 * {@link Job#getFuture()}.
	 */
	public void futureArrival(final Job f, final double arrivesAt) {
		// execute asynchronously a little later so exactly concurrent job
		// selections don't see each others results
		shop.schedule(new Event(shop.simTime(), LOOKAHEAD_PRIO) {

			@Override
			public void handle() {
				assert f.isFuture();
				assert !queue.contains(f);
				addToQueue(f, arrivesAt);
			}
		});
	}

	private void addToQueue(Job j, double arrivesAt) {
		j.arriveInQueue(this, arrivesAt);

		queue.add(j);
		Operation o = j.getCurrentOperation();
		if (!batchingUsed) {
			batchingUsed = !BATCH_INCOMPATIBLE.equals(o.batchFamily);
		}

		if (!j.isFuture()) {
			workContentReal += o.procTime;
		} else {
			numFutures++;
			workContentFuture += o.procTime;
		}

		if (jobsPerBatchFamily != null)
			addJobToBatchFamily(j);
		
		if (numListener() > 0) {
			justArrived = j;
			fire(WS_JOB_ARRIVAL);
			justArrived = null;
		}

		// are there jobs that could be started and at least a free
		// machine
		if (numBusy < numInGroup && numJobsWaiting() > 0) {
			// at least 1 machine idle, start job selection
			selectAndStart();
		}
	}

	public void removeFromQueue(Job j) {
		boolean removeRes = queue.remove(j);
		j.removedFromQueue();

		if (!j.isFuture()) {
			Operation o = j.getCurrentOperation();
			workContentReal -= o.procTime;
			assert workContentReal >= -1e-6 : "" + workContentReal;
			assert removeRes;
			if (jobsPerBatchFamily != null)
				removeJobOfBatchFamily(j, o.batchFamily);
		} else {
			if (removeRes) {
				Operation o = j.getOps()[j.getTaskNumber() - 1];
				workContentFuture -= o.procTime;
				assert workContentFuture >= -1e-6 : "" + workContentFuture;
				numFutures--;
				if (jobsPerBatchFamily != null)
					removeJobOfBatchFamily(j, o.batchFamily);
			}
		}
	}

	/**
	 * Start processing the current batch/job.
	 */
	protected void startProc(final PrioRuleTarget batch) {
		assert !batch.isFuture();
		for (int i = 0; i < batch.numJobsInBatch(); i++) {
			assert !queue.contains(batch.job(i).getMyFuture());
		}
		assert numBusy < numInGroup;
		assert batch.getCurrentOperation().machine == this;
		assert currMachine.state == MachineState.IDLE;

		double simTime = shop.simTime();
		freeMachines.remove(currMachine);

		// remove job/batch's jobs from queue
		for (int i = 0; i < batch.numJobsInBatch(); i++) {
			Job job = batch.job(i);
			removeFromQueue(job);
		}

		// at least 1 machine idle, start job
		numBusy++;

		Operation op = batch.getCurrentOperation();

		oldSetupState = currMachine.setupState;
		newSetupState = op.setupState;
		setupTime = 0.0;
		if (oldSetupState != newSetupState) {
			setupTime = setupMatrix[oldSetupState][newSetupState];
			currMachine.setupState = newSetupState;
		}

		double tCompl = simTime + op.procTime + setupTime;
		currMachine.onDepart.setTime(tCompl);
		currMachine.procFinished = tCompl;
		currMachine.procStarted = simTime;
		currMachine.curJob = batch;
		shop.schedule(currMachine.onDepart);

		for (int i = 0; i < batch.numJobsInBatch(); i++) {
			Job j = batch.job(i);
			j.startProcessing();
		}

		currMachine.state = MachineState.WORKING;
	}

	/** Called when an operation of Job j is finished. */
	protected void depart() {
		assert currMachine.state == MachineState.WORKING;

		PrioRuleTarget b = currMachine.curJob;
		currMachine.curJob = null;

		currMachine.state = MachineState.IDLE;
		currMachine.procFinished = -1.0d;
		currMachine.procStarted = -1.0d;
		freeMachines.addFirst(currMachine);

		numBusy--;

		if (numListener() > 0) {
			justCompleted = b;
			fire(WS_JOB_COMPLETED);
			justCompleted = null;
		}

		currMachine = null;

		for (int i = 0, n = b.numJobsInBatch(); i < n; i++) {
			Job j = b.job(i);
			j.endProcessing();
			// send jobs to next machine
			j.proceed();
		}

		// start next job on this machine
		if (numJobsWaiting() > 0)
			selectAndStart();
	}

	/**
	 * Selects the next batch from the queue and starts processing. Even though
	 * this method is public it should never be called externally unless you
	 * know exactly what you are doing.
	 */
	public void selectAndStart() {
		// reuse select event objects
		SelectEvent e = selectEventCache;
		if (e == null) {
			e = new SelectEvent(shop.simTime());
		} else {
			selectEventCache = e.next;
			e.setTime(shop.simTime());
		}

		// execute asynchronously so all jobs arrived/departed before selection
		shop.schedule(e);
	}

	protected void selectAndStart0() {
		assert numJobsWaiting() > 0;
		assert numBusy < numInGroup;

		PrioRuleTarget nextBatch = nextJobAndMachine();
		assert freeMachines.contains(currMachine);

		// start work on selected job/batch
		if (nextBatch != null) {
			startProc(nextBatch);
		}

		// inform listener
		if (numListener() > 0) {
			justStarted = nextBatch;
			fire(WS_JOB_SELECTED);
			justStarted = null;
		}

		currMachine = null;
	}

	protected PrioRuleTarget nextJobAndMachine() {
		// just a check if freeMachines contains the right data
		for (IndividualMachine md : machDat) {
			if (md.state == MachineState.IDLE)
				assert freeMachines.contains(md);
		}
		assert freeMachines.size() > 0;

		PrioRuleTarget maxJob;

		if (!batchingUsed) {
			// normal job
			currMachine = freeMachines.peekLast();
			maxJob = queue.peekLargest();

			if (freeMachines.size() > 1) {
				// more than a single machine are free, we have to check them
				// all
				IndividualMachine maxMachine = currMachine;
				double[] maxPrio = queue.getBestPrios();
				if (maxPrio != null)
					maxPrio = maxPrio.clone();

				Iterator<IndividualMachine> it = freeMachines
						.descendingIterator();
				// skip first entry, which was already considered
				it.next();
				while (it.hasNext()) {
					currMachine = it.next();

					Job job = queue.peekLargest();
					double[] prios = queue.getBestPrios();

					if (maxPrio == null
							|| PriorityQueue.comparePrioArrays(maxPrio, prios) >= 0) {
						// copy priorities
						maxPrio = prios;
						if (maxPrio != null)
							maxPrio = maxPrio.clone();
						// remember job and machine
						maxJob = job;
						maxMachine = currMachine;
					}
				}

				currMachine = maxMachine;
			}
		} else {
			// batch machine

			// TODO: check all free machines similarly to normal jobs; only
			// makes a difference if batch machines can have setups too
			currMachine = freeMachines.peekLast();
			maxJob = getBatchForming().nextBatch();
		}

		if (maxJob == null || maxJob.isFuture()) {
			return null;
		} else {
			return maxJob;
		}
	}

	/**
	 * Return the number of jobs waiting in {@link #queue}, ready to be started
	 * immediately. This does not include the KeepIdleDummy. This means, the
	 * following equation holds: queue.size()=={@link #numJobsWaiting()}+
	 * {@link #numFutures()}+1.
	 * 
	 * @see #numFutures()
	 */
	public int numJobsWaiting() {
		int res = queue.size() - numFutures;
		return res;
	}

	/**
	 * Returns the number of future jobs in the {@link #queue}. This does not
	 * include the KeepIdleDummy.
	 * 
	 * @see #numJobsWaiting()
	 */
	public int numFutures() {
		return numFutures;
	}

	/**
	 * How much work have all machines in this group to finish their current
	 * jobs.
	 */
	public double startedWorkInGroup() {
		double res = 0.0;
		for (int i = 0; i < machDat.length; i++) {
			if (machDat[i].procFinished > shop.simTime())
				res += (machDat[i].procFinished - shop.simTime());
		}
		return res;
	}

	public double againIdleIn() {
		assert numInGroup == 1;
		return startedWorkInGroup();
	}

	public double againIdle() {
		return againIdleIn() + shop.simTime();
	}

	/**
	 * Returns the sum of processing times of all operations currently waiting
	 * in this machine's queue.
	 */
	public double workContent(boolean includeFutureJobs) {
		// jobs already started
		double res = startedWorkInGroup() + workContentReal;

		if (includeFutureJobs)
			res += workContentFuture;

		// normalize with machine group size
		return res / numInGroup;
	}

	public PrioRuleTarget getProcessedJob(int machIdx) {
		return machDat[machIdx].curJob;
	}

	public int getSetupState(int machIdx) {
		return machDat[machIdx].setupState;
	}

	public void setSetupMatrix(double[][] setupMatrix) {
		this.setupMatrix = setupMatrix;
	}

	public double[][] getSetupMatrix() {
		return setupMatrix;
	}

	/**
	 * Translates a setup state {@code s} in a numeric constant.
	 * 
	 * @see #setupStateToString(int)
	 * @param s
	 *            A setup state name.
	 * @return Numeric constant for {@code s}.
	 */
	public int translateSetupState(String s) {
		if (DEF_SETUP_STR.equals(s)) {
			return DEF_SETUP;
		} else {
			if (setupStateTranslate == null) {
				setupStateTranslate = new ArrayList<String>();
				setupStateTranslate.add(DEF_SETUP_STR); // ensure an index of 0
			}
			int i = setupStateTranslate.indexOf(s);
			if (i < 0) {
				i = setupStateTranslate.size();
				setupStateTranslate.add(s);
			}
			return i;
		}
	}

	/**
	 * Provides a human-readable string for a numeric setup state.
	 * 
	 * @see #translateSetupState(String)
	 * @param id
	 *            The numeric setup id. This was usually (optionally) created
	 *            before using {@code translateSetupState(String)}
	 */
	public String setupStateToString(int id) {
		if (id == DEF_SETUP)
			return DEF_SETUP_STR;
		else {
			if (setupStateTranslate != null & id >= 0
					&& id < setupStateTranslate.size())
				return setupStateTranslate.get(id);
			else
				return "sId" + id;
		}
	}

	public int numFreeMachines() {
		return freeMachines.size();
	}

	public Collection<IndividualMachine> getFreeMachines() {
		return Collections.unmodifiableCollection(freeMachines);
	}

	public IndividualMachine[] machDat() {
		return machDat;
	}

	public int numBusy() {
		return numBusy;
	}

	public int numInGroup() {
		return numInGroup;
	}

	public JobShop shop() {
		return shop;
	}

	public Map<String, List<Job>> getJobsByFamily() {
		if (jobsPerBatchFamily == null) {
			jobsPerBatchFamily = new HashMap<String, List<Job>>();
			for (int i = 0, n = queue.size(); i < n; i++) {
				Job j = queue.get(i);
				addJobToBatchFamily(j);
			}
		}
		return jobsPerBatchFamily;
	}

	private void addJobToBatchFamily(Job j) {
		String bf = j.getCurrentOperation().batchFamily;

		List<Job> jobsInFamily = jobsPerBatchFamily.get(bf);
		if (jobsInFamily == null) {
			jobsInFamily = new ArrayList<Job>();
			jobsPerBatchFamily.put(bf, jobsInFamily);
		}

		jobsInFamily.add(j);
	}

	private void removeJobOfBatchFamily(Job j, String bf) {
		List<Job> jobsInFamily = jobsPerBatchFamily.get(bf);
		boolean removeRes = jobsInFamily.remove(j);
		assert removeRes;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name == null ? "m" + index : name;
	}

	public void setBatchForming(BatchForming formBatch) {
		this.batchForming = formBatch;
		if (formBatch != null)
			formBatch.setOwner(this);
	}

	public BatchForming getBatchForming() {
		return batchForming;
	}

	public void setBatchSequencingRule(PR batchSequencingRule) {
		this.batchSequencingRule = batchSequencingRule;
		if (batchSequencingRule != null)
			batchSequencingRule.setOwner(this);
	}

	public PR getBatchSequencingRule() {
		return batchSequencingRule;
	}

	//
	//
	// event notification
	//
	//

	private NotifierAdapter<WorkStation, WorkStationEvent> adapter = null;

	@Override
	public void addNotifierListener(
			NotifierListener<WorkStation, WorkStationEvent> listener) {
		if (adapter == null)
			adapter = new NotifierAdapter<WorkStation, WorkStationEvent>(this);
		adapter.addNotifierListener(listener);
	}

	@Override
	public NotifierListener<WorkStation, WorkStationEvent> getNotifierListener(
			int index) {
		return adapter.getNotifierListener(index);
	}

	@Override
	public void removeNotifierListener(
			NotifierListener<WorkStation, WorkStationEvent> listener) {
		adapter.removeNotifierListener(listener);
	}

	@Override
	public int numListener() {
		return adapter == null ? 0 : adapter.numListener();
	}

	protected void fire(WorkStationEvent event) {
		if (adapter != null)
			adapter.fire(event);
	}

	/**
	 * Offers a simple get/put-mechanism to store and retrieve information as a
	 * kind of global data store. This can be used as a simple extension
	 * mechanism.
	 * 
	 * @param key
	 *            The key name.
	 * @param value
	 *            value to assign to {@code key}.
	 * @see #valueStoreGet(String)
	 */
	public void valueStorePut(Object key, Object value) {
		if (valueStore == null)
			valueStore = new HashMap<Object, Object>();
		valueStore.put(key, value);
	}

	/**
	 * Retrieves a value from the value store.
	 * 
	 * @param key
	 *            The entry to return, e.g., identified by a name.
	 * @return The value associated with {@code key}.
	 * @see #valueStorePut(Object, Object)
	 */
	public Object valueStoreGet(Object key) {
		if (valueStore == null)
			return null;
		else
			return valueStore.get(key);
	}

}
