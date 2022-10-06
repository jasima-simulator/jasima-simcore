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
package jasima.shopSim.prioRules.batch;

import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Largest Relative Batch rule, which chooses the
 * batch that uses most of the available capacity of the machine.
 * 
 * @author Christoph Pickardt, 2011-11-14
 */
public class MaxBatchSize extends PR {

	private static final long serialVersionUID = -689042486920247381L;

	@Override
	public double calcPrio(PrioRuleTarget b) {
		Operation o = b.getCurrentOperation();
		return ((double) b.numJobsInBatch() / o.getMaxBatchSize());
	}

}
