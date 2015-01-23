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
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * Negates the value of the base rule. This way FCFS becomes LCFS, Shortest
 * Processing Time first becomes Longest Processing Time first etc.
 * <p>
 * 
 * @author Torsten Hildebrandt
 * @version $Id$
 */
public class Inverse extends PR {

	private PR base;

	public Inverse(PR baseRule) {
		super();
		base = baseRule;
		if (baseRule.getTieBreaker() != null)
			throw new IllegalArgumentException(
					"baseRule can't have a tie breaker. This has to be set as a tie breaker of InverseRule.");
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -base.calcPrio(j);
	}

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		base.beforeCalc(q);
	}

	@Override
	public String getName() {
		return "INV_" + String.valueOf(base);
	}

	@Override
	public PR clone() throws CloneNotSupportedException {
		Inverse c = (Inverse) super.clone();
		if (base != null)
			c.base = base.silentClone();
		return c;
	}

	@Override
	public void setOwner(WorkStation o) {
		super.setOwner(o);
		if (base != null)
			base.setOwner(o);
	}

}
