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

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * A rule from "Towards Improved Dispatching Rules for Complex Shop Floor
 * Scenarios—a Genetic Programming Approach", Hildebrandt, Heger, Scholz-Reiter,
 * GECCO 2010, doi:10.1145/1830483.1830530
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class GECCO2010_lookahead extends GPRuleBase {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double p = j.getCurrentOperation().procTime;
		double winq2 = jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(j);
		double tiq = j.getShop().simTime() - j.getArriveTime();
		double npt = PTPlusWINQPlusNPT.npt(j);
		double ol = j.numOpsLeft();

		return p
				* (ifte(tiq
						* (p / ((tiq + npt - 1) * (winq2 + npt)) + winq2)
						/ ifte(ifte(ol * p / (tiq * winq2), 1 / p, 2 * npt - ol
								/ ((p + npt) * tiq)), 1 / p, 1), 1 / p, tiq - 1)
						/ (winq2 + (p * p)
								/ ((1 - 2 * npt) / (2 * p + npt) + 2 * npt)) + 1 / (p * (p + npt))

				);
	}

}
