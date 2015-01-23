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
 * This rule implements the "remaining processing time per slack" rule.
 * This is rule 17 (S/WKR, the smallest ratio of slack per work remaining) in
 * Haupt (1989): "A Survey of Priority Rule-Based Scheduling".
 * 
 * @author Torsten Hildebrandt, 2013-08-10
 * @version "$Id$"
 */
public class SRPTPerSLK extends PR {

	public SRPTPerSLK() {
		super();
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return job.remainingProcTime() / SLK.slack(job);
	}

}
