/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
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
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.core;

import jasima.core.random.continuous.DblStream;
import jasima.core.simulation.Event;

/**
 * This class represents a single machine, which is part of a
 * {@link WorkStation}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class IndividualMachine {

	public enum MachineState {
		DOWN, IDLE, WORKING
	}

	public final WorkStation workStation;
	public final int idx; // index in workStation.machDat

	/**
	 * @param workStation
	 */
	IndividualMachine(WorkStation workStation, int idx) {
		super();
		this.workStation = workStation;
		this.idx = idx;

		timeBetweenFailures = null;
		timeToRepair = null;
		state = MachineState.DOWN;
		initialSetup = WorkStation.DEF_SETUP;
		relDate = 0.0;
	}

	public double relDate;
	public int initialSetup;

	public DblStream timeBetweenFailures;
	public DblStream timeToRepair;

	public MachineState state;
	public double procFinished;
	public int setupState;
	public PrioRuleTarget curJob;

	// called whenever an operation is finished
	Event onDepart = new Event(0.0d, WorkStation.DEPART_PRIO) {
		@Override
		public void handle() {
			workStation.currMachine = IndividualMachine.this;
			workStation.depart();
			workStation.currMachine = null;
		}
	};

	Event activateEvent = new Event(0.0d, WorkStation.ACTIVATE_PRIO) {
		@Override
		public void handle() {
			workStation.currMachine = IndividualMachine.this;
			workStation.activate();
			workStation.currMachine = null;
		}

		@Override
		public boolean isAppEvent() {
			return false;
		}
	};

	Event takeDownEvent = new Event(0.0d, WorkStation.TAKE_DOWN_PRIO) {
		@Override
		public void handle() {
			workStation.currMachine = IndividualMachine.this;
			workStation.takeDown();
			workStation.currMachine = null;
		}

		@Override
		public boolean isAppEvent() {
			return false;
		}
	};

	private String name = null;

	@Override
	public String toString() {
		if (name == null)
			name = workStation.getName()
					+ (workStation.numInGroup > 1 ? "." + idx : "");
		return name;
	}

}