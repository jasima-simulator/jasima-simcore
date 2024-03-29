/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.shopSim.core;

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentContainer;
import jasima.core.simulation.SimComponentContainerBase;
import jasima.core.util.MsgCategory;
import jasima.core.util.TypeUtil;
import jasima.core.util.observer.NotifierListener;

/**
 * Implements a shop simulation. The scenarios covered are usually job shops and
 * flow shops.
 * 
 * @author Torsten Hildebrandt
 */
public class Shop extends SimComponentContainerBase {

	public enum ShopMessage implements SimComponentEvent {
		JOB_RELEASED, JOB_FINISHED
	}

	// parameters
	private int maxJobsInSystem = 0;
	private int stopAfterNumJobs = 0;
	private boolean enableLookAhead = false;

	private SimComponentContainerBase sources;
	private SimComponentContainerBase machines;
	public Route[] routes = {};

	public int jobsFinished;
	public int jobsStarted;

	// fields used during event notification
	public Job lastJobReleased;
	public Job lastJobFinished;

	public Shop() {
		super();

		sources = new SimComponentContainerBase();
		addChild(sources);

		machines = new SimComponentContainerBase();
		addChild(machines);
	}

	@Override
	public void init() {
		super.init();

		jobsStarted = jobsFinished = 0;
	}

	public void jobFinished(Job j) {
		jobsFinished++;

		if (getStopAfterNumJobs() > 0 && jobsFinished >= getStopAfterNumJobs()) {
			getSim().end(); // abort simulation
		}

		j.jobFinished();

		if (isTraceEnabled()) {
			trace("leave_system", j);
		}

		lastJobFinished = j;
		fire(ShopMessage.JOB_FINISHED);
	}

	public void startJob(Job nextJob) {
		nextJob.setJobNum(jobsStarted++);

		if (getMaxJobsInSystem() > 0 && (jobsStarted - jobsFinished) >= getMaxJobsInSystem()) {
			getSim().print(MsgCategory.WARN, defFormat("WIP reaches %d, aborting sim.", getMaxJobsInSystem()));
			getSim().end(); // abort simulation
		}

		nextJob.jobReleased();

		if (isTraceEnabled()) {
			trace("enter_system", nextJob);
		}

		lastJobReleased = nextJob;
		fire(ShopMessage.JOB_RELEASED);

		WorkStation mach = nextJob.getCurrentOperation().getMachine();
		mach.enqueueOrProcess(nextJob);
	}

	@Override
	public void produceResults(Map<String, Object> res) {
		super.produceResults(res);

		res.put("numJobsFinished", jobsFinished);
		res.put("numJobsStarted", jobsStarted);
		res.put("numWIP", jobsStarted - jobsFinished);
	}

