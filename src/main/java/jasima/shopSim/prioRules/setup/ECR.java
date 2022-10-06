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
 * This class implements the Enhanced Critical Ratio rule, developed by Chiang
 * and Fu (2009), see also Pickardt and Branke (2012).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class ECR extends PR {

	private static final long serialVersionUID = -122785104940341037L;

	public final double L;
	public final double U;
	public final double B;
	public final double D;

	private double[][] setupMatrix;

	public ECR(double L, double U, double B, double D) {
		super();
		this.L = L;
		this.U = U;
		this.B = B;
		this.D = D;
	}

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		if (setupMatrix == null)
			setupMatrix = getOwner().getSetupMatrix();

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		double CR = (j.getDueDate() - j.getShop().simTime()) / j.remainingProcTime();
		if (CR < L || CR > U)
			return PriorityQueue.MIN_PRIO;

		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;
		double totalUrgency = 0.0;
		double r;
		double a;
		for (int i = 0, n = q.size(); i < n; i++) {
			Job job = q.get(i);
			if (arrivesTooLate(job))
				continue;
			if (job.equals(j)) {
				r = j.remainingProcTime() - j.currProcTime();
				a = j.getDueDate() - j.getShop().simTime()
						- setupMatrix[getOwner().currMachine.setupState][j.getCurrentOperation().getSetupState()]
						- j.currProcTime();

			} else {
				r = job.remainingProcTime();
				a = job.getDueDate() - j.getShop().simTime()
						- setupMatrix[getOwner().currMachine.setupState][j.getCurrentOperation().getSetupState()]
						- j.currProcTime() - setupMatrix[j.getCurrentOperation().getSetupState()][job
								.getCurrentOperation().getSetupState()];
			}
			totalUrgency += calculateUrgency(r, a);
		}
		return -totalUrgency;
	}

	public double calculateUrgency(double r, double a) {
		if (r <= 0)
			return 0.0d;
		if (a >= r)
			return Math.pow((r / a), 2);
		else
			return B + D * (r - a);
	}

}
