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
package jasima.shopSim.prioRules.batch;

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.prioRules.setup.ATCS;

/**
 * This class implements a version of the Batch ATC with Setups rule by Mason et
 * al. (2002). The implementation is an analogous batch extension of the ATCS
 * rule by Lee and Pinedo (1997). Variant1 is another BATCS implementation
 * motivated by a paper by Mehta and Uzsoy (1998). The main difference lies
 * within the calculation of the slack of a batch, where the Variant1 takes into
 * account all jobs in the batch instead of only the most urgent one.
 * <p />
 * BATCS is supposed to be applied together with BestOfFamilyBatching to choose
 * among batches of different families.
 * 
 * @author Christoph Pickardt, 2011-11-14
 * @version $Id$
 */
public class BATCS extends ATCS {

	public BATCS(double k1, double k2) {
		super(k1, k2);
	}

	@Override
	public double calcPrio(PrioRuleTarget prt) {
		if (arrivesTooLate(prt))
			return PriorityQueue.MIN_PRIO;

		double slack = getEarliestODD(prt) - prt.getShop().simTime()
				- prt.currProcTime();
		double prod1 = -Math.max(slack, 0.0d) / slackNorm;
		double prod2 = setupNorm != 0.0 ? -setupMatrix[getOwner().currMachine.setupState][prt
				.getCurrentOperation().setupState] / setupNorm
				: 0.0;

		return Math.log(prt.getWeight() / prt.currProcTime())
				+ prod1
				+ prod2
				+ Math.log((double) prt.numJobsInBatch()
						/ prt.getCurrentOperation().maxBatchSize);
	}

	public double getEarliestODD(PrioRuleTarget j) {
		double odd = j.job(0).getCurrentOperationDueDate();
		for (int i = 1; i < j.numJobsInBatch(); i++) {
			double d = j.job(i).getCurrentOperationDueDate();
			if (d < odd)
				odd = d;
		}
		return odd;
	}

	@Override
	public String getName() {
		return "BATCS(k1=" + getK1() + ";k2=" + getK2() + ")";
	}

	public static class Variant1 extends ATCS {

		public Variant1(double k1, double k2) {
			super(k1, k2);
		}

		@Override
		public double calcPrio(PrioRuleTarget prt) {
			if (arrivesTooLate(prt))
				return PriorityQueue.MIN_PRIO;

			double prod1 = -getTotalOSlack(prt) / slackNorm;
			double prod2 = setupNorm != 0.0 ? -setupMatrix[getOwner().currMachine.setupState][prt
					.getCurrentOperation().setupState] / setupNorm
					: 0.0;

			return Math.log(prt.getWeight() / prt.currProcTime())
					+ prod1
					+ prod2
					+ Math.log((double) prt.numJobsInBatch()
							/ prt.getCurrentOperation().maxBatchSize);
		}

		public double getTotalOSlack(PrioRuleTarget b) {
			double oSlack = 0;
			for (int i = 0; i < b.numJobsInBatch(); i++) {
				oSlack += Math.max(b.job(i).getCurrentOperationDueDate()
						- b.getShop().simTime() - b.currProcTime(), 0);
			}
			return oSlack;
		}

		@Override
		public String getName() {
			return "BATCS_V1(k1=" + getK1() + ";k2=" + getK2() + ")";
		}
	}

}
