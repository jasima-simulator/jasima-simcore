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
package jasima.shopSim.prioRules.setup;

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Family First Come First Served rule, developed by
 * Flynn (1987), which assigns highest priority to all jobs that belong to the
 * family which includes the job that arrived first to the queue, see also
 * Pickardt and Branke (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class FFCFS extends PR {

	private static final long serialVersionUID = -9158038750813584890L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(earliestFamilyArrival(j));
	}

	public double earliestFamilyArrival(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().getSetupState();
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double at = Double.POSITIVE_INFINITY;
		for (int i = 0; i < q.size(); i++) {
			Job job = q.get(i);
			if (job.getCurrentOperation().getSetupState() == family && job.getArriveTime() < at) {
				at = job.getArriveTime();
			}
		}
		assert at != Double.POSITIVE_INFINITY;
		return at;
	}

}
