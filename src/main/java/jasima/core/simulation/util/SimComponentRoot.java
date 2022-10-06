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