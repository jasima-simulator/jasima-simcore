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

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Weighted Batch Processing Time rule, developed by
 * Raman et al. (1989), which is an additive combination of MMS and SPT, see
 * also Pickardt and Branke (2012).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class WBPT extends MMS {

	private static final long serialVersionUID = 8461106077678169718L;

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (arrivesTooLate(job))
			return PriorityQueue.MIN_PRIO;

		double marginalSetup = getOwner().getSetupMatrix()[getOwner().currMachine.setupState][job.getCurrentOperation()
				.getSetupState()] / jobsPerFamily.get("" + job.getCurrentOperation().getSetupState());

		return -(marginalSetup + job.currProcTime());
	}

}
