/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.prioRules.batch;

import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Largest Relative Batch rule, which chooses the
 * batch that uses most of the available capacity of the machine.
 * 
 * @author Christoph Pickardt, 2011-11-14
 * @version "$Id$"
 */
public class MaxBatchSize extends PR {

	@Override
	public double calcPrio(PrioRuleTarget b) {
		Operation o = b.getCurrentOperation();
		return ((double) b.numJobsInBatch() / o.maxBatchSize);
	}

}
