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
