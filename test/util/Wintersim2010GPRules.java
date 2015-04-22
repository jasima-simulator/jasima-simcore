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
package util;

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.prioRules.basic.SLK;
import jasima.shopSim.prioRules.gp.GPRuleBase;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: Wintersim2010GPRules.java 550 2015-01-23 15:07:23Z
 *          thildebrandt@gmail.com $
 */
public class Wintersim2010GPRules {

	@SuppressWarnings("serial")
	public abstract static class Base extends GPRuleBase {

		public double max(double v1, double v2, double v3) {
			return max(max(v1, v2), v3);
		}

		private Map<String, Integer> famSizes;
		private double sAvg;

		// private double pAvg;

		@Override
		public void beforeCalc(PriorityQueue<?> q) {
			super.beforeCalc(q);

			calcNumCompatible();
			sAvg = calcSetupAvg();
			// pAvg = calcProcAvg();
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
					setupAvg += setupMatrix[machineSetup][j2
							.getCurrentOperation().setupState];
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
	}

	public static class GPRuleSize199 extends Base {

		private static final long serialVersionUID = -1679348310041076504L;

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
					* max(bf
							* ifte(max(1, rpt) - sl, w, bf)
							* (rpt * ifte(bf - sl, w, bf) / sl + 2 * rpt / sl - s)
							+ rpt * ifte(bf - s, w, bf) / sl + rpt / sl - s,
							ifte(bf - sl, w, bf)
									* max(-ifte(max(1, rpt) - max(sl, ttd), w,
											bf)
											- rpt
											/ sl
											+ (bf - max(1, ttd))
											* ifte(bf - max(1, ttd), w, bf)
											+ bf,
											sAvg
													+ bf
													* ifte(max(1, rpt)
															- max(sl, ttd), w,
															bf) - s
													- max(1, rpt, sl)
													+ max(1, rpt) + 1) + rpt
									/ sl - s)
					* ifte(max(
							bf * rpt * ifte(max(1, rpt) - sl, w, bf) / sl
									- ifte(bf - sl, w, bf) + (bf - max(1, ttd))
									* ifte(bf - max(1, ttd), w, bf), sAvg + bf
									* ifte(max(1, rpt) - max(sl, ttd), w, bf)
									- s - max(1, rpt, sl) + max(1, rpt) + 1)
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

	public static class GPRuleSize122 extends Base {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3582637779309774037L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double rpt = j.remainingProcTime();
			double sAvg = setupAvg();

			return ifte(max(1, rpt) - max(1, rpt, sl), w, bf)
					* bf
					* max(ifte(bf - sl, w, bf)
							* max(-ifte(max(1, rpt) - max(sl, ttd), w, bf) + s
									+ bf,
									bf
											* ifte(-max(sl, ttd), w, bf)
											+ bf
											* ifte(-max(sl, ttd) - sl
													+ max(1, rpt), w, bf) - s
											- max(1, rpt, sl) + max(1, rpt))
							+ rpt / sl - s,
							sAvg
									+ bf
									* ifte(max(1, rpt) - sl, w, bf)
									* (rpt * ifte(bf - sl, w, bf) / sl + 2
											* rpt / sl - s) + rpt / sl - s + 1);
			// return mul(max(add(sub(mul(max(add(sub(s, ifte(sub(max(rpt, 1),
			// max(sl, ttd)), w, bf)), bf), add(add(sub(mul(bf, ifte(sub(
			// 0, max(sl, ttd)), w, bf)), s), sub(max(rpt, 1), max(sl,
			// max(rpt, 1)))), mul(bf, ifte(sub(sub(max(rpt, 1), max(sl,
			// ttd)), sl), w, bf)))), ifte(sub(bf, sl), w, bf)), s), div(
			// rpt, sl)), add(add(sub(mul(
			// add(sub(add(mul(div(rpt, sl), ifte(sub(bf, sl), w, bf)),
			// div(rpt, sl)), s), div(rpt, sl)), mul(bf, ifte(sub(
			// max(rpt, 1), sl), w, bf))), s), div(rpt, sl)), add(
			// sAvg, 1))), mul(bf, ifte(sub(max(rpt, 1), max(sl, max(rpt,
			// 1))), w, bf)));
		}

	}

	public static class GPRuleSize120 extends Base {

		private static final long serialVersionUID = 2413698265966721565L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double rpt = j.remainingProcTime();
			double sAvg = setupAvg();

