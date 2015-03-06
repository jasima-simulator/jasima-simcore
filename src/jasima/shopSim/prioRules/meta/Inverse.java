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
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class Inverse extends PR {

	private static final long serialVersionUID = -162829434910106115L;

	private PR base;

	public Inverse() {
		this(null);
	}

	public Inverse(PR baseRule) {
		super();
		setBaseRule(baseRule);
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -getBaseRule().calcPrio(j);
	}

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		getBaseRule().beforeCalc(q);
	}

	@Override
	public String getName() {
		return "INV_" + String.valueOf(getBaseRule());
	}

	@Override
	public PR clone() throws CloneNotSupportedException {
		Inverse c = (Inverse) super.clone();
		if (getBaseRule() != null)
			c.setBaseRule(getBaseRule().silentClone());
		return c;
	}

	@Override
	public void setOwner(WorkStation o) {
		super.setOwner(o);
		if (getBaseRule() != null)
			getBaseRule().setOwner(o);
	}

	public PR getBaseRule() {
		return base;
	}

	public void setBaseRule(PR base) {
		if (base != null) {
			if (base.getTieBreaker() != null)
				throw new IllegalArgumentException(
						"baseRule can't have a tie breaker. This has to be set as a tie breaker of InverseRule.");
			base.setOwner(getOwner());
		}
		this.base = base;
	}

}
