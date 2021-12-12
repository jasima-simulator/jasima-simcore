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

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;

/**
 * This class implements the Shortest Normalized Setup and Processing Time rule,
 * see Pickardt and Branke (2012).
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class SNSPT extends PR {

	private static final long serialVersionUID = 3765192665858565459L;

	protected double[][] setupMatrix;

	protected double procNorm;
	protected double setupNorm;

	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		if (setupMatrix == null)
			setupMatrix = getOwner().getSetupMatrix();

		int numJobs = 0;
		procNorm = 0.0d;
		setupNorm = 0.0d;

		for (int i = 0; i < q.size(); i++) {
			PrioRuleTarget j = q.get(i);
			if (arrivesTooLate(j))
				continue;

			procNorm += j.currProcTime();
			setupNorm += setupMatrix[getOwner().currMachine.setupState][j.getCurrentOperation().getSetupState()];
			numJobs++;
		}
		procNorm = (procNorm / numJobs);
		setupNorm = (setupNorm / numJobs);

		super.beforeCalc(q);
	}

	@Override
	public double calcPrio(PrioRuleTarget job) {
		if (arrivesTooLate(job))
			return PriorityQueue.MIN_PRIO;

		double sRatio = setupNorm != 0.0
				? setupMatrix[getOwner().currMachine.setupState][job.getCurrentOperation().getSetupState()] / setupNorm
				: 0.0;
		return -(sRatio + (job.currProcTime() / procNorm));
	}

}
