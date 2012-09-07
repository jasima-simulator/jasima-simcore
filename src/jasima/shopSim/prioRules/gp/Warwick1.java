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
package jasima.shopSim.prioRules.gp;

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class Warwick1 extends GPRuleBase {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double p = j.getCurrentOperation().procTime;
		double winq = jasima.shopSim.prioRules.upDownStream.WINQ.winq(j);
		double winq2 = jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(j);
		double tiq = j.getShop().simTime() - j.getArriveTime();
		double npt = PTPlusWINQPlusNPT.npt(j);
		double rpt = j.remainingProcTime();

		return div(
				div(ifte(
						div(tiq, p),
						div(tiq,
								add(max(mul(p, p), add(1, div(tiq, p))),
										mul(winq2, npt))), div(tiq, p)),
						max(tiq,
								div(ifte(
										add(max(mul(winq2, npt), sub(winq, rpt)),
												winq),
										div(max(add(1, winq), max(0, tiq)),
												add(mul(p, p), winq)),
										div(tiq, p)), add(1, 1)))), p);
	}

}
