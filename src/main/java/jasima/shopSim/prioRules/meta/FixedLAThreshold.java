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
 * This class implements a rule that assigns a lower priority to any lookahead
 * job arriving later than a given threshold value.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class FixedLAThreshold extends LookaheadThreshold {

	private static final long serialVersionUID = 4971777067231682191L;

	private double maxWait;

	public FixedLAThreshold(PR baseRule, double maxWait) {
		super();

		setMaxWait(maxWait);
		setTieBreaker(baseRule);
	}

	public FixedLAThreshold(PR baseRule) {
		this(baseRule, 0.0);
	}

	@Override
	public boolean arrivesTooLate(PrioRuleTarget prt) {
		return arrivesTooLate(prt, getMaxWait());
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
		return "FLA(" + getMaxWait() + ")";
	}

	public double getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(double maxWait) {
		if (maxWait < 0.0)
			throw new IllegalArgumentException("maxWait " + maxWait + " can not be negative!");

		this.maxWait = maxWait;
	}

}
