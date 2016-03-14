package jasima.core.simulation;

import jasima.core.experiment.Experiment;

public class SimulationExperiment extends Experiment {

	private static final long serialVersionUID = 715276566550554922L;

	private double simulationLength = 0.0d;

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

		sim.getRndStreamFactory().setSeed(getInitialSeed());
		sim.setSimulationLength(getSimulationLength());

		createSimComponents();
	}

	protected Simulation createSim() {
		return new Simulation();
	}

	protected void createSimComponents() {
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

}
