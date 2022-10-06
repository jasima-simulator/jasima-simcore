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

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Family Earliest Due Date rule, developed by
 * Mahmoodi et al. (1990), which assigns highest priority to all jobs that
 * belong to the family which includes the job with the earliest due date, see
 * also Pickardt and Branke (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class FEDD extends PR {

	private static final long serialVersionUID = -1900010822908956309L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;
		return -(earliestFamilyDueDate(j));
	}

	public double earliestFamilyDueDate(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().getSetupState();
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double dd = Double.POSITIVE_INFINITY;
		for (int i = 0; i < q.size(); i++) {
			Job job = q.get(i);
			if (arrivesTooLate(job))
				continue;
			if (job.getCurrentOperation().getSetupState() == family && job.getDueDate() < dd) {
				dd = job.getDueDate();
			}
		}
		assert dd != Double.POSITIVE_INFINITY;
		return dd;
	}

}
