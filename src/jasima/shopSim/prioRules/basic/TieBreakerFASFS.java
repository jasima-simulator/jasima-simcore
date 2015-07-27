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
