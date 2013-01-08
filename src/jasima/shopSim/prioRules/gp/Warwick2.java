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
package jasima.shopSim.prioRules.gp;

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class Warwick2 extends GPRuleBase {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double p = j.getCurrentOperation().procTime;
		double winq = jasima.shopSim.prioRules.upDownStream.WINQ.winq(j);
		double winq2 = jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(j);
		double tiq = j.getShop().simTime() - j.getArriveTime();
		double npt = PTPlusWINQPlusNPT.npt(j);
		double tis = j.getShop().simTime() - j.getRelDate();
		double odd = j.getCurrentOperationDueDate() - j.getShop().simTime();

		return div(
				div(div(mul(
						tiq,
						ifte(tiq,
								div(div(div(
										div(mul(tiq,
												mul(tiq,
														ifte(tiq,
																div(div(max(
																		tis,
																		mul(p,
																				p)),
																		tiq), p),
																max(npt, winq)))),
												p), max(0, tiq)), tiq), p),
								max(p, winq2))),
						max(sub(div(
								mul(tiq,
										ifte(tiq,
												div(max(mul(winq2, npt),
														max(mul(p, p),
																div(1, p))), p),
												max(add(1, max(tis, odd)), 1))),
								tiq), max(tiq, 0)),
								max(mul(max(
										mul(winq2, npt),
										max(mul(p, p),
												div(div(div(div(tiq, p),
														max(p, winq2)),
														max(div(ifte(
																tiq,
																div(div(tiq, p),
																		max(p,
																				winq2)),
																add(p, p)), p),
																tiq)), p))), p),
										1))),
						max(mul(winq2, npt),
								max(add(p, div(ifte(tiq, 0, add(p, p)), p)),
										odd))), p);
	}

}
