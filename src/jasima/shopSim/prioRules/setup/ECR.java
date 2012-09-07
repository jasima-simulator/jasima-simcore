/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
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
 * @version $Id$
 */
public class ECR extends PR {

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

		double CR = (j.getDueDate() - j.getShop().simTime())
				/ j.remainingProcTime();
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
				a = j.getDueDate()
						- j.getShop().simTime()
						- setupMatrix[getOwner().currMachine.setupState][j
								.getCurrentOperation().setupState]
						- j.currProcTime();

			} else {
				r = job.remainingProcTime();
				a = job.getDueDate()
						- j.getShop().simTime()
						- setupMatrix[getOwner().currMachine.setupState][j
								.getCurrentOperation().setupState]
						- j.currProcTime()
						- setupMatrix[j.getCurrentOperation().setupState][job
								.getCurrentOperation().setupState];
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
