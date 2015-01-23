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

/**
 * This class can be used to model scheduled maintenance. Maintenance intervals
 * and duration are modeled using the {@code timeBetweenFailures} and
 * {@code timeToRepair} settings. The difference to {@link DowntimeSource} is
 * that {@code timeBetweenFailures} uses the <b>beginning</b> of the last maintenance
 * as the reference, whereas {@link DowntimeSource} uses the <b>end</b> of the last
 * downtime.
 * <p>
 * A simple example: consider a machine that is going to be down every 24 hours
 * for a duration of 1 hour, i.e., it is available for processing for 23 hours.
 * Using {@code MaintenanceSource}, this is modeled setting
 * {@code timeBetweenFailures} to 24 hours and using a {@code timeToRepair} of 1
 * hour.
 * 
 * @see DowntimeSource
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2014-04-16
 * @version "$Id$"
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
	protected double calcDeactivateTime(JobShop shop) {
		lastDeactivate = lastDeactivate + getTimeBetweenFailures().nextDbl();
		return lastDeactivate;
	}

}
