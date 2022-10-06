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

/**
 * This class implements a method to distinguish lookahead jobs that arrive
 * later than a given threshold value.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public abstract class LookaheadThreshold extends PR {

	private static final long serialVersionUID = 9004100743968954317L;

	@Override
	public double calcPrio(PrioRuleTarget t) {
		return arrivesTooLate(t) ? -1 : +1;
	}

	@Override
	public abstract boolean arrivesTooLate(PrioRuleTarget prt);

	public boolean arrivesTooLate(PrioRuleTarget prt, double maxWait) {
		if (prt.isFuture()) {
			double arrivesIn = prt.getArriveTime() - prt.getShop().simTime();
			assert arrivesIn >= 0.0d;

			return (arrivesIn > maxWait);
		} else
			return false;
	}

}
