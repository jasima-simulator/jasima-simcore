/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.util;

import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.core.WorkStation.WorkStationEvent;

import java.util.Map;

/**
 * Possible base class for workstation listeners. Delegates all events to
 * seperate methods. Additional events can be processed by overriding
 * {@link #handleOther(WorkStation, WorkStation.WorkStationEvent)}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public abstract class WorkStationListenerBase implements
		NotifierListener<WorkStation, WorkStationEvent>, Cloneable {

	@Override
	public final void update(WorkStation m, WorkStationEvent event) {
		if (event == WorkStation.WS_JOB_ARRIVAL) {
			arrival(m, m.justArrived);
		} else if (event == WorkStation.WS_JOB_SELECTED) {
			operationStarted(m, m.justStarted, m.oldSetupState,
					m.newSetupState, m.setupTime);
		} else if (event == WorkStation.WS_JOB_COMPLETED) {
			operationCompleted(m, m.justCompleted);
		} else if (event == WorkStation.WS_ACTIVATED) {
			activated(m, m.currMachine);
		} else if (event == WorkStation.WS_DEACTIVATED) {
			deactivated(m, m.currMachine);
		} else if (event == WorkStation.WS_DONE) {
			done(m);
		} else if (event == WorkStation.WS_COLLECT_RESULTS) {
			produceResults(m, m.resultMap);
		} else if (event == WorkStation.WS_INIT) {
			init(m);
		} else {
			handleOther(m, event);
		}
	}

	protected void handleOther(WorkStation m, WorkStationEvent event) {
	}

	protected void init(WorkStation m) {
	}

	protected void produceResults(WorkStation m, Map<String, Object> resultMap) {
	}

	protected void done(WorkStation m) {
	}

	protected void operationCompleted(WorkStation m,
			PrioRuleTarget justCompleted) {
	}

	protected void operationStarted(WorkStation m, PrioRuleTarget justStarted,
			int oldSetupState, int newSetupState, double setupTime) {
	}

	protected void arrival(WorkStation m, Job justArrived) {
	}

	protected void activated(WorkStation m, IndividualMachine justActivated) {
	}

	protected void deactivated(WorkStation m, IndividualMachine justDeactivated) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
