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
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the Minimum Average Setup plus Processing time rule,
 * developed by Russell and Philipoom (1991), which assigns highest priority to
 * all jobs that belong to the family with the shortest processing time per job,
 * see also Pickardt and Branke (2012).
 * <p />
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version $Id$
 */
public class MASP extends MMS {

	protected Map<String, Double> sumProcTimePerFamily = new HashMap<String, Double>();

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		sumProcTimePerFamily.clear();
		assert sumProcTimePerFamily.isEmpty();
		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);

			if (arrivesTooLate(j))
				continue;

			String sf = "" + j.getCurrentOperation().setupState;
			if (sumProcTimePerFamily.get(sf) == null) {
				sumProcTimePerFamily.put(sf, j.getCurrentOperation().procTime);
			} else {
				double familyValue = sumProcTimePerFamily.get(sf)
						+ j.getCurrentOperation().procTime;
				sumProcTimePerFamily.put(sf, familyValue);
			}
		}

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		double setup = getOwner().getSetupMatrix()[getOwner().currMachine.setupState][j
				.getCurrentOperation().setupState];
		String family = "" + j.getCurrentOperation().setupState;

		return -((setup + sumProcTimePerFamily.get(family)))
				/ jobsPerFamily.get(family);
	}

	public double sumFamilyProcessingTime(PrioRuleTarget j) {
		final int family = j.getCurrentOperation().setupState;
		final PriorityQueue<Job> q = j.getCurrMachine().queue;
		assert q.size() > 0;

		double procTime = 0;
		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget job = q.get(i);
			if (arrivesTooLate(job))
				continue;
			if (job.getCurrentOperation().setupState == family) {
				procTime = procTime + job.getCurrentOperation().procTime;
			}
		}
		return procTime;
	}

}
