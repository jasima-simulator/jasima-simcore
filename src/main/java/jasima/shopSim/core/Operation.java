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
 * Operations specify what to process.
 * 
 * @author Torsten Hildebrandt
 */
public class Operation implements Cloneable {

	private WorkStation machine;
	private double procTime = Double.NaN;
	private int setupState = WorkStation.DEF_SETUP;
	private String batchFamily = WorkStation.BATCH_INCOMPATIBLE;
	private int maxBatchSize = 1;

	@Override
	public Operation clone() throws CloneNotSupportedException {
		return (Operation) super.clone();
	}

	public WorkStation getMachine() {
		return machine;
	}

	public void setMachine(WorkStation machine) {
		this.machine = machine;
	}

	public int getSetupState() {
		return setupState;
	}

	public void setSetupState(int setupState) {
		this.setupState = setupState;
	}

	public String getBatchFamily() {
		return batchFamily;
	}

	public void setBatchFamily(String batchFamily) {
		this.batchFamily = batchFamily;
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	public double getProcTime() {
		return procTime;
	}

	public void setProcTime(double procTime) {
		this.procTime = procTime;
	}

}
