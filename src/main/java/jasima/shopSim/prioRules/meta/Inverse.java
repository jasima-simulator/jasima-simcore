/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
