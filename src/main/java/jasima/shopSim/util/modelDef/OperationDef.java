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

import jasima.shopSim.core.WorkStation;

public class OperationDef extends PropertySupport {

	private static final long serialVersionUID = 1410027539388338623L;

	public static final String PROP_NAME = "name";
	public static final String PROP_PROC_TIME = "procTime";
	public static final String PROP_WORKSTATION = "workstation";
	public static final String PROP_SETUP = "setup";
	public static final String PROP_BATCH_FAMILY = "batchFamily";
	public static final String PROP_MAX_BATCH_SIZE = "maxBatchSize";

	private String name;
	private double procTime;
	private int workstation;
	private String setup = WorkStation.DEF_SETUP_STR;
	private String batchFamily = WorkStation.BATCH_INCOMPATIBLE;
	private int maxBatchSize = 1;

	public double getProcTime() {
		return procTime;
	}

	public void setProcTime(double procTime) {
		firePropertyChange(PROP_PROC_TIME, this.procTime, this.procTime = procTime);
	}

	public int getWorkstation() {
		return workstation;
	}

	public void setWorkstation(int workstation) {
		firePropertyChange(PROP_WORKSTATION, this.workstation, this.workstation = workstation);
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
		firePropertyChange(PROP_BATCH_FAMILY, this.batchFamily, this.batchFamily = batchFamily);
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		firePropertyChange(PROP_MAX_BATCH_SIZE, this.maxBatchSize, this.maxBatchSize = maxBatchSize);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		firePropertyChange(PROP_NAME, this.name, this.name = name);
	}
}
