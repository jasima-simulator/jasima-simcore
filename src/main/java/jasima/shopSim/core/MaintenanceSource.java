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

/**
 * This class can be used to model scheduled maintenance. Maintenance intervals
 * and duration are modeled using the {@code timeBetweenFailures} and
 * {@code timeToRepair} settings. The difference to {@link DowntimeSource} is
 * that {@code timeBetweenFailures} uses the <b>beginning</b> of the last
 * maintenance as the reference, whereas {@link DowntimeSource} uses the
 * <b>end</b> of the last downtime.
 * <p>
 * A simple example: consider a machine that is going to be down every 24 hours
 * for a duration of 1 hour, i.e., it is available for processing for 23 hours.
 * Using {@code MaintenanceSource}, this is modeled setting
 * {@code timeBetweenFailures} to 24 hours and using a {@code timeToRepair} of 1
 * hour.
 * 
 * @see DowntimeSource
 * @author Torsten Hildebrandt
 */
public class MaintenanceSource extends DowntimeSource {

	private double lastDeactivate;

	public MaintenanceSource(IndividualMachine machine) {
		super(machine);
	}

	@Override
	public void init() {
		super.init();
		lastDeactivate = getMachine().workStation.shop().simTime();
	}

	@Override
	protected double calcDeactivateTime(Shop shop) {
		lastDeactivate = lastDeactivate + getTimeBetweenFailures().nextDbl();
		return lastDeactivate;
	}

}
