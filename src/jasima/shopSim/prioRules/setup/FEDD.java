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
package jasima.shopSim.prioRules.setup;

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Family Earliest Due Date rule, developed by
 * Mahmoodi et al. (1990), which assigns highest priority to all jobs that
 * belong to the family which includes the job with the earliest due date, see
 * also Pickardt and Branke (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version "$Id$"
 */
public class FEDD extends PR {

	private static final long serialVersionUID = -1900010822908956309L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;
		return -(earliestFamilyDueDate(j));
	}

	public double earliestFamilyDueDate(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().setupState;
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double dd = Double.POSITIVE_INFINITY;
		for (int i = 0; i < q.size(); i++) {
			Job job = q.get(i);
			if (arrivesTooLate(job))
				continue;
			if (job.getCurrentOperation().setupState == family
					&& job.getDueDate() < dd) {
				dd = job.getDueDate();
			}
		}
		assert dd != Double.POSITIVE_INFINITY;
		return dd;
	}

}
