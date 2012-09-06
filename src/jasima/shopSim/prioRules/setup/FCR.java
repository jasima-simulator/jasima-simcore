/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.shopSim.prioRules.setup;

import jasima.shopSim.core.Job;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Family Critical Ratio rule, developed by Kim and
 * Bobrowski (1994), which assigns highest priority to all jobs that belong to
 * the family which includes the job with the lowest critical ratio, see also
 * Pickardt and Branke (2012).
 * <p />
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class FCR extends PR {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;
		return -(earliestFamilyCR(j));
	}

	public double earliestFamilyCR(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().setupState;
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double cr = Double.POSITIVE_INFINITY;
		for (int i = 0; i < q.size(); i++) {
			Job job = q.get(i);
			if (arrivesTooLate(job))
				continue;
			if (job.getCurrentOperation().setupState == family) {
				double procRem = job.remainingProcTime();
				double jobcr;

				jobcr = (job.getDueDate() - job.getShop().simTime()) / procRem;

				if (jobcr < cr)
					cr = jobcr;
			}
		}
		assert cr != Double.POSITIVE_INFINITY;
		return cr;
	}

}
