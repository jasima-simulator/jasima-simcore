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
package jasima.shopSim.core;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblStream;
import jasima.core.simulation.Event;
import jasima.shopSim.util.WorkStationListenerBase;

/**
 * Abstraction of a downtime source. Each {@link IndividualMachine} can have
 * multiple downtime sources. Downtimes are specified using
 * {@link #timeBetweenFailures} and {@link #timeToRepair}.
 * {@link #timeBetweenFailures} specifies the time until the next takeDown-event
 * after the last activation.
 * <p>
 * A simple example: consider a machine that is going to be down every 24 hours
 * for a duration of 1 hour, i.e., it is available for processing for 23 hours.
 * Using {@code DowntimeSource}, this is modeled setting
 * {@code timeBetweenFailures} to 23 hours and using a {@code timeToRepair} of 1
 * hour.
 * 
 * @see MaintenanceSource
 * @author Torsten Hildebrandt, 2014-04-15
 * @version 
 *          "$Id$"
 */
public class DowntimeSource {

	private final IndividualMachine machine;
	private DblStream timeBetweenFailures;
	private DblStream timeToRepair;
	private String name;

	public DowntimeSource(IndividualMachine machine) {
		super();
		this.machine = machine;
	}

	public void init() {
		// initialize random streams
		RandomFactory fact = machine.workStation.shop.getRndStreamFactory();
		if (timeBetweenFailures != null
				&& timeBetweenFailures.getRndGen() == null) {
			fact.initNumberStream(timeBetweenFailures, toString()
					+ ".timeBetweenFailures");
			timeBetweenFailures.init();
		}
		if (timeToRepair != null && timeToRepair.getRndGen() == null) {
			fact.initNumberStream(timeToRepair, toString() + ".timeToRepair");
			timeToRepair.init();
		}

		machine.workStation.addNotifierListener(new WorkStationListenerBase() {
			@Override
			protected void activated(WorkStation m,
					IndividualMachine justActivated) {
				if (justActivated == machine
						&& machine.downReason == DowntimeSource.this) {
					onActivate();
				}
			}

			@Override
			protected void deactivated(WorkStation m,
					IndividualMachine justDeactivated) {
				if (justDeactivated == machine
						&& machine.downReason == DowntimeSource.this) {
					onDeactivate();
				}
			}

			@Override
			protected void done(WorkStation m) {
				m.removeNotifierListener(this);
			}
		});

		// schedule begin of first downtime
		onActivate();
	}

	protected void onActivate() {
		if (isSourceActive()) {
			JobShop shop = machine.workStation.shop();

			// schedule next downtime
			double nextFailure = calcDeactivateTime(shop);
			shop.schedule(new Event(nextFailure, WorkStation.TAKE_DOWN_PRIO) {
				@Override
				public void handle() {
					assert machine.workStation.currMachine == null;
					machine.workStation.currMachine = machine;
					machine.takeDown(DowntimeSource.this);
					machine.workStation.currMachine = null;
				}
			});
		}
	}

	protected boolean isSourceActive() {
		return timeBetweenFailures != null;
	}

	protected void onDeactivate() {
		JobShop shop = machine.workStation.shop();

		double whenReactivated = calcActivateTime(shop);
		machine.procFinished = whenReactivated;

		// schedule reactivation
		shop.schedule(new Event(whenReactivated, WorkStation.ACTIVATE_PRIO) {
			@Override
			public void handle() {
				assert machine.workStation.currMachine == null;
				machine.workStation.currMachine = machine;
				machine.activate();
				machine.workStation.currMachine = null;
			}
		});
	}

	protected double calcDeactivateTime(JobShop shop) {
		return shop.simTime() + timeBetweenFailures.nextDbl();
	}

	protected double calcActivateTime(JobShop shop) {
		return shop.simTime() + timeToRepair.nextDbl();
	}

	@Override
	public String toString() {
		return "downSource." + String.valueOf(machine)
				+ (name != null ? "." + name : "");
	}

	// boring getters and setters below

	public DblStream getTimeBetweenFailures() {
		return timeBetweenFailures;
	}

	public void setTimeBetweenFailures(DblStream timeBetweenFailures) {
		this.timeBetweenFailures = timeBetweenFailures;
	}

	public DblStream getTimeToRepair() {
		return timeToRepair;
	}

	public void setTimeToRepair(DblStream timeToRepair) {
		this.timeToRepair = timeToRepair;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IndividualMachine getMachine() {
		return machine;
	}

}
