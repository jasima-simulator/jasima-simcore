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
 * <p>
 * This class implements the Apparent Tardiness Costs with Setups rule,
 * developed by Lee and Pinedo (1997). This implementation uses an operation due
 * date computed by ODD instead of the global slack.
 * </p>
 * <p>
 * The class directly inherits from MaxWaitThresholdRule and the threshold value
 * is also used to calculate queue terminals that only consider jobs which
 * arrive before that the threshold value.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @author Torsten Hildebrandt
 */
public class ATCS extends PR {

	private static final long serialVersionUID = -331130943946735200L;

	private double k1;
	private double k2;

	protected double[][] setupMatrix;

	public ATCS() {
		this(1.0, 1.0);
	}

	public ATCS(double k1, double k2) {
		super();
		this.k1 = k1;
		this.k2 = k2;
	}

	protected double slackNorm;
	protected double setupNorm;

	/**
	 * Update procAvg and setupAvg before inherited method is called which in
	 * turn calls calcPrio(job).
	 */
	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		if (setupMatrix == null)
			setupMatrix = getOwner().getSetupMatrix();

		int numJobs = 0;
		slackNorm = 0.0d;
		setupNorm = 0.0d;

		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);

			if (arrivesTooLate(j))
				continue;

			slackNorm += j.currProcTime();
			setupNorm += setupMatrix[getOwner().currMachine.setupState][j.getCurrentOperation().getSetupState()];
			numJobs++;
		}
		slackNorm = (slackNorm / numJobs) * k1;
		setupNorm = (setupNorm / numJobs) * k2;

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (arrivesTooLate(job))
			return PriorityQueue.MIN_PRIO;

		double slack = job.getCurrentOperationDueDate() - job.getShop().simTime() - job.currProcTime();
		double prod1 = -Math.max(slack, 0.0d) / slackNorm;
		double prod2 = setupNorm != 0.0
				? -setupMatrix[getOwner().currMachine.setupState][job.getCurrentOperation().getSetupState()] / setupNorm
				: 0.0;

		return Math.log(job.getWeight() / job.currProcTime()) + prod1 + prod2;
		// double prod1 = Math.exp(-Math.max(slack, 0.0d) / slackNorm);
		// double prod2 = setupNorm != 0.0 ? Math
		// .exp(-setupMatrix[getOwner().currMachine.setupState][job
		// .getCurrentOperation().setupState]
		// / setupNorm) : 1.0;
		//
		// return job.getWeight() / job.currProcTime() * prod1 * prod2;
	}

	@Override
	public String getName() {
		return "ATCS(k1=" + k1 + ";k2=" + k2 + ")";
	}

	public double getK1() {
		return k1;
	}

	public void setK1(double k1) {
		this.k1 = k1;
	}

	public double getK2() {
		return k2;
	}

	public void setK2(double k2) {
		this.k2 = k2;
	}

}
