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
package jasima.shopSim.prioRules.meta;

import jasima.shopSim.core.PR;

/**
 * Helper class to use a rule which is designed to assign a low priority to
 * future jobs when the lookahead is enabled, thereby generating non delay
 * schedules. Such a non delay rule has to be set the tie breaker rule of this
 * class.
 * <p>
 * IgnoreFuturedRule assigns each future job a priority of -1 and each "normal"
 * job a priority of +1, therefore the tie breaker rule is used to select one of
 * the non-future jobs.
 * 
 * @author Torsten Hildebrandt, 2010-02-16
 * @version "$Id$"
 */
public class IgnoreFutureJobs extends FixedLAThreshold {

	public IgnoreFutureJobs(PR baseRule) {
		super(baseRule, 0.0);
	}

	@Override
	public String getName() {
		return "IGF";
	}

}
