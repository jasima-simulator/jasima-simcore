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
package jasima.shopSim.util.modelDef;

import jasima.core.util.TypeUtil;
import jasima.shopSim.core.WorkStation;

public class WorkstationDef extends PropertySupport {

	private static final long serialVersionUID = -3259382268320160215L;

	public static final String PROP_NAME = "name";
	public static final String PROP_NUM_IN_GROUP = "numInGroup";
	public static final String PROP_INITIAL_SETUPS = "initialSetups";
	public static final String PROP_MACH_RELEASE_DATES = "machReleaseDates";
	public static final String PROP_SETUP_STATES = "setupStates";
	public static final String PROP_SETUP_TIMES = "setupTimes";

	private String name;
	private int numInGroup = 1;
	private String[] initialSetups = null;
	private double[] machReleaseDates = null;
	private String[] setupStates = { WorkStation.DEF_SETUP_STR };
	private double[][] setupTimes = { { 0.0 } };

	public WorkstationDef() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}

	public int getNumInGroup() {
		return numInGroup;
	}

	public void setNumInGroup(int numInGroup) {
		firePropertyChange(PROP_NUM_IN_GROUP, this.numInGroup, this.numInGroup = numInGroup);
	}

	public String[] getInitialSetups() {
		return initialSetups;
	}

	public void setInitialSetups(String[] initialSetups) {
		firePropertyChange(PROP_INITIAL_SETUPS, this.initialSetups, this.initialSetups = initialSetups);
	}

	public double[] getMachReleaseDates() {
		return machReleaseDates;
	}

	public void setMachReleaseDates(double[] machReleaseDates) {
		firePropertyChange(PROP_MACH_RELEASE_DATES, this.machReleaseDates, this.machReleaseDates = machReleaseDates);
	}

	public String[] getSetupStates() {
		return setupStates;
	}

	public void setSetupStates(String[] setupStates) {
		firePropertyChange(PROP_SETUP_STATES, this.setupStates, this.setupStates = setupStates);
	}

	public double[][] getSetupTimes() {
		return setupTimes;
	}

	public void setSetupTimes(double[][] setupTimes) {
		firePropertyChange(PROP_SETUP_TIMES, this.setupTimes, this.setupTimes = setupTimes);
	}

	@Override
	public WorkstationDef clone() {
		WorkstationDef c = (WorkstationDef) super.clone();

		if (initialSetups != null) {
			c.initialSetups = initialSetups.clone();
		}

		if (machReleaseDates != null) {
			c.machReleaseDates = machReleaseDates.clone();
		}

		if (setupStates != null) {
			c.setupStates = setupStates.clone();
		}

		c.setupTimes = TypeUtil.deepCloneArrayIfPossible(setupTimes);

		return c;
	}

}
