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

import java.util.Collections;
import java.util.List;

import jasima.core.simulation.SimEvent;

/**
 * This class represents a single machine, which is part of a
 * {@link WorkStation}.
 * 
 * @author Torsten Hildebrandt
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

	@SuppressWarnings("unchecked")
	public List<? extends DowntimeSource> downsources = Collections.EMPTY_LIST;

	public MachineState state;
	public DowntimeSource downReason;
	public double procStarted;
	public double procFinished;
	public int setupState;
	public PrioRuleTarget curJob;

	public IndividualMachine(WorkStation workStation, int idx) {
		super();
		this.workStation = workStation;
		this.idx = idx;

		state = MachineState.DOWN;
		initialSetup = WorkStation.DEF_SETUP;
		relDate = 0.0;
	}

	// called whenever an operation is finished
	SimEvent onDepart = new SimEvent(0.0d, WorkStation.DEPART_PRIO) {
		@Override
		public void handle() {
			workStation.currMachine = IndividualMachine.this;
			workStation.depart();
			workStation.currMachine = null;
		}
	};

	/** Activation from DOWN state. */
	public void activate() {
		if (state != MachineState.DOWN)
			throw new IllegalStateException("Only a machine in state DOWN can be activated .");
		assert curJob == null;

		state = MachineState.IDLE;
		procFinished = -1.0d;
		procStarted = -1.0d;

		workStation.activated(this);

		downReason = null;
	}

	/**
	 * Machine going down for a certain amount of time. If this machine is
	 * already down or currently processing, this operation is finished before
	 * the new downtime can become active.
	 * 
	 * @param downReason
	 *            The {@link DowntimeSource} causing the shutdown.
	 */
	public void takeDown(final DowntimeSource downReason) {
		final Shop shop = workStation.shop();

		if (state != MachineState.IDLE) {
			assert procFinished >= shop.simTime();
			assert curJob != null || state == MachineState.DOWN;

			// don't interrupt ongoing operation/downtime, postpone takeDown
			// instead
			shop.getSim().scheduleAt(procFinished, WorkStation.TAKE_DOWN_PRIO, () -> {
				assert workStation.currMachine == null;
				workStation.currMachine = IndividualMachine.this;
				takeDown(downReason);
				workStation.currMachine = null;
			});
		} else {
			assert state == MachineState.IDLE;

			procStarted = shop.simTime();
			procFinished = shop.simTime();
			state = MachineState.DOWN;
			this.downReason = downReason;
			curJob = null;

			workStation.takenDown(this);
		}
	}

	protected void init() {
		setupState = initialSetup;
		procFinished = relDate;
		procStarted = 0.0;
		state = MachineState.DOWN;

		// schedule initial activation
		workStation.getSim().scheduleAt(relDate, WorkStation.ACTIVATE_PRIO, () -> {
			assert workStation.currMachine == null;
			workStation.currMachine = IndividualMachine.this;
			IndividualMachine.this.activate();
			workStation.currMachine = null;
		});

		// init downsources
		for (DowntimeSource ds : downsources) {
			ds.init();
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		if (name == null)
			return workStation.getName() + (workStation.numInGroup() > 1 ? "." + idx : "");

		return name;
	}

}