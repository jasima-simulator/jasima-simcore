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

import jasima.core.simulation.Simulation;
import jasima.core.util.Util;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.WorkStation.WorkStationEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Implements a shop simulation. Despite its name the scenario not necessarily
 * has to be a job shop.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class JobShop extends Simulation {

	public static class JobShopEvent extends SimEvent {
	}

	// constants for default events thrown by a shop (in addition to simulation
	// events)
	public static final JobShopEvent JOB_RELEASED = new JobShopEvent();
	public static final JobShopEvent JOB_FINISHED = new JobShopEvent();

	// parameters
	private int maxJobsInSystem = 0;
	private int maxJobsFinished = 0;
	private boolean enableLookAhead = true;

	public JobSource[] sources = {};
	public WorkStation[] machines = {};
	public Route[] routes = {};

	public int jobsFinished;
	public int jobsStarted;

	private boolean strangePriorities;

	// fields used during event notification
	public Job lastJobReleased;
	public Job lastJobFinished;

	public JobShop() {
		super();
	}

	@Override
	protected void init() {
		super.init();

		jobsStarted = jobsFinished = 0;

		for (WorkStation m : machines)
			m.init();

		for (JobSource s : sources)
			s.init();
	}

	@Override
	protected void done() {
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

	public void startJob(Job nextJob) {
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
	 * Adds a listener to all {@link WorkStation}s in the shop.
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
	 * Returns the status of lookahead mechanism.
	 * 
	 * @return Whether lookahead is used.
	 */
	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	/**
	 * Enable the lookahead mechanism of this shop.
	 * 
	 * @param enableLookAhead
	 *            Whether to enable or disable lookahead.
	 */
	public void setEnableLookAhead(boolean enableLookAhead) {
		this.enableLookAhead = enableLookAhead;
	}

	/**
	 * End simulation if WIP (work in process) reaches this value (0: no limit)
	 * 
	 * @param maxJobsInSystem
	 *            The maximum number of jobs in the system.
	 */
	public void setMaxJobsInSystem(int maxJobsInSystem) {
		this.maxJobsInSystem = maxJobsInSystem;
	}

	/**
	 * Returns the maximum number of jobs in the system.
	 */
	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	/**
	 * End simulation if a certain number of jobs was completed (0 (default): no
	 * limit).
	 * 
	 * @param maxJobsFinished
	 *            The number of jobs to finish.
	 */
	public void setMaxJobsFinished(int maxJobsFinished) {
		this.maxJobsFinished = maxJobsFinished;
	}

	/**
	 * Returns the number of jobs to complete before the simulation is ended.
	 * 
	 * @return The number of jobs to complete before terminating the simulation.
	 */
	public int getMaxJobsFinished() {
		return maxJobsFinished;
	}

	/**
	 * Gets the list of job sources in this shop.
	 */
	public JobSource[] getSources() {
		return sources;
	}

	/**
	 * Sets all job sources in this shop.
	 */
	public void setSources(JobSource[] sources) {
		this.sources = sources;

		int i = 0;
		for (JobSource js : sources) {
			js.setShop(this);
			js.index = i++;
		}
	}

	public void addJobSource(JobSource js) {
		ArrayList<JobSource> list = new ArrayList<JobSource>(
				Arrays.asList(sources));
		list.add(js);

		js.setShop(this);
		js.index = list.size() - 1;

		sources = list.toArray(new JobSource[list.size()]);
	}

	public void removeJobSource(JobSource js) {
		ArrayList<JobSource> list = new ArrayList<JobSource>(
				Arrays.asList(sources));
		if (list.remove(js)) {
			js.setShop(null);
			js.index = -1;
			int i = 0;
			for (JobSource s : list) {
				s.index = i++;
			}
			sources = list.toArray(new JobSource[list.size()]);
		}
	}

	/**
	 * Gets the list of workstations in this shop.
	 */
	public WorkStation[] getMachines() {
		return machines;
	}

	/**
	 * Sets the workstations of this shop.
	 */
	public void setMachines(WorkStation[] machines) {
		this.machines = machines;
		int i = 0;
		for (WorkStation w : machines) {
			w.shop = this;
			w.index = i++;
		}
	}

	/**
	 * Adds a single machine to this shop.
	 * 
	 * @see #getMachines()
	 */
	public void addMachine(WorkStation machine) {
		ArrayList<WorkStation> list = new ArrayList<WorkStation>(
				Arrays.asList(machines));
		list.add(machine);

		machine.shop = this;
		machine.index = list.size() - 1;

		machines = list.toArray(new WorkStation[list.size()]);
	}

	/**
	 * Remove a machine from this shop.
	 */
	public void removeMachine(WorkStation machine) {
		ArrayList<WorkStation> list = new ArrayList<WorkStation>(
				Arrays.asList(machines));
		if (list.remove(machine)) {
			machine.shop = null;
			machine.index = -1;
			int i = 0;
			for (WorkStation w : list) {
				w.index = i++;
			}
			machines = list.toArray(new WorkStation[list.size()]);
		}
	}

	/**
	 * Returns the routes added to this job shop.
	 */
	public Route[] getRoutes() {
		return routes;
	}

	/**
	 * Sets the routes available for this job shop.
	 */
	public void setRoutes(Route[] routes) {
		this.routes = routes;
	}

	public void addRoute(Route r) {
		ArrayList<Route> list = new ArrayList<Route>(Arrays.asList(routes));
		list.add(r);
		routes = list.toArray(new Route[list.size()]);
	}

	public void removeRoute(Route r) {
		ArrayList<Route> list = new ArrayList<Route>(Arrays.asList(routes));
		if (list.remove(r)) {
			routes = list.toArray(new Route[list.size()]);
		}
	}

}
