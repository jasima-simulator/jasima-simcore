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
package jasima.shopSim.util.modelDef;

import jasima.shopSim.core.WorkStation;

public class WorkstationDef extends PropertySupport {

	private static final long serialVersionUID = -3259382268320160215L;

	public static final String PROP_MACHINES = "machines";
	public static final String PROP_NAME = "name";
	public static final String PROP_SETUP_STATES = "setupStates";
	public static final String PROP_SETUP_TIMES = "setupTimes";

	private String name;
	private IndividualMachineDef[] machines;;
	private String[] setupStates = { WorkStation.DEF_SETUP_STR };
	private double[][] setupTimes = { { 0.0 } };

	public WorkstationDef() {
		super();
		setMachines(new IndividualMachineDef[] { new IndividualMachineDef() });
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}

	public IndividualMachineDef[] getMachines() {
		return machines;
	}

	public void setMachines(IndividualMachineDef[] ms) {
		if (ms != null)
			for (IndividualMachineDef m : ms) {
				m.setWorkstation(this);
			}
		if (machines != null)
			for (IndividualMachineDef m : machines) {
				m.setWorkstation(null);
			}
		firePropertyChange(PROP_MACHINES, this.machines, this.machines = ms);
	}

	public String[] getSetupStates() {
		return setupStates;
	}

	public void setSetupStates(String[] setupStates) {
		firePropertyChange(PROP_SETUP_STATES, this.setupStates,
				this.setupStates = setupStates);
	}

	public double[][] getSetupTimes() {
		return setupTimes;
	}

	public void setSetupTimes(double[][] setupTimes) {
		firePropertyChange(PROP_SETUP_TIMES, this.setupTimes,
				this.setupTimes = setupTimes);
	}

}