	/**
	 * Adds a listener to all {@link WorkStation}s in the shop.
	 * 
	 * @param listener        The machine listener to add.
	 * @param cloneIfPossible whether to try to clone a new instance for each
	 *                        machine using
	 *                        {@link TypeUtil#cloneIfPossible(Object)}.
	 */
	public void installMachineListener(NotifierListener<SimComponent, SimComponentEvent> listener,
			boolean cloneIfPossible) {
		for (SimComponent sc : machines.getChildren()) {
			WorkStation m = (WorkStation) sc;
			NotifierListener<SimComponent, SimComponentEvent> ml = listener;
			if (cloneIfPossible)
				ml = TypeUtil.cloneIfPossible(ml);

			m.addListener(ml);
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
	 * Enable the lookahead mechanism of this shop. If enabled dispatching rules can
	 * select jobs arriving from in the near future.
	 * 
	 * @param enableLookAhead Whether to enable or disable lookahead.
	 */
	public void setEnableLookAhead(boolean enableLookAhead) {
		this.enableLookAhead = enableLookAhead;
	}

	/**
	 * End simulation if WIP (work in process) reaches this value (0: no limit)
	 * 
	 * @param maxJobsInSystem The maximum number of jobs in the system.
	 */
	public void setMaxJobsInSystem(int maxJobsInSystem) {
		this.maxJobsInSystem = maxJobsInSystem;
	}

	/**
	 * Returns the maximum number of jobs in the system, before the simulation is
	 * terminated.
	 * 
	 * @return The maximum number of jobs in the system.
	 */
	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	/**
	 * End simulation if a certain number of jobs was completed (%lt;=0 (default):
	 * no limit).
	 * 
	 * @param stopAfterNumJobs The number of jobs to finish.
	 */
	public void setStopAfterNumJobs(int stopAfterNumJobs) {
		this.stopAfterNumJobs = stopAfterNumJobs;
	}

	/**
	 * Returns the number of jobs to complete before the simulation is ended.
	 * 
	 * @return The number of jobs to complete before terminating the simulation.
	 */
	public int getStopAfterNumJobs() {
		return stopAfterNumJobs;
	}

	/**
	 * Gets the job sources in this shop as an array.
	 * 
	 * @return An array of job sources.
	 * @see #sources
	 */
	public JobSource[] getSources() {
		return sources.getChildren().toArray(new JobSource[0]);
	}

	/**
	 * Returns the {@link SimComponentContainer} containing all {@link JobSource}s
	 * of this shop.
	 * 
	 * @return The container object.
	 */
	public SimComponentContainer sources() {
		return sources;
	}

	/**
	 * Sets all job sources in this shop.
	 * 
	 * @param sources An array with all job sources.
	 */
	public void setSources(JobSource[] sources) {
		this.sources.removeChildren();

		for (JobSource js : sources) {
			addJobSource(js);
		}
	}

	public void addJobSource(JobSource js) {
		js.setShop(this);
		js.index = sources.numChildren();
		sources.addChild(js);
	}

	public void removeJobSource(JobSource js) {
		if (sources.removeChild(js)) {
			js.setShop(null);
			js.index = -1;

			int i = 0;
			for (SimComponent sc : sources.getChildren()) {
				JobSource s = (JobSource) sc;
				s.index = i++;
			}
		}
	}

	/**
	 * Gets an array of the workstations contained in this shop.
	 * 
	 * @return An array of all workstations of this shop.
	 * 
	 * @see #machines()
	 */
	public WorkStation[] getMachines() {
		return machines.getChildren().toArray(new WorkStation[machines.numChildren()]);
	}

	/**
	 * Returns the {@link SimComponentContainer} containing all {@link WorkStation}s
	 * of this shop.
	 * 
	 * @return The container object.
	 */
	public SimComponentContainer machines() {
		return machines;
	}

	/**
	 * Sets the workstations of this shop.
	 * 
	 * @param machines An array containing all workstations for this shop.
	 */
	public void setMachines(WorkStation[] machines) {
		this.machines.removeChildren();

		for (WorkStation ws : machines) {
			addMachine(ws);
		}
	}

	/**
	 * Adds a single machine to this shop.
	 * 
	 * @see #getMachines()
	 * @param machine The workstation to add.
	 */
	public void addMachine(WorkStation machine) {
		machine.shop = this;
		machine.index = machines.numChildren();
		machines.addChild(machine);
	}

	/**
	 * Removes a machine from this shop.
	 * 
	 * @param machine The workstation to remove.
	 */
	public void removeMachine(WorkStation machine) {
		if (machines.removeChild(machine)) {
			machine.shop = null;
			machine.index = -1;

			int i = 0;
			for (SimComponent sc : machines.getChildren()) {
				WorkStation w = (WorkStation) sc;
				w.index = i++;
			}
		}
	}

	/**
	 * Returns a workstation with the given name, or {@code null} if no such
	 * workstation exists.
	 * 
	 * @param name The workstation's name.
	 * @return The workstation with the given name, if it exists. {@code null}
	 *         otherwise.
	 */
	public WorkStation getWorkstationByName(String name) {
		WorkStation res = null;

		if (getMachines() != null)
			for (WorkStation w : getMachines()) {
				if (name.equals(w.getName())) {
					res = w;
					break; // for w
				}
			}

		return res;
	}

	/**
	 * Returns the routes added to this job shop. Do not modify externally.
	 * 
	 * @return An array of all routes in this shop.
	 */
	public Route[] getRoutes() {
		return routes;
	}

	/**
	 * Sets the routes available for this job shop.
	 * 
	 * @param routes The route list.
	 */
	public void setRoutes(Route[] routes) {
		this.routes = routes.clone();
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

	@Override
	public Shop clone() {
		throw new UnsupportedOperationException("clone()");
	}

}
