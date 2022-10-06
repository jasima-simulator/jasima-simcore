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

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * This class implements the Largest Batch Family rule, which chooses the job
 * for which most other jobs of the same batch family are waiting in the queue.
 * 
 * @author Christoph Pickardt, 2011-11-14
 */
public class LBF extends PR {

	private static final long serialVersionUID = 2198657617441057488L;

	public LBF() {
		super();
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		final String family = j.getCurrentOperation().getBatchFamily();
		if (WorkStation.BATCH_INCOMPATIBLE.equals(family))
			return 1;

		int res = 0;
		for (int i = 0; i < j.getCurrMachine().getJobsByFamily().get(family).size(); i++) {
			if (!arrivesTooLate(j.getCurrMachine().getJobsByFamily().get(family).get(i)))
				res++;
		}
		assert res >= 1;

		return res;
	}

	@Override
	public String getName() {
		return "LBF";
	}

}
