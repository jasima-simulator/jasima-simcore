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
package jasima.core.simulation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jasima.core.experiment.Experiment;
import jasima.core.simulation.util.SimOperations;
import jasima.core.util.MsgCategory;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimRunnable;

public class SimulationExperiment extends Experiment implements SimOperations {

	private static final long serialVersionUID = 5208352155973550329L;

	// parameters

	private double simulationLength = Double.NaN;
	private double initialSimTime = 0.0d;
	private double statsResetTime = 0.0d;
	private long simTimeToMillisFactor = 60 * 1000; // simulation time in minutes
	private Instant simTimeStartInstant = null; // beginning of current year will be used if not set explicitly
	private List<Consumer<Simulation>> initActions = null;
	private SimComponent modelRoot = null;
	private SimAction mainProcess = null;

	// fields used during run

	protected transient Simulation sim;

	/**
	 * Create a new SimulationExperiment.
	 */
	public SimulationExperiment() {
		super();
	}

	/**
	 * Creates a new SimulationExperiment with the given {@link SimAction} as its
	 * main process.
	 * 
	 * {@link #setMainProcess(SimAction)}
	 */
	public SimulationExperiment(SimAction mainProcess) {
		this();
		setMainProcess(mainProcess);
	}

	/**
	 * Creates a new SimulationExperiment with the given {@link SimRunnable} as its
	 * main process.
	 * 
	 * {@link #setMainProcess(SimAction)}
	 */
	public SimulationExperiment(SimRunnable mainProcess) {
		this();
		setMainProcess(SimProcessUtil.simAction(mainProcess));
	}

	/**
	 * Creates a new SimulationExperiment with the given SimComponent as its main
	 * component.
	 * 
	 * {@link #setModelRoot(SimComponent)}
	 */
	public SimulationExperiment(SimComponent modelRoot) {
		this();
		setModelRoot(modelRoot);
	}

	@Override
	protected void init() {
		super.init();

		sim = createSim();

		initSim();

		createSimComponents();

		// prepare simulation and all components for run
		sim.init();

		// call initActions
		if (initActions != null) {
			initActions.forEach(a -> a.accept(sim));
		}
	}

	protected void initSim() {
		sim.setInitialSimTime(getInitialSimTime());
		sim.setPrintLevel(getLogLevel());
		sim.getRndStreamFactory().setSeed(getInitialSeed());
		sim.setMainProcessActions(getMainProcess());

		if (getSimulationLength() >= 0.0) {
			sim.setSimulationLength(getSimulationLength());
		}

		if (getSimTimeStartInstant() != null) {
			sim.setSimTimeStartInstant(getSimTimeStartInstant());
		} else {
			// nothing set, use beginning of current year
			LocalDate yearBeg = LocalDate.of(Year.now(Clock.systemUTC()).getValue(), 1, 1);
			sim.setSimTimeStartInstant(yearBeg.atStartOfDay(ZoneOffset.UTC).toInstant());
		}
		sim.setSimTimeToMillisFactor(getSimTimeToMillisFactor());

		if (getModelRoot() != null) {
			sim.addComponent(getModelRoot());
		}

		// forward simulation print events to experiment print events
		sim.addPrintListener(this::print);
	}

	protected Simulation createSim() {
		return new Simulation();
	}

	protected void createSimComponents() {
	}

	protected void print(SimPrintMessage event) {
		// don't forward trace messages
		if (event.getCategory().ordinal() < MsgCategory.TRACE.ordinal()) {
			print(event.getCategory(), "sim_message\t%s", event);
		}
	}

	@Override
	protected void beforeRun() {
		sim.beforeRun();
	}

	@Override
	protected void performRun() {
		sim.run();
	}

	@Override
	protected void afterRun() {
		sim.afterRun();
	}

	@Override
	protected void done() {
		sim.done();
	}

	@Override
	protected void produceResults() {
		super.produceResults();

		sim.produceResults(resultMap);
	}

