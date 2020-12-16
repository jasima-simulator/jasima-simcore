package jasima.core.simulation.util;

import jasima.core.simulation.SimComponentContainer;
import jasima.core.simulation.Simulation;

public interface SimCtx {

	public Simulation getSim();
	public SimComponentContainer getRootComponent();
	
}
