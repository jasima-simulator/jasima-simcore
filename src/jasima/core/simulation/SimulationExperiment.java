package jasima.core.simulation;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.function.Consumer;

import jasima.core.experiment.Experiment;
import jasima.core.simulation.Simulation.SimPrintMessage;
import jasima.core.util.MsgCategory;

public class SimulationExperiment extends Experiment {

	private static final long serialVersionUID = 5208352155973550329L;

	// parameters

	private double simulationLength = Double.NaN;
	private double initialSimTime = 0.0d;
	private double statsResetTime = 0.0d;
	private long simTimeToMillisFactor = 60 * 1000; // simulation time in minutes
	private Instant simTimeStartInstant = null; // beginning of current year will be used if not set explicitly
	private ArrayList<Consumer<Simulation>> initActions = null;
	private SimComponent rootComponent = null; 

	// fields used during run

	protected transient Simulation sim;

	public SimulationExperiment() {
		super();
	}

	@Override
	protected void init() {
		super.init();

		sim = createSim();

		initSim();

		createSimComponents();

		// call initActions
		if (initActions != null) {
			initActions.forEach(a -> a.accept(sim));
		}
	}

	protected void initSim() {
		sim.setInitialSimTime(getInitialSimTime());
		sim.setPrintLevel(getLogLevel());
		sim.getRndStreamFactory().setSeed(getInitialSeed());

		if (getSimTimeStartInstant() != null) {
			sim.setSimTimeStartInstant(getSimTimeStartInstant());
		} else {
			// nothing set, use beginning of current year
			LocalDate yearBeg = LocalDate.of(Year.now(Clock.systemUTC()).getValue(), 1, 1);
			sim.setSimTimeStartInstant(yearBeg.atStartOfDay(ZoneOffset.UTC).toInstant());
		}
		sim.setSimTimeToMillisFactor(getSimTimeToMillisFactor());
		
		if (getRootComponent()!=null) {
			sim.addComponent(getRootComponent());
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
		if (getSimulationLength() >= 0.0) {
			sim.setSimulationLength(getSimulationLength());
		}

		sim.init();
	}

	@Override
	protected void performRun() {
		sim.run();
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
	 * Returns the simulation object created in {@code init()}.
	 */
	public Simulation sim() {
		return sim;
	}

	/**
	 * Returns the current simulation time. This is the same as calling
	 * {@code sim().simTime()} directly.
	 */
	protected double simTime() {
		return sim.simTime();
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

	public SimComponent getRootComponent() {
		return rootComponent;
	}

	public void setRootComponent(SimComponent rootComponent) {
		this.rootComponent = rootComponent;
	}

	@Override
	public SimulationExperiment clone() throws CloneNotSupportedException {
		SimulationExperiment c = (SimulationExperiment) super.clone();

		if (initActions != null) {
			// shallow clone
			c.initActions = new ArrayList<>(initActions);
		}
		
		if (rootComponent!=null) {
			// attempts a deep clone of rootComponent
			c.rootComponent = rootComponent.clone();
		}

		return c;
	}

}
