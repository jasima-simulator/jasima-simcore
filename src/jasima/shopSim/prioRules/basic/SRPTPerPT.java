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
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Remaining Processing Time per Imminent Processing
 * Time rule. This is rule 10 in Blackwater et al. (1982)
 * "A state-of-the-art survey of dispatching rules for job shop operations". It
 * seems to perform well especially for the makespan objective.
 * 
 * @author Torsten Hildebrandt, 2011-12-01
 * @version $Id$
 */
public class SRPTPerPT extends PR {

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return job.remainingProcTime() / job.getCurrentOperation().procTime;
	}

}
