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
package jasima.shopSim.prioRules.upDownStream;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;

/**
 * The class implements the Extended Least Work content In Next Queue rule, see
 * Haupt (1989). The work content of all jobs waiting or arriving soon at the
 * work centre which a job visits for its next operation defines the priority of
 * a job. For its last operation, the work content in the next queue of a job is
 * zero.
 * <p>
 * The lookahead needs to be enabled in the simulation for this rule to work
 * properly.
 * 
 * @author Torsten Hildebrandt
 */
public class XWINQ extends PR {

	private static final long serialVersionUID = -83039692160872143L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -xwinq(job);
	}

	/**
	 * Computes the work in next queue of the jobs currently waiting in front of
	 * the next machine <em>plus</em> future jobs.
	 */
	public static double xwinq(PrioRuleTarget job) {
		int nextTask = job.getTaskNumber() + 1;
		if (nextTask >= job.getOps().length)
			return 0;

		WorkStation mNext = job.getOps()[nextTask].getMachine();
		return mNext.workContent(true);
	}

}
