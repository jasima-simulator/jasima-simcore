/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.core;

import jasima.core.util.TypeUtil;
import jasima.core.util.ValueStore;
import jasima.core.util.ValueStoreImpl;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierImpl;

/**
 * Main work unit in a shop.
 * 
 * @author Torsten Hildebrandt
 */
// TODO: PrioRuleTarget should be an interface
public class Job extends PrioRuleTarget implements Cloneable, ValueStore, Notifier<Job, Object> {

	/** Base class for job messages. */
	public static class JobMessage {

		private final String msgName;

		public JobMessage(String name) {
			super();
			this.msgName = name;
		}

		@Override
		public String toString() {
			return msgName;
		}

		// constants for events thrown by a job

		public static final JobMessage JOB_RELEASED = new JobMessage("JOB_RELEASED");
		public static final JobMessage JOB_FINISHED = new JobMessage("JOB_FINISHED");
		public static final JobMessage JOB_ARRIVED_IN_QUEUE = new JobMessage("JOB_ARRIVED_IN_QUEUE");
		public static final JobMessage JOB_REMOVED_FROM_QUEUE = new JobMessage("JOB_REMOVED_FROM_QUEUE");
		public static final JobMessage JOB_START_OPERATION = new JobMessage("JOB_START_OPERATION");
		public static final JobMessage JOB_END_OPERATION = new JobMessage("JOB_END_OPERATION");
	}

	private final Shop shop;
	// delegate Notifier functionality
	private NotifierImpl<Job, Object> notifierAdapter;
	// delegate ValueStore functionality
	private ValueStoreImpl valueStore;

	private double arriveTime; // arrival time at current machine
	private WorkStation currMachine;
	// when will job finish processing on its current machine (if started)
	private double startTime;
	private double finishTime;
	private double relDate;
	private double dueDate;
	private int jobNum; // global number of job in system
	private int jobType;
	private double weight = 1.0d;
	private int taskNumber; // current operation
	private Operation[] ops;
	private double[] opDueDates;
	private boolean isFuture = false;
	private String name = null;
	private Route route = null;
	// we cache the value returned by remainingProcTime()
	private double remProcTime = -1.0d;

	private Job future;

	public Job(Shop shop) {
		super();

		this.shop = shop;

		notifierAdapter = new NotifierImpl<Job, Object>(this);
		valueStore = new ValueStoreImpl();
	}

	public void setArriveTime(double fl) {
		arriveTime = fl;
	}

	public double getArriveTime() {
		return arriveTime;
	}

	public void setJobType(int job) {
		jobType = job;
	}

	public int getJobType() {
		return jobType;
	}

	public void setTaskNumber(int tn) {
		remProcTime = -1.0d;
		taskNumber = tn;
	}

	public int getTaskNumber() {
		return taskNumber;
	}

	public void setCurrMachine(WorkStation currMachine) {
		this.currMachine = currMachine;
	}

	public WorkStation getCurrMachine() {
		return currMachine;
	}

	public Operation getCurrentOperation() {
		return ops[taskNumber];
	}

	public double currProcTime() {
		return ops[taskNumber].getProcTime();
	}

	public double procSum() {
		double res = 0d;
		for (Operation o : ops) {
			res += o.getProcTime();
		}
		return res;
	}

	public double remainingProcTime() {
		if (remProcTime < 0.0d) {
			remProcTime = 0f;
			Operation[] ops = this.ops;
			for (int i = taskNumber; i < ops.length; i++) {
				remProcTime += ops[i].getProcTime();
			}
		}
		return remProcTime;
	}

	public int numOps() {
		return getOps().length;
	}

	public int numOpsLeft() {
		return getOps().length - getTaskNumber();
	}

	/**
	 * If the this job has one or more tasks yet to be done, send the job to the
	 * next machine on its route
	 */
	void proceed() {
		if (!isLastOperation()) {
			setTaskNumber(getTaskNumber() + 1);

			WorkStation mNext = ops[taskNumber].getMachine();
			mNext.enqueueOrProcess(this);
		} else {
			shop.jobFinished(this);
		}
	}

	void jobReleased() {
		if (numListener() > 0)
			fire(JobMessage.JOB_RELEASED);
	}

	void jobFinished() {
		if (numListener() > 0)
			fire(JobMessage.JOB_FINISHED);
	}

	void arriveInQueue(WorkStation workStation, double arrivesAt) {
		setCurrMachine(workStation);
		setArriveTime(arrivesAt);

		if (numListener() > 0)
			fire(JobMessage.JOB_ARRIVED_IN_QUEUE);
	}

	void removedFromQueue() {
		if (numListener() > 0)
			fire(JobMessage.JOB_REMOVED_FROM_QUEUE);
	}

	void startProcessing() {
		setFinishTime(currMachine.currMachine.procFinished);
		setStartTime(currMachine.shop().simTime());
		notifyNextMachine();

		if (numListener() > 0)
			fire(JobMessage.JOB_START_OPERATION);
	}

	void endProcessing() {
		if (numListener() > 0)
			fire(JobMessage.JOB_END_OPERATION);
	}

