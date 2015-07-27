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
package jasima.shopSim.prioRules.batch;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;

/**
 * This class implements the Largest Batch Family rule, which chooses the job
 * for which most other jobs of the same batch family are waiting in the queue.
 * 
 * @author Christoph Pickardt, 2011-11-14
 * @version "$Id$"
 */
public class LBF extends PR {

	private static final long serialVersionUID = 2198657617441057488L;

	public LBF() {
		super();
	}

	@Override
	public double calcPrio(PrioRuleTarget j) {
		if (arrivesTooLate(j))
			return PriorityQueue.MIN_PRIO;

		final String family = j.getCurrentOperation().batchFamily;
		if (WorkStation.BATCH_INCOMPATIBLE.equals(family))
			return 1;

		int res = 0;
		for (int i = 0; i < j.getCurrMachine().getJobsByFamily().get(family)
				.size(); i++) {
			if (!arrivesTooLate(j.getCurrMachine().getJobsByFamily()
					.get(family).get(i)))
				res++;
		}
		assert res >= 1;

		return res;
	}

	@Override
	public String getName() {
		return "LBF";
	}

}