	/**
	 * Converts a given Java {@link Duration} (i.e., a time span) to the
	 * corresponding (relative) simulation time.
	 * 
	 * @param d The duration to be converted to simulation time.
	 * @return The amount of simulation time.
	 */
	public double toSimTime(Duration d) {
		double millis = d.toMillis();
		return millis / simTimeToMillisFactor;
	}

	/**
	 * Returns the simulation object created in {@code init()}. Raises an
	 * {@link IllegalStateException} if not simulation was set before.
	 */
	public Simulation getSim() {
		if (sim == null)
			throw new IllegalStateException("no simulation.");
		return sim;
	}

	@Override // inherited from Experiment as well as SimOperations/Simulation
	public void addResult(String name, Object value) {
		SimOperations.super.addResult(name, value); // delegate to simulation's implementation
	}

	@Override // inherited from Experiment as well as SimOperations/Simulation
	public void addResults(Map<String, Object> map) {
		SimOperations.super.addResults(map); // delegate to simulation's implementation
	}

	@Override // inherited from Experiment as well as SimOperations/Simulation
	public void addResults(Map<String, Object> map, String namePrefix) {
		SimOperations.super.addResults(map, namePrefix); // delegate to simulation's implementation
	}

	/**
	 * Sets the maximum simulation time. A value of 0.0 means no such limit.
	 * 
	 * @param simulationLength Stop simulation at this point in time.
	 */
	public void setSimulationLength(double simulationLength) {
		this.simulationLength = simulationLength;
	}

	public double getSimulationLength() {
		return simulationLength;
	}

	/**
	 * Sets the statistics reset time. When this time is reached then the
	 * {@code resetStats()} methods of all components will be called. Statistics
	 * reset methods will also be executed once before the simulation starts
	 * (independently from this setting).
	 * 
	 * @param statsResetTime The time when to call all statics reset methods.
	 */
	public void setStatsResetTime(double statsResetTime) {
		this.statsResetTime = statsResetTime;
	}

	public double getStatsResetTime() {
		return statsResetTime;
	}

	public double getInitialSimTime() {
		return initialSimTime;
	}

	/**
	 * Sets the starting time for the simulation clock.
	 */
	public void setInitialSimTime(double initialSimTime) {
		this.initialSimTime = initialSimTime;
	}

	/**
	 * Adds a {@code Consumer<Simulation>} that is called after creating the
	 * simulation components to perform additional initialization tasks.
	 */
	public void addInitAction(Consumer<Simulation> action) {
		if (initActions == null) {
			initActions = new ArrayList<>();
		}
		initActions.add(action);
	}

	public Instant getSimTimeStartInstant() {
		return simTimeStartInstant;
	}

	public void setSimTimeStartInstant(Instant simTimeStartInstant) {
		this.simTimeStartInstant = simTimeStartInstant;
	}

	public long getSimTimeToMillisFactor() {
		return simTimeToMillisFactor;
	}

	public void setSimTimeToMillisFactor(long simTimeToMillisFactor) {
		this.simTimeToMillisFactor = simTimeToMillisFactor;
	}

	/**
	 * Specifies the time unit of the (double-valued) simulation time. The default
	 * value is ChronoUnit.MINUTES.
	 *
	 * @see Simulation#setSimTimeToMillisFactor(long)
	 */
	public void setSimTimeToMillisFactor(TemporalUnit u) {
		setSimTimeToMillisFactor(Duration.of(1, u).toMillis());
	}

	public SimComponent getModelRoot() {
		return modelRoot;
	}

	public void setModelRoot(SimComponent rootComponent) {
		this.modelRoot = rootComponent;
	}

	public SimAction getMainProcess() {
		return mainProcess;
	}

	public void setMainProcess(SimAction mainProcess) {
		this.mainProcess = mainProcess;
	}

	@Override
	public SimulationExperiment clone() {
		SimulationExperiment c = (SimulationExperiment) super.clone();

		if (initActions != null) {
			// shallow clone
			c.initActions = new ArrayList<>(initActions);
		}

		if (modelRoot != null) {
			// attempts a deep clone of rootComponent
			c.modelRoot = modelRoot.clone();
		}

		return c;
	}

}
