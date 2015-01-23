/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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

import jasima.core.experiment.Experiment;
import jasima.core.random.RandomFactory;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.simulation.Simulation.SimPrintEvent;
import jasima.core.util.Util;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.WorkStation.WorkStationEvent;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.util.ShopListenerBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Base class for shop experiments. This class wraps a {@link JobShop}. Derived
 * classes will typically populate the shop with machines and JobSources and add
 * functionality to collect some statistics and produce appropriate experiment
 * results.
 * 
 * @author Torsten Hildebrandt, 2010-03-12
 * @version 
 *          "$Id$"
 */
public abstract class JobShopExperiment extends Experiment {

	private static final long serialVersionUID = 2660935009898060395L;

	// experiment parameters
	private double simulationLength = 0.0d;
	private int maxJobsInSystem = 0;
	private int stopAfterNumJobs = 0;
	private boolean enableLookAhead = false;

	private PR sequencingRule;
	private PR batchSequencingRule;
	private BatchForming batchForming;

	private PR[] sequencingRules;
	private PR[] batchSequencingRules;
	private BatchForming[] batchFormingRules;

	private NotifierListener<Simulation, SimEvent>[] shopListener;
	private NotifierListener<WorkStation, WorkStationEvent>[] machineListener;
	private HashMap<String, NotifierListener<WorkStation, WorkStationEvent>[]> machListenerSpecific;

	// fields used during experiment execution
	public JobShop shop;

	@Override
	public void init() {
		super.init();

		createShop();
		configureShop();
		postConfigShop();
	}

	protected void configureShop() {
		shop.setMaxJobsInSystem(getMaxJobsInSystem());
		shop.setStopAfterNumJobs(getStopAfterNumJobs());
		shop.setSimulationLength(getSimulationLength());
		shop.setEnableLookAhead(isEnableLookAhead());
	}

	protected void postConfigShop() {
		// set dispatching rule of machines
		for (int i = 0; i < shop.machines.length; i++) {
			WorkStation m = shop.machines[i];

			PR sr = getSequencingRule(i);
			if (sr != null) {
				sr = sr.silentClone();
				sr.setOwner(m);
				m.queue.setSequencingRule(sr);
			}

			PR br = getBatchSequencingRule(i);
			if (br != null) {
				br = br.silentClone();
				br.setOwner(m);
				m.setBatchSequencingRule(br);
			}

			BatchForming bf = getBatchForming(i);
			if (bf != null) {
				bf = bf.silentClone();
				bf.setOwner(m);
				m.setBatchForming(bf);
			}
		}

		// install shop listener
		if (shopListener != null)
			for (NotifierListener<?, ?> l : shopListener) {
				l = shop.installSimulationListener(
						(NotifierListener<Simulation, SimEvent>) l, true);
			}

		// install generic machine listener
		if (machineListener != null)
			for (NotifierListener<?, ?> l : machineListener) {
				shop.installMachineListener(
						(NotifierListener<WorkStation, WorkStationEvent>) l,
						true);
			}

		// install specific machine listener
		if (machListenerSpecific != null) {
			for (String machName : machListenerSpecific.keySet()) {
				WorkStation ws = shop.getWorkstationByName(machName);
				if (ws == null) {
					throw new RuntimeException(
							"Error installing machine listener: can't find a workstation named '"
									+ machName + "'");
				}
				NotifierListener<WorkStation, WorkStationEvent>[] mls = machListenerSpecific
						.get(machName);
				for (NotifierListener<WorkStation, WorkStationEvent> ml : mls) {
					ml = Util.cloneIfPossible(ml);
					ws.addNotifierListener(ml);
				}
			}
		}

		// forward simulation print events to experiment print events
		shop.addNotifierListener(new ShopListenerBase() {

			@Override
			protected void print(Simulation sim, SimPrintEvent event) {
				// translate category
				ExpMsgCategory cat = ExpMsgCategory.values()[event.category
						.ordinal()];
				assert cat.toString().equals(event.category.toString());

				JobShopExperiment.this.print(cat, "sim_message\t%f\t%s",
						sim.simTime(), event);
			}
		});
	}

