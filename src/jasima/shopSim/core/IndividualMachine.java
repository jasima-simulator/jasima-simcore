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

import jasima.core.random.RandomFactory;
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

	public final WorkStation workStation; // the workstation this machine
											// belongs to
	public final int idx; // index in workStation.machDat

	public double relDate;
	public int initialSetup;
	public String name = null;

	public DblStream timeBetweenFailures;
	public DblStream timeToRepair;

	public MachineState state;
	public double procStarted;
	public double procFinished;
	public int setupState;
	public PrioRuleTarget curJob;

	public IndividualMachine(WorkStation workStation, int idx) {
		super();
		this.workStation = workStation;
		this.idx = idx;

		timeBetweenFailures = null;
		timeToRepair = null;
		state = MachineState.DOWN;
		initialSetup = WorkStation.DEF_SETUP;
		relDate = 0.0;
	}

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
			assert workStation.currMachine == null;
			workStation.currMachine = IndividualMachine.this;
			IndividualMachine.this.activate();
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
			assert workStation.currMachine == null;
			workStation.currMachine = IndividualMachine.this;
			IndividualMachine.this.takeDown();
			workStation.currMachine = null;
		}

		@Override
		public boolean isAppEvent() {
			return false;
		}
	};

	/** Activation from DOWN state. */
	protected void activate() {
		assert state == MachineState.DOWN;
		assert curJob == null;

		state = MachineState.IDLE;
		procFinished = -1.0d;
		procStarted = -1.0d;

		workStation.activated(this);

		// schedule next down time
		if (timeBetweenFailures != null) {
			JobShop shop = workStation.shop();

			double nextFailure = shop.simTime() + timeBetweenFailures.nextDbl();
			takeDownEvent.setTime(nextFailure);
			shop.schedule(takeDownEvent);
		}
	}

	/** Machine going down. */
	protected void takeDown() {
		final JobShop shop = workStation.shop();

		if (state != MachineState.IDLE) {
			// don't interrupt ongoing operation / down time, postpone takeDown
			// instead
			assert procFinished > shop.simTime();
			assert curJob != null || state == MachineState.DOWN;
			takeDownEvent.setTime(procFinished);
			shop.schedule(takeDownEvent);
		} else {
			assert state == MachineState.IDLE;

			double whenReactivated = shop.simTime() + timeToRepair.nextDbl();

			procStarted = shop.simTime();
			procFinished = whenReactivated;
			state = MachineState.DOWN;
			curJob = null;

			workStation.takenDown(this);

			// schedule reactivation
			assert activateEvent.getTime() <= shop.simTime();
			activateEvent.setTime(whenReactivated);
			shop.schedule(activateEvent);
		}
	}

	protected void init() {
		setupState = initialSetup;
		procFinished = relDate;
		procStarted = 0.0;
		state = MachineState.DOWN;
		activateEvent.setTime(relDate);
		workStation.shop.schedule(activateEvent);

		RandomFactory fact = workStation.shop.getRndStreamFactory();
		if (timeBetweenFailures != null
				&& timeBetweenFailures.getRndGen() == null) {
			fact.initNumberStream(timeBetweenFailures, toString() + ".timeBetweenFailures");
			timeBetweenFailures.init();
		}
		if (timeToRepair != null && timeToRepair.getRndGen() == null) {
			fact.initNumberStream(timeToRepair, toString() + ".timeToRepair");
			timeToRepair.init();
		}
	}

	@Override
	public String toString() {
		if (name == null)
			name = workStation.getName()
					+ (workStation.numInGroup() > 1 ? "." + idx : "");
		return name;
	}

}