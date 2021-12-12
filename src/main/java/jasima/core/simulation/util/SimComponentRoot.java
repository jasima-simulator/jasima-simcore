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
	public void simStart() {
		super.simStart();
		trace("sim_start");
	}

	@Override
	public void simEnd() {
		super.simEnd();
		trace("sim_end");
	}
}