	/**
	 * Notify next machine of future arrival. This mehod is called whenever an
	 * operation is started. This method assumes isFinished to be set to the correct
	 * value before this method is called.
	 */
	public void notifyNextMachine() {
		if (!isLastOperation() && shop.isEnableLookAhead()) {
			final Job f = getMyFuture();
			final WorkStation next = f.ops[f.taskNumber].getMachine();
			next.futureArrival(f, getFinishTime());
		}
	}

	/**
	 * Returns a clone of this Job switched to the next operation.
	 * 
	 * @return The future clone of this job.
	 */
	public Job getMyFuture() {
		if (future == null) {
			future = clone();
			future.setFuture(true);
		}
		future.setTaskNumber(taskNumber + 1);
		return future;
	}

	void setFuture(boolean isFuture) {
		this.isFuture = isFuture;
	}

	public boolean isFuture() {
		return isFuture;
	}

	public boolean isLastOperation() {
		return taskNumber == ops.length - 1;
	}

	@Override
	public String toString() {
		String s = getName() + "#" + taskNumber;
		if (isFuture())
			s += "(future)";
		return s;
	}

	public Shop getShop() {
		return shop;
	}

	public double getDueDate() {
		return dueDate;
	}

	public void setDueDate(double dueDate) {
		this.dueDate = dueDate;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Job getFuture() {
		return future;
	}

	public void setFuture(Job future) {
		this.future = future;
	}

	public void setRelDate(double relDate) {
		this.relDate = relDate;
	}

	public double getRelDate() {
		return relDate;
	}

	public void setJobNum(int jobNum) {
		this.jobNum = jobNum;
	}

	@Override
	public int getJobNum() {
		return jobNum;
	}

	/**
	 * Sets the completion time of the current operation. This is called by a
	 * machine whenever processing starts.
	 * 
	 * @param finishTime The finish time of the current {@link Operation}.
	 */
	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;
	}

	public double getFinishTime() {
		return finishTime;
	}

	public double getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time of the current operation. This is used internally and
	 * called by a machine whenever processing starts.
	 * 
	 * @param startTime The start time of the current operation.
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public void setOpDueDates(double[] opDueDates) {
		this.opDueDates = opDueDates;
	}

	public double[] getOpDueDates() {
		return opDueDates;
	}

	@Override
	public double getCurrentOperationDueDate() {
		if (opDueDates == null) {
			setOpDueDates(computeDueDatesTWC(this, (dueDate - relDate) / procSum()));
		}

		return opDueDates[taskNumber];
	}

	public void setOps(Operation[] ops) {
		this.ops = ops;
		this.opDueDates = null;
	}

	public Operation[] getOps() {
		return ops;
	}

	@Override
	public Job job(int i) {
		if (i != 0)
			throw new IllegalArgumentException("" + i);
		return this;
	}

	@Override
	public int numJobsInBatch() {
		return 1;
	}

	/**
	 * Computes operational due dates based on the total work content method, /*
	 * i.e., proportional to an operation's processing time.
	 * 
	 * @param j  The job for which to compute operation due dates.
	 * @param ff The due date factor to use.
	 * @return An array containing operation due dates for each operation of
	 *         {@code j}.
	 */
	public static double[] computeDueDatesTWC(Job j, double ff) {
		Operation[] ops = j.ops;

		double[] res = new double[ops.length];

		double due = j.getRelDate();

		for (int i = 0; i < res.length; i++) {
			due += ff * ops[i].getProcTime();
			res[i] = due;
		}

		return res;
	}

	@Override
	public boolean isBatch() {
		return false;
	}

	@Override
	public String getName() {
		if (name == null)
			return getClass().getSimpleName() + "." + jobType + "." + getJobNum();
		else
			return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The route this object is following (might be null).
	 */
	public Route getRoute() {
		return route;
	}

	/**
	 * Sets the {@link Route} this object is following. This might be {@code null},
	 * as not every job has to be created using a {@code Route} (e.g. in a dynamic
	 * job shop where each {@code Job} has its unique rule).
	 * 
	 * @param route The route this Job is following.
	 */
	public void setRoute(Route route) {
		this.route = route;
	}

	//
	//
	// ValueStore implementation
	//
	//

	@Override
	public ValueStore valueStoreImpl() {
		return valueStore;
	}

	//
	//
	// Event notification
	//
	//

	@Override
	public Notifier<Job, Object> notifierImpl() {
		return notifierAdapter;
	}

	// cloning

	@Override
	public Job clone() {
		try {
			Job j = (Job) super.clone();
			j.future = null;

			// clone value store copying (but not cloning!) all of its entries
			j.valueStore = valueStore.clone();

			// clone listeners
			j.notifierAdapter = new NotifierImpl<>(j);
			for (int i = 0; i < numListener(); i++) {
				j.addListener(TypeUtil.cloneIfPossible(getListener(i)));
			}

			return j;
		} catch (CloneNotSupportedException shouldNeverOccur) {
			throw new AssertionError(shouldNeverOccur);
		}
	}

}
