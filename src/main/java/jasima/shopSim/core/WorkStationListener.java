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
package jasima.shopSim.core;

import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_ACTIVATED;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_DEACTIVATED;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_JOB_ARRIVAL;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_JOB_COMPLETED;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_JOB_SELECTED;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponent.SimComponentEvent;
import jasima.core.simulation.SimComponentLifecycleListener;

/**
 * Possible base class for workstation listeners. Delegates all events to
 * seperate methods. Additional events can be processed by overriding
 * {@link #handleOther(SimComponent, Object)}.
 * 
 * @author Torsten Hildebrandt
 */
public interface WorkStationListener extends SimComponentLifecycleListener {

	default void inform(SimComponent o, SimComponentEvent event) {
		WorkStation m = (WorkStation) o;

		if (event == WS_JOB_ARRIVAL) {
			arrival(m, m.justArrived);
		} else if (event == WS_JOB_SELECTED) {
			operationStarted(m, m.justStarted, m.oldSetupState, m.newSetupState, m.setupTime);
		} else if (event == WS_JOB_COMPLETED) {
			operationCompleted(m, m.justCompleted);
		} else if (event == WS_ACTIVATED) {
			activated(m, m.currMachine);
		} else if (event == WS_DEACTIVATED) {
			deactivated(m, m.currMachine);
		} else {
			SimComponentLifecycleListener.super.inform(o, event);
		}
	}

	default void operationCompleted(WorkStation m, PrioRuleTarget justCompleted) {
	}

	default void operationStarted(WorkStation m, PrioRuleTarget justStarted, int oldSetupState, int newSetupState,
			double setupTime) {
	}

	default void arrival(WorkStation m, Job justArrived) {
	}

	default void activated(WorkStation m, IndividualMachine justActivated) {
	}

	default void deactivated(WorkStation m, IndividualMachine justDeactivated) {
	}

}
