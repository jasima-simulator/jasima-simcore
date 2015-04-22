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
package jasima.shopSim.prioRules.meta;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements a method to distinguish lookahead jobs that arrive
 * later than a given threshold value.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version 
 *          "$Id$"
 */
public abstract class LookaheadThreshold extends PR {

	private static final long serialVersionUID = 9004100743968954317L;

	@Override
	public double calcPrio(PrioRuleTarget t) {
		return arrivesTooLate(t) ? -1 : +1;
	}

	@Override
	public abstract boolean arrivesTooLate(PrioRuleTarget prt);

	public boolean arrivesTooLate(PrioRuleTarget prt, double maxWait) {
		if (prt.isFuture()) {
			double arrivesIn = prt.getArriveTime() - prt.getShop().simTime();
			assert arrivesIn >= 0.0d;

			return (arrivesIn > maxWait);
		} else
			return false;
	}

}
