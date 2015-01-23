/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
 *******************************************************************************/
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Apparent Tardiness Costs rule by Vepsalainen and
 * Morton (1987).
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
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

		double slack = job.getCurrentOperationDueDate()
				- job.getShop().simTime() - job.currProcTime();
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
