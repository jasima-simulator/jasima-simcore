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

import java.util.HashMap;
import java.util.Map;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Minimum Marginal Setup time rule, developed by Arzi
 * and Raviv (1998), which assigns highest priority to all jobs that belong to
 * the family with the shortest setup time per job, see also Pickardt and Branke
 * (2012).
 * <p>
 * This rule should be used with another rule to distinguish jobs of the
 * selected family.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class MMS extends PR {

	private static final long serialVersionUID = -4646210941243911341L;

	protected Map<String, Integer> jobsPerFamily = new HashMap<String, Integer>();

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		jobsPerFamily.clear();
		assert jobsPerFamily.isEmpty();
		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);

			if (arrivesTooLate(j))
				continue;

			String sf = "" + j.getCurrentOperation().getSetupState();
			if (jobsPerFamily.get(sf) == null) {
				jobsPerFamily.put(sf, 1);
			} else {
				int familyValue = jobsPerFamily.get(sf) + 1;
				jobsPerFamily.put(sf, familyValue);
			}
		}

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		double marginalSetup = getOwner().getSetupMatrix()[getOwner().currMachine.setupState][j.getCurrentOperation()
				.getSetupState()] / jobsPerFamily.get("" + j.getCurrentOperation().getSetupState());

		return -marginalSetup;
	}

}
