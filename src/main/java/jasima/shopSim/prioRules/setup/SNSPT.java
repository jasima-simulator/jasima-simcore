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

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Shortest Normalized Setup and Processing Time rule,
 * see Pickardt and Branke (2012).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class SNSPT extends PR {

	private static final long serialVersionUID = 3765192665858565459L;

	protected double[][] setupMatrix;

	protected double procNorm;
	protected double setupNorm;

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		if (setupMatrix == null)
			setupMatrix = getOwner().getSetupMatrix();

		int numJobs = 0;
		procNorm = 0.0d;
		setupNorm = 0.0d;

		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);
			if (arrivesTooLate(j))
				continue;

			procNorm += j.currProcTime();
			setupNorm += setupMatrix[getOwner().currMachine.setupState][j.getCurrentOperation().getSetupState()];
			numJobs++;
		}
		procNorm = (procNorm / numJobs);
		setupNorm = (setupNorm / numJobs);

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (arrivesTooLate(job))
			return PriorityQueue.MIN_PRIO;

		double sRatio = setupNorm != 0.0
				? setupMatrix[getOwner().currMachine.setupState][job.getCurrentOperation().getSetupState()] / setupNorm
				: 0.0;
		return -(sRatio + (job.currProcTime() / procNorm));
	}

}
