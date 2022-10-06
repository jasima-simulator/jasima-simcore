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
 * This class implements a rule that sequences job in increasing order of their
 * job number. As this attribute strongly correlates to the time of arrival, the
 * rule operates similarly to {@link FASFS}. However, there are subtle
 * differences between the two implementations. With the FASFS rule, jobs can
 * have equal priorities if they arrived at exactly the same instant. Therefore,
 * this rule is preferred as a final tie-breaker as it ensures a deterministic
 * order.
 * 
 * @author Torsten Hildebrandt
 * @see FASFS
 */
public class TieBreakerFASFS extends PR {

	private static final long serialVersionUID = 2648534592090568569L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		// all future jobs have lower priority than regular ones
		if (j.isFuture())
			return -j.getJobNum();
		else
			return 1.0 / (j.getJobNum() + 1);
	}

}
