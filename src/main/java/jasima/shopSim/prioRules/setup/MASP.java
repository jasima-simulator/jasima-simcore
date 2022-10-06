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

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Minimum Average Setup plus Processing time rule,
 * developed by Russell and Philipoom (1991), which assigns highest priority to
 * all jobs that belong to the family with the shortest processing time per job,
 * see also Pickardt and Branke (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class MASP extends MMS {

	private static final long serialVersionUID = -464213521355057733L;

	protected Map<String, Double> sumProcTimePerFamily = new HashMap<String, Double>();

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		sumProcTimePerFamily.clear();
		assert sumProcTimePerFamily.isEmpty();
		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);

			if (arrivesTooLate(j))
				continue;

			String sf = "" + j.getCurrentOperation().getSetupState();
			if (sumProcTimePerFamily.get(sf) == null) {
				sumProcTimePerFamily.put(sf, j.currProcTime());
			} else {
				double familyValue = sumProcTimePerFamily.get(sf) + j.currProcTime();
				sumProcTimePerFamily.put(sf, familyValue);
			}
		}

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		double setup = getOwner().getSetupMatrix()[getOwner().currMachine.setupState][j.getCurrentOperation()
				.getSetupState()];
		String family = "" + j.getCurrentOperation().getSetupState();

		return -((setup + sumProcTimePerFamily.get(family))) / jobsPerFamily.get(family);
	}

	public double sumFamilyProcessingTime(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().getSetupState();
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double procTime = 0;
		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget job = q.get(i);
			if (arrivesTooLate(job))
				continue;
			if (job.getCurrentOperation().getSetupState() == family) {
				procTime = procTime + job.currProcTime();
			}
		}
		return procTime;
	}

}
