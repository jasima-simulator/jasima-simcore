/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.prioRules.gp;

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * A rule from "Towards Improved Dispatching Rules for Complex Shop Floor
 * Scenarios—a Genetic Programming Approach", Hildebrandt, Heger, Scholz-Reiter,
 * GECCO 2010, doi:10.1145/1830483.1830530
 * 
 * @author Torsten Hildebrandt
 */
public class GECCO2010_genSeed_2reps extends GPRuleBase {

	private static final long serialVersionUID = -6972361592426110350L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double p = j.currProcTime();
		double winq = jasima.shopSim.prioRules.upDownStream.WINQ.winq(j);
		double tiq = j.getShop().simTime() - j.getArriveTime();
		double npt = PTPlusWINQPlusNPT.npt(j);
		double tis = j.getShop().simTime() - j.getRelDate();
		double rpt = j.remainingProcTime();

		return max(winq, ifte(
				(2 * p * tiq / (winq + 2 * p) + max(p, winq))
						/ (ifte(ifte(winq - tis, 2 - p, tis + p) + winq - rpt, npt * (winq + 2 * p) / (2 * p) + p,
								ifte(winq - tis, -winq + rpt + 2 * p, max(tis + p, winq))) + winq + max(p, winq))
				+ max(2 * p, 6 * (p * p)),
				npt * (winq + p) * (winq + max(p, winq) + 1) / (p * (rpt + p)),
				tis * (((winq + p) * (winq + max(p, winq) + 1) / (rpt + p) + max(p, winq))
						* max(winq / tis, (1 - p) * (winq + p) / max(p, winq)) + p) / tiq)
				+ ifte(p - winq, npt * (winq + 2 * p) + rpt + p, max(tis, ifte(winq - tis, 1 - winq, tis + p) + winq)))
				- npt * (winq / tis + winq) - max(npt, 2 * p * (2 * p + 1));
	}

}
