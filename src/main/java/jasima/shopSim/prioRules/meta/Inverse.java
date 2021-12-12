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
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * Negates the value of the base rule. This way FCFS becomes LCFS, Shortest
 * Processing Time first becomes Longest Processing Time first etc.
 * 
 * @author Torsten Hildebrandt
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
	public PR clone() {
		Inverse c = (Inverse) super.clone();

		if (getBaseRule() != null)
			c.setBaseRule(getBaseRule().clone());
		return c;
	}

	@Override
	public PR setOwner(WorkStation o) {
		super.setOwner(o);
		if (getBaseRule() != null)
			getBaseRule().setOwner(o);
		return this;
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
