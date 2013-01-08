/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 *******************************************************************************/
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
 * <p />
 * This class does not take setup times into account.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class AdaptiveLAThreshold extends LookaheadThreshold {

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
				double timeToComplete = setupMatrix[currSetupState][o.setupState]
						+ o.procTime
						+ setupMatrix[o.setupState][currSetupState];
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
			throw new IllegalArgumentException("maxWaitRelative "
					+ maxWaitRelative + " has to be within [0,1]!");

		this.maxWaitRelative = maxWaitRelative;
	}

}
