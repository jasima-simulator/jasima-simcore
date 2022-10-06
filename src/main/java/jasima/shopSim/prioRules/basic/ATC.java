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
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Apparent Tardiness Costs rule by Vepsalainen and
 * Morton (1987).
 * 
 * @author Torsten Hildebrandt
 */
public class ATC extends PR {

	private static final long serialVersionUID = -5302187389726254037L;

	private double k;

	public ATC() {
		this(1.0);
	}

	public ATC(double k) {
		super();
		setK(k);
	}

	protected double slackNorm;

	/**
	 * Update procAvg and setupAvg before inherited method is called which in
	 * turn calls calcPrio(job).
	 */
	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		int numJobs = 0;
		slackNorm = 0.0d;

		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);

			if (arrivesTooLate(j))
				continue;

			slackNorm += j.currProcTime();
			numJobs++;
		}
		slackNorm = (slackNorm / numJobs) * k;

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (arrivesTooLate(job))
			return PriorityQueue.MIN_PRIO;

		double slack = job.getCurrentOperationDueDate() - job.getShop().simTime() - job.currProcTime();
		double prod1 = -Math.max(slack, 0.0d) / slackNorm;

		return (job.getWeight() / job.currProcTime()) * Math.exp(prod1);
	}

	@Override
	public String getName() {
		return "ATC(k=" + getK() + ")";
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}

}
