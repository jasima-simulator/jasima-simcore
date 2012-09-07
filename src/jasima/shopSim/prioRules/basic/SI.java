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
 * @version $Id$
 */
public class SI extends PR {

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
