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
package jasima.shopSim.prioRules.meta;

import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements a rule with a max waiting time threshold value
 * specified relative to the minimum time it takes to process any job waiting in
 * the queue. maxWaitRelative has to be in the range [0,1]. The maximal useful
 * waiting time actually is even a little smaller than 1: if a job arrives in
 * say 10 time units, but to process the shortest job in queue also takes 10
 * time units, it could be finished exactly when the lookahead job arrives.
 * <p>
 * This class does not take setup times into account.
 * 
 * @author Torsten Hildebrandt
 */
public class AdaptiveLAThreshold extends LookaheadThreshold {

	private static final long serialVersionUID = -9044425584894392686L;

	private double maxWaitRelative;

	private double maxWait;

	public AdaptiveLAThreshold(PR baseRule) {
		this(baseRule, 1.0);
	}

	public AdaptiveLAThreshold(double maxWaitRelative) {
		this(null, maxWaitRelative);
	}

	public AdaptiveLAThreshold(PR baseRule, double maxWaitRelative) {
		super();

		setMaxWaitRelative(maxWaitRelative);
		setTieBreaker(baseRule);
	}

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		maxWait = getMaxWaitRelative() * maxProcTimeWaiting(q);
	}

	@Override
	public boolean arrivesTooLate(PrioRuleTarget prt) {
		return arrivesTooLate(prt, maxWait);
	}

	public double maxProcTimeWaiting(PriorityQueue<?> q) {
		// find maximum time in which a jobs currently waiting can be finished
		// (incl. setups)
		double[][] setupMatrix = getOwner().getSetupMatrix();
		int currSetupState = getOwner().currMachine.setupState;

		double res = 0.0d;
		for (int i = 0, n = q.size(); i < n; i++) {
			PrioRuleTarget j = q.get(i);
			if (!j.isFuture()) {
				Operation o = j.getCurrentOperation();
				double timeToComplete = setupMatrix[currSetupState][o.getSetupState()] + o.getProcTime()
						+ setupMatrix[o.getSetupState()][currSetupState];
				if (timeToComplete > res) {
					res = timeToComplete;
				}
			}
		}
		return res;
	}

	@Override
	public String getName() {
		return "ALA(" + getMaxWaitRelative() + ")";
	}

	public double getMaxWaitRelative() {
		return maxWaitRelative;
	}

	public void setMaxWaitRelative(double maxWaitRelative) {
		if (maxWaitRelative < 0.0 || maxWaitRelative > 1.0)
			throw new IllegalArgumentException("maxWaitRelative " + maxWaitRelative + " has to be within [0,1]!");

		this.maxWaitRelative = maxWaitRelative;
	}

}
