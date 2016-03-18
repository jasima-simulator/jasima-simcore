/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.core;

import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_ACTIVATED;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_DEACTIVATED;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_JOB_ARRIVAL;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_JOB_COMPLETED;
import static jasima.shopSim.core.WorkStation.WorkStationMessage.WS_JOB_SELECTED;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentLifeCycleListener;

/**
 * Possible base class for workstation listeners. Delegates all events to
 * seperate methods. Additional events can be processed by overriding
 * {@link #handleOther(WorkStation, WorkStation.WorkStationMessage)}.
 * 
 * @author Torsten Hildebrandt
 */
public interface WorkStationListener extends SimComponentLifeCycleListener {

	default void inform(SimComponent o, Object event) {
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
			SimComponentLifeCycleListener.super.inform(o, event);
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
