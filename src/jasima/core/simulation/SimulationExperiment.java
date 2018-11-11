package jasima.core.simulation;

import java.util.ArrayList;
import java.util.function.Consumer;

import jasima.core.experiment.Experiment;
import jasima.core.simulation.Simulation.SimPrintMessage;
import jasima.core.util.MsgCategory;

public class SimulationExperiment extends Experiment {

	private static final long serialVersionUID = 5208352155973550329L;

	// parameters

	private double simulationLength = Double.NaN;
	private double initalSimTime = 0.0d;
	private double statsResetTime = 0.0d;
	private ArrayList<Consumer<Simulation>> initActions;

	// fields used during run

	protected Simulation sim;

	public SimulationExperiment() {
		super();

		initActions = new ArrayList<>();
	}

	@Override
	protected void init() {
		super.init();

		initSim();

		// call initActions
		initActions.forEach(a -> a.accept(sim));
	}

	protected void initSim() {
		sim = createSim();

		sim.setInitialSimTime(getInitalSimTime());
		sim.setPrintLevel(getLogLevel());
		sim.getRndStreamFactory().setSeed(getInitialSeed());

		// forward simulation print events to experiment print events
		sim.addPrintListener(this::print);

		createSimComponents();
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
	 * Returns the current simulation time. This is the same as calling {@code sim().simTime()} directly.
	 */
	protected double simTime() {
		return sim.simTime();
	}

	/**
	 * Sets the maximum simulation time. A value of 0.0 means no such limit.
	 * 
	 * @param simulationLength
	 *            Stop simulation at this point in time.
	 */
	public void setSimulationLength(double simulationLength) {
		this.simulationLength = simulationLength;
	}

	public double getSimulationLength() {
		return simulationLength;
	}

	/**
	 * Sets the statistics reset time. When this time is reached then the {@code resetStats()} methods of all components will be called.
	 * Statistics reset methods will also be executed once before the simulation starts (independently from this setting).
	 * 
	 * @param statsResetTime
	 *            The time when to call all statics reset methods.
	 */
	public void setStatsResetTime(double statsResetTime) {
		this.statsResetTime = statsResetTime;
	}

	public double getStatsResetTime() {
		return statsResetTime;
	}

	public double getInitalSimTime() {
		return initalSimTime;
	}

	/**
	 * Sets the starting time for the simulation clock.
	 */
	public void setInitalSimTime(double initalSimTime) {
		this.initalSimTime = initalSimTime;
	}

	/**
	 * Adds a {@code Consumer<Simulation>} that is called after creating the simulation components to perform additional initialization
	 * tasks.
	 */
	public void addInitAction(Consumer<Simulation> action) {
		initActions.add(action);
	}

}
