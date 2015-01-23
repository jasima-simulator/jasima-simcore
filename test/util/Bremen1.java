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

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.prioRules.gp.GPRuleBase;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class Bremen1 extends GPRuleBase {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (j.isFuture())
			return PriorityQueue.MIN_PRIO;

		double p = j.getCurrentOperation().procTime;
		double winq = jasima.shopSim.prioRules.upDownStream.WINQ.winq(j);
		double tiq = j.getShop().simTime() - j.getArriveTime();
		double npt = PTPlusWINQPlusNPT.npt(j);
		double tis = j.getShop().simTime() - j.getRelDate();
		double ol = j.numOpsLeft();

		return -(p * (npt - npt / p) * winq //
		+ p * (max(p, ol - tiq) * (max(p - npt, p / (tis + p)) + 1) + 1) //
		);
		// p vor die Klammer zu ziehen, gibt leider wieder minimal schlechtere
		// Ergebnisse
		// return - p * ((npt-npt/p) * winq
		// + ( max(p, ol - tiq) * ( max(p - npt, p / (tis + p)) + 1) + 1 ));
	}

}