			return ifte(max(1, rpt) - max(1, rpt, sl), w, bf)
					* bf
					* max(ifte(bf - sl, w, bf)
							* max(-ifte(max(1, rpt) - max(sl, ttd), w, bf) + s
									+ bf,
									bf
											* ifte(max(1, rpt) - max(sl, ttd),
													w, bf) + bf
											* ifte(max(1, rpt) - sl, w, bf) - s
											- max(1, rpt, sl) + max(1, rpt))
							+ rpt / sl - s, bf
							* ifte(max(1, rpt) - sl, w, bf)
							* (sAvg + rpt * ifte(bf - sl, w, bf) / sl + rpt
									/ sl - s + 1) + sAvg + rpt / sl - s + 1);
			// return mul(max(add(sub(mul(max(add(sub(s, ifte(sub(max(rpt, 1),
			// max(sl, ttd)), w, bf)), bf), add(add(sub(mul(bf, ifte(sub(
			// max(rpt, 1), max(sl, ttd)), w, bf)), s), sub(max(rpt, 1),
			// max(sl, max(rpt, 1)))), mul(bf, ifte(sub(max(rpt, 1), sl),
			// w, bf)))), ifte(sub(bf, sl), w, bf)), s), div(rpt, sl)),
			// add(add(sub(mul(add(sub(add(mul(div(rpt, sl), ifte(sub(bf,
			// sl), w, bf)), div(rpt, sl)), s), add(sAvg, 1)),
			// mul(bf, ifte(sub(max(rpt, 1), sl), w, bf))), s),
			// div(rpt, sl)), add(sAvg, 1))), mul(bf, ifte(sub(
			// max(rpt, 1), max(sl, max(rpt, 1))), w, bf)));
		}

	}

	public static class GPRuleSize110 extends Base {

		private static final long serialVersionUID = -632568088038973213L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double s = setupTime(j);
			double rpt = j.remainingProcTime();
			double sAvg = setupAvg();
			double tiq = j.getShop().simTime() - j.getArriveTime();
			double p = j.currProcTime();

			return ifte(
					w + bf - max(1, s),
					max(1, sAvg),
					2
							* w
							- max(ifte(tiq - bf, max(1, s),
									ifte(tiq - bf, max(1, s), bf * tiq)),
									max(1, s) * ttd / rpt)
							- max(ifte(tiq - bf, max(1, s), bf * tiq), ttd
									/ (rpt * (w + p)) + max(1, s)))
					+ w
					+ bf
					- max(ifte(max(1, s) - bf, max(1, s), max(0, sAvg)),
							w + ttd / (max(1, s) * rpt) - bf
									- max(1, s, rpt * (w - bf) / ttd))
					- max(1, s);
			// return sub(add(sub(w, sub(max(1, s), bf)), ifte(sub(w, sub(
			// max(1, s), bf)), max(1, sAvg), sub(add(w, sub(w, max(add(
			// max(1, s), div(div(ttd, rpt), add(w, p))), ifte(
			// sub(tiq, bf), max(1, s), mul(tiq, bf))))), max(div(div(ttd,
			// rpt), div(1, max(1, s))), ifte(sub(tiq, bf), max(1, s),
			// ifte(sub(tiq, bf), max(1, s), mul(tiq, bf))))))), max(add(
			// sub(sub(w, bf), max(div(sub(w, bf), div(ttd, rpt)), max(1,
			// s))), div(div(ttd, rpt), max(1, s))), ifte(sub(max(
			// 1, s), bf), max(1, s), max(sAvg, 0))));
		}

	}

	public static class GPRuleSize99 extends Base {

		private static final long serialVersionUID = -1326856180336964441L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double rpt = j.remainingProcTime();
			double sAvg = setupAvg();

