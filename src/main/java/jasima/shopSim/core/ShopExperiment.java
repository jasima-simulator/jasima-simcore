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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponent.SimComponentEvent;
import jasima.core.simulation.SimulationExperiment;
import jasima.core.util.TypeUtil;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.batchForming.BatchForming;

/**
 * Base class for shop experiments. This class wraps a {@link Shop}. Derived
 * classes will typically populate the shop with machines and JobSources and add
 * functionality to collect some statistics and produce appropriate experiment
 * results.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class ShopExperiment extends SimulationExperiment {

	private static final long serialVersionUID = 2660935009898060395L;

	// experiment parameters
	private int maxJobsInSystem = 0;
	private int stopAfterNumJobs = 0;
	private boolean enableLookAhead = false;

	private PR sequencingRule;
	private PR batchSequencingRule;
	private BatchForming batchForming;

	private PR[] sequencingRules;
	private PR[] batchSequencingRules;
	private BatchForming[] batchFormingRules;

	private NotifierListener<SimComponent, SimComponentEvent>[] shopListener;
	private NotifierListener<SimComponent, SimComponentEvent>[] machineListener;
	private HashMap<String, NotifierListener<SimComponent, SimComponentEvent>[]> machListenerSpecific;

	// fields used during experiment execution
	protected Shop shop;

	@Override
	protected void createSimComponents() {
		super.createSimComponents();

		createShop();
		configureShop();
	}

	protected void createShop() {
		shop = doCreateShop();
		sim.getRootComponent().addChild(shop);
	}

	/**
	 * Factory method to create/initialize a shop object.
	 * 
	 * @return The new {@link Shop} instance.
	 */
	protected Shop doCreateShop() {
		return new Shop();
	}

	protected void configureShop() {
		shop.setMaxJobsInSystem(getMaxJobsInSystem());
		shop.setStopAfterNumJobs(getStopAfterNumJobs());
		shop.setEnableLookAhead(isEnableLookAhead());

		// set dispatching rule of machines
		for (int i = 0, n = shop.machines().numChildren(); i < n; i++) {
			WorkStation m = (WorkStation) shop.machines().getChild(i);

			PR sr = getSequencingRule(i);
			if (sr != null) {
				sr = sr.clone();
				sr.setOwner(m);
				m.queue.setSequencingRule(sr);
			}

			PR br = getBatchSequencingRule(i);
			if (br != null) {
				br = br.clone();
				br.setOwner(m);
				m.setBatchSequencingRule(br);
			}

			BatchForming bf = getBatchForming(i);
			if (bf != null) {
				bf = bf.clone();
				bf.setOwner(m);
				m.setBatchForming(bf);
			}
		}

		// install shop listener
		if (shopListener != null)
			for (NotifierListener<SimComponent, SimComponentEvent> l : shopListener) {
				NotifierListener<SimComponent, SimComponentEvent> c = TypeUtil.cloneIfPossible(l);
				shop.addListener(c);
			}

		// install generic machine listener
		if (machineListener != null)
			for (NotifierListener<SimComponent, SimComponentEvent> l : machineListener) {
				shop.installMachineListener(l, true);
			}

		// install specific machine listener
		if (machListenerSpecific != null) {
			for (String machName : machListenerSpecific.keySet()) {
				WorkStation ws = shop.getWorkstationByName(machName);
				if (ws == null) {
					throw new RuntimeException(
							"Error installing machine listener: can't find a workstation named '" + machName + "'");
				}

				NotifierListener<SimComponent, SimComponentEvent>[] mls = machListenerSpecific.get(machName);
				for (NotifierListener<SimComponent, SimComponentEvent> ml : mls) {
					ml = TypeUtil.cloneIfPossible(ml);
					ws.addListener(ml);
				}
			}
		}
	}

	@Override
	protected void beforeRun() {
		super.beforeRun();
	}

	protected PR getSequencingRule(int i) {
		if (getSequencingRules() != null)
			return getSequencingRules()[i];
		else
			return getSequencingRule();
	}

	protected PR getBatchSequencingRule(int i) {
		if (getBatchSequencingRules() != null)
			return getBatchSequencingRules()[i];
		else
			return getBatchSequencingRule();
	}

	protected BatchForming getBatchForming(int i) {
		if (getBatchFormingRules() != null)
			return getBatchFormingRules()[i];
		else
			return getBatchForming();
	}

	@Override
	protected void done() {
		super.done();

		int wip = shop.jobsStarted - shop.jobsFinished;
		aborted = (getMaxJobsInSystem() > 0 && wip >= getMaxJobsInSystem()) ? 1 : 0;
	}

	@Override
	protected void finish() {
		super.finish();

		shop = null;
	}

	@Override
	public ShopExperiment clone() {
		if (resultMap != null) {
			throw new IllegalStateException("Can't clone an experiment that was already executed.");
		}
		ShopExperiment clone = (ShopExperiment) super.clone();

		if (sequencingRule != null)
			clone.sequencingRule = sequencingRule.clone();
		if (batchSequencingRule != null)
			clone.batchSequencingRule = batchSequencingRule.clone();
		if (batchForming != null)
			clone.batchForming = batchForming.clone();

		clone.sequencingRules = TypeUtil.deepCloneArrayIfPossible(sequencingRules);
		clone.batchSequencingRules = TypeUtil.deepCloneArrayIfPossible(batchSequencingRules);
		clone.batchFormingRules = TypeUtil.deepCloneArrayIfPossible(batchFormingRules);
		clone.shopListener = TypeUtil.deepCloneArrayIfPossible(shopListener);
		clone.machineListener = TypeUtil.deepCloneArrayIfPossible(machineListener);

		if (machListenerSpecific != null) {
			clone.machListenerSpecific = new HashMap<>();
			for (String name : machListenerSpecific.keySet()) {
				clone.machListenerSpecific.put(name, TypeUtil.cloneIfPossible(machListenerSpecific.get(name)));
			}
		}

		return clone;
	}

	//
	//
	// boring getters and setters for parameters below
	//
	//

	/**
	 * End simulation if WIP (work in process) reaches this value (&lt;=0: no limit;
	 * default is -1).
	 * 
	 * @param maxJobsInSystem The maximum number of concurrent jobs allowed in the
	 *                        system.
	 */
	public void setMaxJobsInSystem(int maxJobsInSystem) {
		this.maxJobsInSystem = maxJobsInSystem;
	}

	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	/**
	 * Enable/disable the lookahead mechanism of this shop. If enabled, dispatching
	 * rules can select jobs arriving in the near future (i.e., jobs already
	 * processed on an immediate predecessor machine).
	 * 
	 * @param enableLookAhead Whether or not to enable one-stop look ahead.
	 */
	public void setEnableLookAhead(boolean enableLookAhead) {
		this.enableLookAhead = enableLookAhead;
	}

	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	/**
	 * End simulation if a certain number of jobs was completed (&lt;=0 (default):
	 * no limit).
	 * 
	 * @param stopAfterNumJobs Set the number of jobs to complete before terminating
	 *                         the simulation.
	 */
	public void setStopAfterNumJobs(int stopAfterNumJobs) {
		this.stopAfterNumJobs = stopAfterNumJobs;
	}

	public int getStopAfterNumJobs() {
		return stopAfterNumJobs;
	}

	public PR getSequencingRule() {
		return sequencingRule;
	}

	/**
	 * Sets a certain dispatching rule to be used for sequencing jobs on all
	 * machines.
	 * 
	 * @see #setSequencingRules(PR[])
	 * @param sequencingRule The sequencing rule to use on all work stations.
	 */
	public void setSequencingRule(PR sequencingRule) {
		this.sequencingRule = sequencingRule;
	}

	public PR getBatchSequencingRule() {
		return batchSequencingRule;
	}

	/**
	 * Sets a certain dispatching rule to be used for sequencing batches on all
	 * batch machines.
	 * 
	 * @see #setBatchSequencingRules(PR[])
	 * @param batchSequencingRule The batch sequencing rule to use on all work
	 *                            stations.
	 */
	public void setBatchSequencingRule(PR batchSequencingRule) {
		this.batchSequencingRule = batchSequencingRule;
	}

	public BatchForming getBatchForming() {
		return batchForming;
	}

	/**
	 * Sets a batch forming mechanism to be used on all machines.
	 * 
	 * @see #setBatchFormingRules(BatchForming[])
	 * @param batchForming The batch forming rule to use on all machines
	 */
	public void setBatchForming(BatchForming batchForming) {
		this.batchForming = batchForming;
	}

	/**
	 * Sets a sequencing rule for specific machines. To use it
	 * {@code sequencingRules} has to contain an entry for each machine
	 * (workstation) in the model.
	 * 
	 * @see #setSequencingRule(PR)
	 * @param sequencingRules An array of sequencing rule, containing one {@link PR}
	 *                        per work station.
	 */
	public void setSequencingRules(PR[] sequencingRules) {
		this.sequencingRules = sequencingRules;
	}

	public PR[] getSequencingRules() {
		return sequencingRules;
	}

	/**
	 * Sets a batch sequencing rule for specific machines. To use it
	 * {@code batchSequencingRules} has to contain an entry for each machine
	 * (workstation) in the model.
	 * 
	 * @see #setBatchSequencingRule(PR)
	 * @param batchSequencingRules An array of batch sequencing rules, one for each
	 *                             workstation.
	 */
	public void setBatchSequencingRules(PR[] batchSequencingRules) {
		this.batchSequencingRules = batchSequencingRules;
	}

	public PR[] getBatchSequencingRules() {
		return batchSequencingRules;
	}

	/**
	 * Sets a batch forming mechanism for specific machines. To use it
	 * {@code batchFormingRules} has to contain an entry for each machine
	 * (workstation) in the model.
	 * 
	 * @see #setBatchForming(BatchForming)
	 * @param batchFormingRules An array of batch forming rules, one for each
	 *                          workstation.
	 */
	public void setBatchFormingRules(BatchForming[] batchFormingRules) {
		this.batchFormingRules = batchFormingRules;
	}

	public BatchForming[] getBatchFormingRules() {
		return batchFormingRules;
	}

	/**
	 * Gets the complete list of {@link Shop} listeners.
	 * 
	 * @return The array of shop listeners; can be null.
	 */
	public NotifierListener<SimComponent, SimComponentEvent>[] getShopListener() {
		return shopListener;
	}

	/**
	 * Sets a list of {@link Shop} listeners to be installed on the shop.
	 * 
	 * @param shopListener The listeners to install during experiment execution.
	 */
	public void setShopListener(NotifierListener<SimComponent, SimComponentEvent>[] shopListener) {
		this.shopListener = shopListener;
	}

	/**
	 * Adds a shop listener to be installed on the experiment's {@link Shop}.
	 * 
	 * @param l The listener to install during experiment execution.
	 */
	public void addShopListener(NotifierListener<SimComponent, SimComponentEvent> l) {
		if (shopListener == null) {
			@SuppressWarnings("unchecked")
			final NotifierListener<SimComponent, SimComponentEvent>[] resArray = new NotifierListener[] { l };
			shopListener = resArray;
		} else {
			ArrayList<NotifierListener<SimComponent, SimComponentEvent>> list = new ArrayList<>(
					Arrays.asList(shopListener));
			list.add(l);

			@SuppressWarnings("unchecked")
			final NotifierListener<SimComponent, SimComponentEvent>[] resArray = new NotifierListener[list.size()];
			shopListener = list.toArray(resArray);
		}
	}

	/**
	 * Gets the complete list of {@link WorkStation} listeners.
	 * 
	 * @return The array of workstation listeners. Can be null.
	 */
	public NotifierListener<SimComponent, SimComponentEvent>[] getMachineListener() {
		return machineListener;
	}

	/**
	 * Sets a list of {@link WorkStation} listeners to be installed on each
	 * {@link WorkStation}.
	 * 
	 * @param machineListener The listeners to install during experiment execution.
	 */
	public void setMachineListener(NotifierListener<SimComponent, SimComponentEvent>[] machineListener) {
		this.machineListener = machineListener;
	}

	/**
	 * Adds a WorkStation listener to be installed on each {@link WorkStation} in
	 * the experiment.
	 * 
	 * @param l The listener to install during experiment execution.
	 */
	public void addMachineListener(NotifierListener<SimComponent, SimComponentEvent> l) {
		if (this.machineListener == null) {
			@SuppressWarnings("unchecked")
			final NotifierListener<SimComponent, SimComponentEvent>[] resArray = new NotifierListener[] { l };
			this.machineListener = resArray;
		} else {
			ArrayList<NotifierListener<SimComponent, SimComponentEvent>> list = new ArrayList<>(
					Arrays.asList(this.machineListener));
			list.add(l);
			@SuppressWarnings("unchecked")
			final NotifierListener<SimComponent, SimComponentEvent>[] resArray = new NotifierListener[list.size()];
			machineListener = list.toArray(resArray);
		}
	}

	/**
	 * Adds a WorkStation-listener to be installed on a certain {@link WorkStation}.
	 * 
	 * @param name The workstation's name.
	 * @param l    The listener to install during experiment execution.
	 */
	public void addMachineListener(String name, NotifierListener<SimComponent, SimComponentEvent> l) {
		if (machListenerSpecific == null)
			machListenerSpecific = new HashMap<>();

		// create new array using an intermediary list
		NotifierListener<SimComponent, SimComponentEvent>[] listeners = machListenerSpecific.get(name);
		ArrayList<NotifierListener<SimComponent, SimComponentEvent>> list = new ArrayList<>();
		if (listeners != null) {
			list.addAll(Arrays.asList(listeners));
		}
		list.add(l);

		@SuppressWarnings("unchecked")
		final NotifierListener<SimComponent, SimComponentEvent>[] resArray = new NotifierListener[list.size()];
		machListenerSpecific.put(name, list.toArray(resArray));
	}

	/**
	 * Returns an array of all listeners registered for a given machine registered
	 * before using {@code #addMachineListener(String, NotifierListener)}.
	 * 
	 * @param name The workstation's name.
	 * @return An array of all listeners for the given machine name.
	 */
	public NotifierListener<SimComponent, SimComponentEvent>[] getMachineListenerSpecific(String name) {
		if (machListenerSpecific == null) {
			@SuppressWarnings("unchecked")
			final NotifierListener<SimComponent, SimComponentEvent>[] resArray = new NotifierListener[0];
			return resArray;
		} else {
			return machListenerSpecific.get(name);
		}
	}

}