	@Override
	protected void beforeRun() {
		shop.init();
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

	protected void createShop() {
		shop = doCreateShop();

		RandomFactory randomFactory = RandomFactory.newInstance(shop);
		randomFactory.setSeed(getInitialSeed());
		shop.setRndStreamFactory(randomFactory);
	}

	/** Factory method to create/initialize a shop object. */
	protected JobShop doCreateShop() {
		return new JobShop();
	}

	@Override
	protected void performRun() {
		shop.run();
	}

	@Override
	protected void done() {
		super.done();

		shop.done();

		int wip = shop.jobsStarted - shop.jobsFinished;
		aborted = (getMaxJobsInSystem() > 0 && wip >= getMaxJobsInSystem()) ? 1
				: 0;
	}

	@Override
	protected void produceResults() {
		super.produceResults();
		shop.produceResults(resultMap);
	}

	@Override
	protected void finish() {
		super.finish();

		shop = null;
	}

	@Override
	public JobShopExperiment clone() throws CloneNotSupportedException {
		if (resultMap != null)
			throw new CloneNotSupportedException(
					"Can't clone an experiment that was already executed.");

		JobShopExperiment clone = (JobShopExperiment) super.clone();

		if (sequencingRule != null)
			clone.sequencingRule = sequencingRule.clone();
		if (batchSequencingRule != null)
			clone.batchSequencingRule = batchSequencingRule.clone();
		if (batchForming != null)
			clone.batchForming = batchForming.clone();

		clone.sequencingRules = Util.deepCloneArrayIfPossible(sequencingRules);
		clone.batchSequencingRules = Util
				.deepCloneArrayIfPossible(batchSequencingRules);
		clone.batchFormingRules = Util
				.deepCloneArrayIfPossible(batchFormingRules);
		clone.shopListener = Util.deepCloneArrayIfPossible(shopListener);
		clone.machineListener = Util.deepCloneArrayIfPossible(machineListener);

		if (machListenerSpecific != null) {
			clone.machListenerSpecific = new HashMap<String, NotifierListener<WorkStation, WorkStationEvent>[]>();
			for (String name : machListenerSpecific.keySet()) {
				clone.machListenerSpecific.put(name,
						Util.cloneIfPossible(machListenerSpecific.get(name)));
			}
		}

		return clone;
	}

	//
	//
	// boring getters and setters for parameters below
	//
	//

	/** Sets the maximum simulation time. A value of 0.0 means no such limit. */
	public void setSimulationLength(double simulationLength) {
		this.simulationLength = simulationLength;
	}

	public double getSimulationLength() {
		return simulationLength;
	}

	/**
	 * End simulation if WIP (work in process) reaches this value (<=0: no
	 * limit; default is -1).
	 */
	public void setMaxJobsInSystem(int maxJobsInSystem) {
		this.maxJobsInSystem = maxJobsInSystem;
	}

	public int getMaxJobsInSystem() {
		return maxJobsInSystem;
	}

	/**
	 * Enable/disable the lookahead mechanism of this shop. If enabled,
	 * dispatching rules can select jobs arriving in the near future (i.e., jobs
	 * already processed on an immediate predecessor machine).
	 */
	public void setEnableLookAhead(boolean enableLookAhead) {
		this.enableLookAhead = enableLookAhead;
	}

	public boolean isEnableLookAhead() {
		return enableLookAhead;
	}

	/**
	 * End simulation if a certain number of jobs was completed (<=0 (default):
	 * no limit).
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
	 */
	public void setBatchFormingRules(BatchForming[] batchFormingRules) {
		this.batchFormingRules = batchFormingRules;
	}

	public BatchForming[] getBatchFormingRules() {
		return batchFormingRules;
	}

	/**
	 * Gets the complete list of {@link JobShop} listeners.
	 * 
	 * @return The array of shop listeners; can be null.
	 */
	public NotifierListener<Simulation, SimEvent>[] getShopListener() {
		return shopListener;
	}

	/**
	 * Sets a list of {@link JobShop} listeners to be installed on the shop.
	 * 
	 * @param shopListener
	 *            The listeners to install during experiment execution.
	 */
	public void setShopListener(
			NotifierListener<Simulation, SimEvent>[] shopListener) {
		this.shopListener = shopListener;
	}

	/**
	 * Adds a shop listener to be installed on the experiment's {@link JobShop}.
	 * 
	 * @param l
	 *            The listener to install during experiment execution.
	 */
	public void addShopListener(NotifierListener<Simulation, SimEvent> l) {
		if (this.shopListener == null) {
			this.shopListener = new NotifierListener[] { l };
		} else {
			ArrayList<NotifierListener<Simulation, SimEvent>> list = new ArrayList<NotifierListener<Simulation, SimEvent>>(
					Arrays.asList(this.shopListener));
			list.add(l);

			shopListener = list.toArray(new NotifierListener[list.size()]);
		}
	}

	/**
	 * Gets the complete list of {@link WorkStation} listeners.
	 * 
	 * @return The array of workstation listeners. Can be null.
	 */
	public NotifierListener<WorkStation, WorkStationEvent>[] getMachineListener() {
		return machineListener;
	}

	/**
	 * Sets a list of {@link WorkStation} listeners to be installed on each
	 * {@link WorkStation}.
	 * 
	 * @param machineListener
	 *            The listeners to install during experiment execution.
	 */
	public void setMachineListener(
			NotifierListener<WorkStation, WorkStationEvent>[] machineListener) {
		this.machineListener = machineListener;
	}

	/**
	 * Adds a WorkStation listener to be installed on each {@link WorkStation}
	 * in the experiment.
	 * 
	 * @param l
	 *            The listener to install during experiment execution.
	 */
	public void addMachineListener(
			NotifierListener<WorkStation, WorkStationEvent> l) {
		if (this.machineListener == null) {
			this.machineListener = new NotifierListener[] { l };
		} else {
			ArrayList<NotifierListener<WorkStation, WorkStationEvent>> list = new ArrayList<NotifierListener<WorkStation, WorkStationEvent>>(
					Arrays.asList(this.machineListener));
			list.add(l);

			machineListener = list.toArray(new NotifierListener[list.size()]);
		}
	}

	/**
	 * Adds a WorkStation-listener to be installed on a certain
	 * {@link WorkStation}.
	 * 
	 * @param name
	 *            The workstation's name.
	 * @param l
	 *            The listener to install during experiment execution.
	 */
	public void addMachineListener(String name,
			NotifierListener<WorkStation, WorkStationEvent> l) {
		if (machListenerSpecific == null)
			machListenerSpecific = new HashMap<String, NotifierListener<WorkStation, WorkStationEvent>[]>();

		// create new array using an intermediary list
		NotifierListener<WorkStation, WorkStationEvent>[] listeners = machListenerSpecific
				.get(name);
		if (listeners == null)
			listeners = new NotifierListener[0];
		ArrayList<NotifierListener<WorkStation, WorkStationEvent>> list = new ArrayList<NotifierListener<WorkStation, WorkStationEvent>>(
				Arrays.asList(listeners));
		list.add(l);

		machListenerSpecific.put(name,
				list.toArray(new NotifierListener[list.size()]));
	}

	/**
	 * Returns an array of all listeners registered for a given machine
	 * registered before using
	 * {@code #addMachineListener(String, NotifierListener)}.
	 * 
	 * @param name
	 *            The workstation's name.
	 * @return An array of all listeners for the given machine name.
	 */
	public NotifierListener<WorkStation, WorkStationEvent>[] getMachineListenerSpecific(
			String name) {
		if (machListenerSpecific == null) {
			return new NotifierListener[0];
		} else {
			return machListenerSpecific.get(name);
		}
	}

}
