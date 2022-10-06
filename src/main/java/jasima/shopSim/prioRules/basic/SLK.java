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
 * This rule implements the Least (global) Slack rule.
 * 
 * @author Torsten Hildebrandt
 */
public class SLK extends PR {

	private static final long serialVersionUID = 4022695033610482583L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -slack(job);
	}

	public static final double slack(PrioRuleTarget j) {
		return j.getDueDate() - j.getShop().simTime() - j.remainingProcTime();
	}
}
