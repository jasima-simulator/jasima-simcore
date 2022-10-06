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
package jasima.shopSim.prioRules.setup;

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Weighted Batch Processing Time rule, developed by
 * Raman et al. (1989), which is an additive combination of MMS and SPT, see
 * also Pickardt and Branke (2012).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class WBPT extends MMS {

	private static final long serialVersionUID = 8461106077678169718L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (arrivesTooLate(job))
			return PriorityQueue.MIN_PRIO;

		double marginalSetup = getOwner().getSetupMatrix()[getOwner().currMachine.setupState][job.getCurrentOperation()
				.getSetupState()] / jobsPerFamily.get("" + job.getCurrentOperation().getSetupState());

		return -(marginalSetup + job.currProcTime());
	}

}
