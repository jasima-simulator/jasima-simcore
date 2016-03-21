package jasima.core.simulation;

import jasima.core.experiment.Experiment;
import jasima.core.simulation.Simulation.SimPrintMessage;
import jasima.core.util.MsgCategory;

public class SimulationExperiment extends Experiment {

	private static final long serialVersionUID = 5208352155973550329L;

	// parameters

	private double simulationLength = 0.0d;
	private double initalSimTime = 0.0d;

	// fields used during run

	protected Simulation sim;

	public SimulationExperiment() {
		super();
	}

	@Override
	protected void init() {
		super.init();

		initSim();
	}

	protected void initSim() {
		sim = createSim();

		sim.setSimulationLength(getSimulationLength());
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

	public double getInitalSimTime() {
		return initalSimTime;
	}

	/**
	 * Sets the starting time for the simulation clock.
	 */
	public void setInitalSimTime(double initalSimTime) {
		this.initalSimTime = initalSimTime;
	}

}
