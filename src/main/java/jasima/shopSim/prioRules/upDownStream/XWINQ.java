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
package jasima.shopSim.prioRules.upDownStream;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;

/**
 * The class implements the Extended Least Work content In Next Queue rule, see
 * Haupt (1989). The work content of all jobs waiting or arriving soon at the
 * work centre which a job visits for its next operation defines the priority of
 * a job. For its last operation, the work content in the next queue of a job is
 * zero.
 * <p>
 * The lookahead needs to be enabled in the simulation for this rule to work
 * properly.
 * 
 * @author Torsten Hildebrandt
 */
public class XWINQ extends PR {

	private static final long serialVersionUID = -83039692160872143L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		return -xwinq(job);
	}

	/**
	 * Computes the work in next queue of the jobs currently waiting in front of
	 * the next machine <em>plus</em> future jobs.
	 */
	public static double xwinq(PrioRuleTarget job) {
		int nextTask = job.getTaskNumber() + 1;
		if (nextTask >= job.getOps().length)
			return 0;

		WorkStation mNext = job.getOps()[nextTask].getMachine();
		return mNext.workContent(true);
	}

}
