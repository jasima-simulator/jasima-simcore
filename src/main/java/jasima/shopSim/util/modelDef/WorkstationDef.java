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
