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
package jasima.shopSim.prioRules.meta;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements a rule that assigns a lower priority to any lookahead
 * job arriving later than a given threshold value.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version 
 *          "$Id$"
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
			throw new IllegalArgumentException("maxWait " + maxWait
					+ " can not be negative!");

		this.maxWait = maxWait;
	}

}
