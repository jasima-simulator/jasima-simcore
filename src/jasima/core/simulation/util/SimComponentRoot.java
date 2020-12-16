package jasima.core.simulation.util;

import jasima.core.simulation.SimComponentContainerBase;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.SimulationExperiment;

/**
 * Used internally as root component for {@link Simulation} and
 * {@link SimulationExperiment}.
 */
public class SimComponentRoot extends SimComponentContainerBase {
	public SimComponentRoot() {
		super();
		setNameInternal("");
	}

	@Override
	public void beforeRun() {
		super.beforeRun();
		trace("sim_start");
	}

	@Override
	public void afterRun() {
		super.afterRun();
		trace("sim_end");
	}
}