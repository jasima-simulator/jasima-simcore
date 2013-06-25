package jasima.shopSim.util.modelDef;

import jasima.shopSim.core.WorkStation;

public class OperationDef extends PropertySupport {

	public static final String PROP_NAME = "name";
	public static final String PROP_PROC_TIME = "procTime";
	public static final String PROP_WORKSTATION = "workstation";
	public static final String PROP_SETUP = "setup";
	public static final String PROP_BATCH_FAMILY = "batchFamily";
	public static final String PROP_MAX_BATCH_SIZE = "maxBatchSize";
	
	private String name;
	private double procTime;
	private WorkstationDef workstation;
	private String setup = WorkStation.DEF_SETUP_STR;
	private String batchFamily = WorkStation.BATCH_INCOMPATIBLE;
	private int maxBatchSize = 1;

	public double getProcTime() {
		return procTime;
	}

	public void setProcTime(double procTime) {
		firePropertyChange(PROP_PROC_TIME, this.procTime, this.procTime = procTime);
	}

	public WorkstationDef getWorkstation() {
		return workstation;
	}

	public void setWorkstation(WorkstationDef workstation) {
		firePropertyChange(PROP_WORKSTATION, this.workstation,
				this.workstation = workstation);
	}

	public String getSetup() {
		return setup;
	}

	public void setSetup(String setup) {
		firePropertyChange(PROP_SETUP, this.setup, this.setup = setup);
	}

	public String getBatchFamily() {
		return batchFamily;
	}

	public void setBatchFamily(String batchFamily) {
		firePropertyChange(PROP_BATCH_FAMILY, this.batchFamily,
				this.batchFamily = batchFamily);
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		firePropertyChange(PROP_MAX_BATCH_SIZE, this.maxBatchSize,
				this.maxBatchSize = maxBatchSize);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}
}
