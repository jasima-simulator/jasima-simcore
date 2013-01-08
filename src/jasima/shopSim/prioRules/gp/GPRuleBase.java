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
package jasima.shopSim.prioRules.gp;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
public abstract class GPRuleBase extends PR {

	@Override
	public abstract double calcPrio(PrioRuleTarget j);

	public static final double ifte(final double cond, final double ifVal,
			final double elseVal) {
		if (cond >= 0.0d)
			return ifVal;
		else
			return elseVal;
	}

	public static final double add(final double v1, final double v2) {
		return v1 + v2;
	}

	public static final double mul(final double v1, final double v2) {
		return v1 * v2;
	}

	public static final double div(final double v1, final double v2) {
		return v1 / v2;
	}

	public static final double sub(final double v1, final double v2) {
		return v1 - v2;
	}

	public static final double max(final double v1, final double v2) {
		return Math.max(v1, v2);
	}

	public static final double min(final double v1, final double v2) {
		return Math.min(v1, v2);
	}

	public static final double neg(final double v1) {
		return -v1;
	}
}
