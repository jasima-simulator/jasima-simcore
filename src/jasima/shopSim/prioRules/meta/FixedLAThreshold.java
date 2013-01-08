/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
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
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.prioRules.meta;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements a rule that assigns a lower priority to any lookahead
 * job arriving later than a given threshold value.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version $Id$
 */
public class FixedLAThreshold extends LookaheadThreshold {

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
