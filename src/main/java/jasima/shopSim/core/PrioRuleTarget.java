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

import jasima.core.util.ValueStore;

/**
 * Common abstract base class for {@link Job}s and {@link Batch}es.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class PrioRuleTarget implements ValueStore {

	public PrioRuleTarget() {
		super();
	}

	public abstract boolean isFuture();

	public abstract Operation getCurrentOperation();

	public abstract WorkStation getCurrMachine();

	public abstract Shop getShop();

	public abstract int getTaskNumber();

	public abstract int numOps();

	public abstract Operation[] getOps();

	public abstract double getArriveTime();

	public abstract double remainingProcTime();

	public abstract double getDueDate();

	public abstract double getWeight();

	public abstract double currProcTime();

	public abstract double getRelDate();

	public abstract int getJobNum();

	public abstract double getCurrentOperationDueDate();

	public abstract double procSum();

	public abstract int numOpsLeft();

	public abstract int numJobsInBatch();

	public abstract Job job(int i);

	public abstract boolean isBatch();

	public abstract String getName();
}