			return ifte(bf - max(1, ttd), w, bf)
					* max(bf * (bf * ifte(bf - sl, w, bf) + bf)
							+ ifte(rpt / sl - max(1, ttd), w, bf) - s
							- max(1, rpt, sl) + max(1, rpt),
							ifte(max(1, rpt) - max(1, rpt, sl), w, bf)
									* bf
									* (sAvg / sl + 3 * rpt / sl - 2 * s + max(
											1, bf - max(1, ttd))) + rpt * w
									/ sl + rpt / sl - s);
			// return mul(max(add(sub(mul(add(sub(add(div(rpt, sl), add(sub(add(
			// div(rpt, sl), div(rpt, sl)), s), div(sAvg, sl))), s), max(
			// 1, sub(bf, max(1, ttd)))), mul(bf, ifte(sub(max(rpt, 1),
			// max(sl, max(rpt, 1))), w, bf))), s), add(mul(div(rpt, sl),
			// w), div(rpt, sl))),
			// add(add(sub(mul(bf, add(mul(bf, ifte(sub(bf, sl), w, bf)),
			// bf)), s), sub(max(rpt, 1), max(sl, max(rpt, 1)))),
			// ifte(sub(div(rpt, sl), max(1, ttd)), w, bf))),
			// ifte(sub(bf, max(1, ttd)), w, bf));
		}
	}

	public static class GPRuleSize98 extends Base {

		private static final long serialVersionUID = 1022237845187168179L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double rpt = j.remainingProcTime();
			double sAvg = setupAvg();

			return ifte(max(1, rpt) - max(1, rpt, sl), w, bf)
					* bf
					* max(rpt
							/ sl
							+ max(-ifte(bf - sl, w, bf) + s + bf, sAvg + bf
									* ifte(max(1, rpt) - max(sl, ttd), w, bf)
									- s - max(1, rpt, sl) + max(1, rpt) + 1)
							* ifte(bf - sl, w, bf) - s,
							sAvg + bf * ifte(max(1, rpt) - sl, w, bf)
									* (2 * rpt / sl - s) + rpt / sl - s + 1);
			// return mul(
			// max(add(sub(mul(max(add(sub(s, ifte(sub(bf, sl), w, bf)),
			// bf), add(add(sub(mul(bf, ifte(sub(max(rpt, 1), max(
			// sl, ttd)), w, bf)), s), sub(max(rpt, 1), max(sl,
			// max(rpt, 1)))), add(sAvg, 1))), ifte(sub(bf, sl),
			// w, bf)), s), div(rpt, sl)), add(add(sub(mul(add(
			// sub(div(rpt, sl), s), div(rpt, sl)), mul(bf, ifte(
			// sub(max(rpt, 1), sl), w, bf))), s), div(rpt, sl)),
			// add(sAvg, 1))),
			// mul(bf, ifte(sub(max(rpt, 1), max(sl, max(rpt, 1))), w, bf)));
		}
	}

	public static class GPRuleSize43 extends Base {

		private static final long serialVersionUID = 7446440495423740022L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double s = setupTime(j);
			double rpt = j.remainingProcTime();
			double sAvg = setupAvg();

			return w
					+ bf
					- max(max(1, s) - bf,
							w + ttd / rpt - bf
									- max(1, s, rpt * (w - bf) / ttd)
									- max(1, 1 / bf)) - max(1, s)
					+ max(0, sAvg);
			// return sub(add(sub(w, sub(max(1, s), bf)), max(0, sAvg)),
			// max(add(
			// sub(sub(sub(w, bf), max(div(sub(w, bf), div(ttd, rpt)),
			// max(1, s))), max(div(1, bf), 1)), div(ttd, rpt)),
			// sub(max(1, s), bf)));
		}
	}

	public static class GPRuleSize33 extends Base {

		private static final long serialVersionUID = 876197137980072205L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double s = setupTime(j);
			double rpt = j.remainingProcTime();

			return w
					+ bf
					- max(1,
							s,
							w + ttd / (max(1, s) * rpt) - bf
									- max(1, s, rpt * (w - bf) / ttd)) - 1;
			// return sub(sub(w, sub(1, bf)), max(add(sub(sub(w, bf),
			// max(div(sub(
			// w, bf), div(ttd, rpt)), max(1, s))), div(div(ttd, rpt),
			// max(1, s))), max(s, 1)));
		}
	}

	public static class GPRuleSize20 extends Base {

		private static final long serialVersionUID = 8213003540581331069L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double rpt = j.remainingProcTime();

			return rpt / sl + bf * ifte(max(1, rpt) - max(rpt / sl, sl), w, bf)
					- s;
			// return add(sub(mul(bf, ifte(
			// sub(max(rpt, 1), max(sl, div(rpt, sl))), w, bf)), s), div(
			// rpt, sl));
		}
	}

	public static class GPRuleSize16 extends Base {

		private static final long serialVersionUID = 6863799400796818222L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double ttd = j.getDueDate() - j.getShop().simTime();
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double rpt = j.remainingProcTime();

			return rpt / sl - s + bf * ifte(rpt - max(1, ttd), w, bf);
			// return add(sub(mul(bf, ifte(sub(rpt, max(1, ttd)), w, bf)), s),
			// div(rpt, sl));
		}
	}

	public static class GPRuleSize12 extends Base {

		private static final long serialVersionUID = 1377669824905528525L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);

			return w / sl + ifte(sl, bf, w) - s + bf;
			// return add(div(w, sl), add(ifte(sl, bf, w), sub(bf, s)));
		}
	}

	public static class GPRuleSize09 extends Base {

		private static final long serialVersionUID = 2363205113298193239L;

		@Override
		public double calcPrio(PrioRuleTarget j) {
			double bf = numCompatible(j);
			double w = j.getWeight();
			double sl = SLK.slack(j);
			double s = setupTime(j);
			double pAvg = procAvg(j);

			return w / max(sl, pAvg) - s + bf;
			// return add(sub(bf, s), div(w, max(pAvg, sl)));
		}
	}

}
