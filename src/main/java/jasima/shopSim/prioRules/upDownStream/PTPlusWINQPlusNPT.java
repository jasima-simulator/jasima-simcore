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

/**
 * This class implements the 2PT+WINQ+NPT rule, developed by Holthaus and
 * Rajendran (2000), DOI: 10.1080/095372800232379, which extends PT+WINQ by
 * taking into account the processing time of a job's next operation.
 * 
 * @author Christoph Pickardt, 2011-11-15
 */
public class PTPlusWINQPlusNPT extends PR {

	private static final long serialVersionUID = 249563235097986052L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(2 * j.currProcTime() + WINQ.winq(j) + npt(j));
	}

	/**
	 * Next Processing Time of a job, i.e. processing time of next operation.
	 */
	public static double npt(PrioRuleTarget job) {
		int nextTask = job.getTaskNumber() + 1;
		if (nextTask >= job.getOps().length)
			return 0.0d;
		else
			return job.getOps()[nextTask].getProcTime();
	}

}
