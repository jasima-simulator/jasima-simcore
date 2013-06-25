package jasima.shopSim.util.modelDef;

import jasima.shopSim.core.WorkStation;

public class IndividualMachineDef extends PropertySupport {

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
		firePropertyChange("workstation", this.workstation, this.workstation = workstation);
	}

}