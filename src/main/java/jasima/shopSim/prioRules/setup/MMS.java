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

import java.util.HashMap;
import java.util.Map;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Minimum Marginal Setup time rule, developed by Arzi
 * and Raviv (1998), which assigns highest priority to all jobs that belong to
 * the family with the shortest setup time per job, see also Pickardt and Branke
 * (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class MMS extends PR {

	private static final long serialVersionUID = -4646210941243911341L;

	protected Map<String, Integer> jobsPerFamily = new HashMap<String, Integer>();

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		jobsPerFamily.clear();
		assert jobsPerFamily.isEmpty();
		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);

			if (arrivesTooLate(j))
				continue;

			String sf = "" + j.getCurrentOperation().getSetupState();
			if (jobsPerFamily.get(sf) == null) {
				jobsPerFamily.put(sf, 1);
			} else {
				int familyValue = jobsPerFamily.get(sf) + 1;
				jobsPerFamily.put(sf, familyValue);
			}
		}

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		double marginalSetup = getOwner().getSetupMatrix()[getOwner().currMachine.setupState][j.getCurrentOperation()
				.getSetupState()] / jobsPerFamily.get("" + j.getCurrentOperation().getSetupState());

		return -marginalSetup;
	}

}
