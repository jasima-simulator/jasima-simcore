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
 * This class implements the Family First Come First Served rule, developed by
 * Flynn (1987), which assigns highest priority to all jobs that belong to the
 * family which includes the job that arrived first to the queue, see also
 * Pickardt and Branke (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class FFCFS extends PR {

	private static final long serialVersionUID = -9158038750813584890L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(earliestFamilyArrival(j));
	}

	public double earliestFamilyArrival(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().getSetupState();
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double at = Double.POSITIVE_INFINITY;
		for (int i = 0; i < q.size(); i++) {
			Job job = q.get(i);
			if (job.getCurrentOperation().getSetupState() == family && job.getArriveTime() < at) {
				at = job.getArriveTime();
			}
		}
		assert at != Double.POSITIVE_INFINITY;
		return at;
	}

}
