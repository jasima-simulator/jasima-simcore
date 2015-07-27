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
