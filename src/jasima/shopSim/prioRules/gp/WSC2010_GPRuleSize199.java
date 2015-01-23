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
package jasima.shopSim.prioRules.gp;

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.prioRules.basic.SLK;

import java.util.HashMap;
import java.util.Map;

/**
 * A rule from
 * "Generating dispatching rules for semiconductor manufacturing to minimize weighted tardiness"
 * , WinterSim 2010, doi:10.1109/WSC.2010.5678946.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class WSC2010_GPRuleSize199 extends GPRuleBase {

	private Map<String, Integer> famSizes;
	private double sAvg;
	private double pAvg;

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		super.beforeCalc(q);

		calcNumCompatible();
		sAvg = calcSetupAvg();
		pAvg = calcProcAvg();
	}

	public int numCompatible(PrioRuleTarget j) {
		String bf = j.getCurrentOperation().batchFamily;
		return famSizes.get(bf).intValue();
	}

	public double setupAvg() {
		return sAvg;
	}

	public double procAvg(PrioRuleTarget j) {
		// return pAvg;
		return j.currProcTime();
	}

	public void calcNumCompatible() {
		famSizes = new HashMap<String, Integer>();

		PriorityQueue<Job> q = getOwner().queue;
		for (int i = 0, n = q.size(); i < n; i++) {
			Job j = q.get(i);

			final String family = j.getCurrentOperation().batchFamily;
			Integer k = famSizes.get(family);
			if (k == null || "BATCH_INCOMPATIBLE".equals(family))
				k = 0;
			famSizes.put(family, k.intValue() + 1);
		}
	}

	public double calcSetupAvg() {
		final PriorityQueue<Job> q = getOwner().queue;
		assert q.size() > 0;
		final double[][] setupMatrix = getOwner().getSetupMatrix();

		final int machineSetup = getOwner().currMachine.setupState;

		int numNonFutures = 0;
		double setupAvg = 0.0d;

		for (int i = 0, n = q.size(); i < n; i++) {
			Job j2 = q.get(i);
			assert !j2.isFuture();
			if (!j2.isFuture()) {
				setupAvg += setupMatrix[machineSetup][j2.getCurrentOperation().setupState];
				numNonFutures++;
			}
		}
		return setupAvg / numNonFutures;
	}

	public double calcProcAvg() {
		final PriorityQueue<Job> q = getOwner().queue;
		assert q.size() > 0;

		int numNonFutures = 0;
		double procAvg = 0.0d;
		for (int i = 0, n = q.size(); i < n; i++) {
			Job job = q.get(i);
			if (!job.isFuture()) {
				procAvg += job.currProcTime();
				numNonFutures++;
			}
		}
		return procAvg / numNonFutures;
	}

	public double setupTime(PrioRuleTarget j) {
		assert j.getCurrMachine() == getOwner();
		final double[][] setupMatrix = j.getCurrMachine().getSetupMatrix();
		final int machineSetup = j.getCurrMachine().currMachine.setupState;

		return setupMatrix[machineSetup][j.getCurrentOperation().setupState];
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double bf = numCompatible(j);
		double ttd = j.getDueDate() - j.getShop().simTime();
		double w = j.getWeight();
		double sl = SLK.slack(j);
		double s = setupTime(j);
		double rpt = j.remainingProcTime();
		double sAvg = setupAvg();

		return bf
				* max(bf * ifte(max(1, rpt) - sl, w, bf)
						* (rpt * ifte(bf - sl, w, bf) / sl + 2 * rpt / sl - s)
						+ rpt * ifte(bf - s, w, bf) / sl + rpt / sl - s,
						ifte(bf - sl, w, bf)
								* max(-ifte(max(1, rpt) - max(sl, ttd), w, bf)
										- rpt / sl + (bf - max(1, ttd))
										* ifte(bf - max(1, ttd), w, bf) + bf,
										sAvg
												+ bf
												* ifte(max(1, rpt)
														- max(sl, ttd), w, bf)
												- s - max(1, max(rpt, sl))
												+ max(1, rpt) + 1) + rpt / sl
								- s)
				* ifte(max(
						bf * rpt * ifte(max(1, rpt) - sl, w, bf) / sl
								- ifte(bf - sl, w, bf) + (bf - max(1, ttd))
								* ifte(bf - max(1, ttd), w, bf), sAvg + bf
								* ifte(max(1, rpt) - max(sl, ttd), w, bf) - s
								- max(1, max(rpt, sl)) + max(1, rpt) + 1)
						* w, w, bf);
		// return mul(max(add(sub(mul(max(add(sub(mul(sub(bf, max(1, ttd)),
		// ifte(sub(bf, max(1, ttd)), w, bf)), add(
		// sub(div(rpt, sl), s), ifte(sub(max(rpt, 1), max(sl, ttd)),
		// w, bf))), sub(bf, s)), add(add(sub(mul(bf, ifte(
		// sub(max(rpt, 1), max(sl, ttd)), w, bf)), s), sub(
		// max(rpt, 1), max(sl, max(rpt, 1)))), add(sAvg, 1))), ifte(
		// sub(bf, sl), w, bf)), s), div(rpt, sl)), add(add(sub(mul(
		// add(sub(add(mul(div(rpt, sl), ifte(sub(bf, sl), w, bf)),
		// div(rpt, sl)), s), div(rpt, sl)), mul(bf, ifte(sub(
		// max(rpt, 1), sl), w, bf))), s), div(rpt, sl)), mul(
		// div(rpt, sl), ifte(sub(bf, s), w, bf)))), mul(bf, ifte(mul(
		// max(add(sub(mul(sub(bf, max(1, ttd)), ifte(sub(bf, max(1,
		// ttd)), w, bf)), ifte(sub(bf, sl), w, bf)), mul(div(
		// rpt, sl),
		// mul(bf, ifte(sub(max(rpt, 1), sl), w, bf)))),
		// add(add(sub(mul(bf, ifte(sub(max(rpt, 1), max(sl,
		// ttd)), w, bf)), s), sub(max(rpt, 1), max(
		// sl, max(rpt, 1)))), add(sAvg, 1))), w), w,
		// bf)));
	}

}
