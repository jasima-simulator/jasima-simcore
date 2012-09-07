/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.shopSim.core;

import jasima.core.simulation.Simulation;
import jasima.core.util.Util;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.WorkStation.WorkStationEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a shop simulation. Despite its name the scenario not necessarily
 * has to be a job shop.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class JobShop extends Simulation {

	public static class JobShopEvent extends SimEvent {
	}

	// constants for default events thrown by a shop

	public static final JobShopEvent JOB_RELEASED = new JobShopEvent();
	public static final JobShopEvent JOB_FINISHED = new JobShopEvent();

	private int maxJobsInSystem = 0; // end simulation if WIP reaches this value
										// (0: no limit)
	private int maxJobsFinished = 0; // end simulation if this number of jobs
										// was finished (0: no limit)

	public boolean enableLookAhead = true;

	public JobSource[] sources = {};
	public WorkStation[] machines = {};
	public Route[] routes = {};

	public int jobsFinished;
	public int jobsStarted;

	private boolean strangePriorities;

	private HashMap<String, Object> valueStore;

	// fields used during event notification
	public Job lastJobReleased;
	public Job lastJobFinished;

	public JobShop() {
		super();
	}

	@Override
	public void init() {
		super.init();

		jobsStarted = jobsFinished = 0;

		for (WorkStation m : machines)
			m.init();

		for (JobSource s : sources)
			s.init();
	}

	@Override
	public void run() {
		super.run();
	}

	@Override
	public void done() {
		super.done();

		for (WorkStation m : machines)
			m.done();

		strangePriorities = false;
		for (WorkStation m : machines) {
			if (m.queue.strangePrioValuesFound) {
				strangePriorities = true;
				break;
			}
		}
	}

	public void addMachine(WorkStation machine) {
		ArrayList<WorkStation> list = new ArrayList<WorkStation>(
				Arrays.asList(machines));
		list.add(machine);

		machine.shop = this;
		machine.index = list.size() - 1;

		machines = list.toArray(new WorkStation[list.size()]);
	}

	public void addJobSource(JobSource js) {
		ArrayList<JobSource> list = new ArrayList<JobSource>(
				Arrays.asList(sources));
		list.add(js);

		js.setShop(this);
		js.index = list.size() - 1;

		sources = list.toArray(new JobSource[list.size()]);
	}

	public void jobFinished(Job j) {
		jobsFinished++;

		if (getMaxJobsFinished() > 0 && jobsFinished >= getMaxJobsFinished()) {
			// System.out.println("" + getMaxJobsFinished()
			// + " jobs finished, aborting sim (job starvation?).");
			end(); // abort simulation
		}

		lastJobFinished = j;
		if (numListener() > 0)
			fire(JOB_FINISHED);
	}

	protected void startJob(Job nextJob) {
		nextJob.setJobNum(jobsStarted++);

		if (getMaxJobsInSystem() > 0
				&& jobsStarted - jobsFinished >= getMaxJobsInSystem()) {
			print(SimMsgCategory.WARN, "WIP reaches " + getMaxJobsInSystem()
					+ ", aborting sim.");
			end(); // abort simulation
		}

		lastJobReleased = nextJob;
		if (numListener() > 0)
			fire(JOB_RELEASED);

		WorkStation mach = nextJob.getCurrentOperation().machine;
		mach.enqueueOrProcess(nextJob);
	}

	@Override
	public void produceResults(Map<String, Object> res) {
		super.produceResults(res);

		res.put("numJobsFinished", jobsFinished);
		res.put("numJobsStarted", jobsStarted);
		res.put("numWIP", jobsStarted - jobsFinished);

		res.put("strangePriorities", strangePriorities ? 1 : 0);

		for (WorkStation m : machines) {
			m.produceResults(res);
		}
	}

	/**
	 * Adds a listener to all machines ({@link WorkStation}s).
	 * 
	 * @param listener
	 *            The machine listener to add.
	 * @param cloneIfPossible
	 *            whether to try to clone a new instance for each machine using
	 *            {@link Util#cloneIfPossible(Object)}.
	 */
	public void installMachineListener(
			NotifierListener<WorkStation, WorkStationEvent> listener,
			boolean cloneIfPossible) {
		for (WorkStation m : machines) {
			NotifierListener<WorkStation, WorkStationEvent> ml = listener;
			if (cloneIfPossible)
				ml = Util.cloneIfPossible(listener);
			m.addNotifierListener(ml);
		}
	}

	/**
	 * Offers a simple get/put-mechanism to store and retrieve information as a
	 * kind of global data store.
	 * 
	 * @param key
	 *            The key name.
	 * @param value
	 *            value to assign to key.
	 */
	public void valueStorePut(String key, Object value) {
		if (valueStore == null)
			valueStore = new HashMap<String, Object>();
		valueStore.put(key, value);
	}

	public Object valueStoreGet(String key) {
		if (valueStore == null)
			return null;
		else
			return valueStore.get(key);
	}

	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	public void setEnableLookAhead(boolean enableLookAhead) {
		this.enableLookAhead = enableLookAhead;
	}

	public void setMaxJobsInSystem(int maxJobsInSystem) {
		this.maxJobsInSystem = maxJobsInSystem;
	}

	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	public void setMaxJobsFinished(int maxJobsFinished) {
		this.maxJobsFinished = maxJobsFinished;
	}

	public int getMaxJobsFinished() {
		return maxJobsFinished;
	}

}
