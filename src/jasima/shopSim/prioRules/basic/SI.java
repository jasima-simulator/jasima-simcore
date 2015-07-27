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
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This rule implements a truncated version of SPT, often referred to as SI^x,
 * see e.g. Blackstone (1982). The SI^x rule first distinguishes between jobs
 * with positive and negative slack with the latter group receiving priority.
 * Jobs within a group are sequenced according to SPT.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version "$Id$"
 */
public class SI extends PR {

	private static final long serialVersionUID = 8008779098412019655L;

	public SI() {
		super();
		super.setTieBreaker(new SPT());
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if ((j.getDueDate() - j.getShop().simTime() - j.remainingProcTime()) <= 0)
			return +1;
		else
			return -1;
	}

	@Override
	public void setTieBreaker(PR tieBreaker) {
		if (getTieBreaker() != null) {
			getTieBreaker().setTieBreaker(tieBreaker);
		} else
			super.setTieBreaker(tieBreaker);
	}

	@Override
	public String getName() {
		return "SI";
	}

}
