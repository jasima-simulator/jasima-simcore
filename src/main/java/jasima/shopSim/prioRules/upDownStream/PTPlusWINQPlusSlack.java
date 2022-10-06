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
