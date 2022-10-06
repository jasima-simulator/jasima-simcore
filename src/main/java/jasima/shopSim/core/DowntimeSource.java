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

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimComponent;

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
 * @author Torsten Hildebrandt
 */
public class DowntimeSource {

	private final IndividualMachine machine;
	private DblSequence timeBetweenFailures;
	private DblSequence timeToRepair;
	private String name;

	public DowntimeSource(IndividualMachine machine) {
		super();
		this.machine = machine;
	}

	public void init() {
		// initialize random streams
		RandomFactory fact = machine.workStation.getSim().getRndStreamFactory();
		if (timeBetweenFailures != null && timeBetweenFailures.getRndGen() == null) {
			fact.initRndGen(timeBetweenFailures, toString() + ".timeBetweenFailures");
		}
		if (timeToRepair != null && timeToRepair.getRndGen() == null) {
			fact.initRndGen(timeToRepair, toString() + ".timeToRepair");
		}

		WorkStationListener wsl = new WorkStationListener() {
			@Override
			public void activated(WorkStation m, IndividualMachine justActivated) {
				if (justActivated == machine && machine.downReason == DowntimeSource.this) {
					onActivate();
				}
			}

			@Override
			public void deactivated(WorkStation m, IndividualMachine justDeactivated) {
				if (justDeactivated == machine && machine.downReason == DowntimeSource.this) {
					onDeactivate();
				}
			}

			@Override
			public void done(SimComponent m) {
				machine.workStation.removeListener(this);
			}
		};
		machine.workStation.addListener(wsl);

		// schedule begin of first downtime
		onActivate();
	}

	protected void onActivate() {
		if (isSourceActive()) {
			Shop shop = machine.workStation.shop();

			// schedule next downtime
			double nextFailure = calcDeactivateTime(shop);
			shop.getSim().scheduleAt(nextFailure, WorkStation.TAKE_DOWN_PRIO, () -> {
				assert machine.workStation.currMachine == null;
				machine.workStation.currMachine = machine;
				machine.takeDown(DowntimeSource.this);
				machine.workStation.currMachine = null;
			});
		}
	}

	protected boolean isSourceActive() {
		return timeBetweenFailures != null;
	}

	protected void onDeactivate() {
		Shop shop = machine.workStation.shop();

		double whenReactivated = calcActivateTime(shop);
		machine.procFinished = whenReactivated;

		// schedule reactivation
		shop.getSim().scheduleAt(whenReactivated, WorkStation.ACTIVATE_PRIO, () -> {
			assert machine.workStation.currMachine == null;
			machine.workStation.currMachine = machine;
			machine.activate();
			machine.workStation.currMachine = null;
		});
	}

	protected double calcDeactivateTime(Shop shop) {
		return shop.simTime() + ensurePositiveNumber(timeBetweenFailures);
	}

	protected double calcActivateTime(Shop shop) {
		return shop.simTime() + ensurePositiveNumber(timeToRepair);
	}

	protected double ensurePositiveNumber(DblSequence dblStream) {
		double ttr;
		do {
			ttr = dblStream.nextDbl();
		} while (ttr < 0);

		return ttr;
	}

	@Override
	public String toString() {
		return "downSource." + String.valueOf(machine) + (name != null ? "." + name : "");
	}

	// boring getters and setters below

	public DblSequence getTimeBetweenFailures() {
		return timeBetweenFailures;
	}

	public void setTimeBetweenFailures(DblSequence timeBetweenFailures) {
		this.timeBetweenFailures = timeBetweenFailures;
	}

	public DblSequence getTimeToRepair() {
		return timeToRepair;
	}

	public void setTimeToRepair(DblSequence timeToRepair) {
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
