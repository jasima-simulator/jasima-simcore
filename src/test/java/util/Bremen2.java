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
package util;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * 
 * @author Torsten Hildebrandt
 */
public class Bremen2 extends PR {

	private static final long serialVersionUID = -2132369816967616199L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (j.isFuture())
			return PriorityQueue.MIN_PRIO;

		double p = j.currProcTime();
		double winq = jasima.shopSim.prioRules.upDownStream.WINQ.winq(j);
		double npt = PTPlusWINQPlusNPT.npt(j);

		return -p * (p + winq) * (p + npt);
	}

}
