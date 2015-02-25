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
 * @version 
 *          "$Id$"
 */
public class TieBreakerFASFS extends PR {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		// all future jobs have lower priority than regular ones
		if (j.isFuture())
			return -j.getJobNum();
		else
			return 1.0 / (j.getJobNum() + 1);
	}

}
