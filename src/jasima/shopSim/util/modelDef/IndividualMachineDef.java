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

public class IndividualMachineDef extends PropertySupport {

	private static final long serialVersionUID = -5914995716138594202L;

	private double machRelDate = 0.0;
	private String initialSetup = WorkStation.DEF_SETUP_STR;
	private String name = null;
	private WorkstationDef workstation = null;

	public double getMachRelDate() {
		return machRelDate;
	}

	public void setMachRelDate(double machRelDate) {
		firePropertyChange("machRelDate", this.machRelDate,
				this.machRelDate = machRelDate);
	}

	public String getInitialSetup() {
		return initialSetup;
	}

	public void setInitialSetup(String initialSetup) {
		firePropertyChange("initialSetup", this.initialSetup,
				this.initialSetup = initialSetup);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange("name", this.name, this.name = name);
	}

	public WorkstationDef getWorkstation() {
		return workstation;
	}

	public void setWorkstation(WorkstationDef workstation) {
		firePropertyChange("workstation", this.workstation,
				this.workstation = workstation);
	}

}