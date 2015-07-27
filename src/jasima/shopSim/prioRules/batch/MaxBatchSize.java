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

import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the Largest Relative Batch rule, which chooses the
 * batch that uses most of the available capacity of the machine.
 * 
 * @author Christoph Pickardt, 2011-11-14
 * @version 
 *          "$Id$"
 */
public class MaxBatchSize extends PR {

	private static final long serialVersionUID = -689042486920247381L;

	@Override
	public double calcPrio(PrioRuleTarget b) {
		Operation o = b.getCurrentOperation();
		return ((double) b.numJobsInBatch() / o.maxBatchSize);
	}

}
