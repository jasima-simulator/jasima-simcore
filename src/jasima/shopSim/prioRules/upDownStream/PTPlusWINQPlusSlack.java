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
package jasima.shopSim.prioRules.upDownStream;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the PT+WINQ+SL rule, developed by Rajendran and
 * Holthaus (1999), which extends PT+WINQ by taking into account the slack of a
 * job.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class PTPlusWINQPlusSlack extends PR {

	private static final long serialVersionUID = -7220017849004140341L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(j.currProcTime() + WINQ.winq(j)
				+ Math.min(j.getDueDate() - j.getShop().simTime() - j.remainingProcTime(), 0.0d));
	}

}
