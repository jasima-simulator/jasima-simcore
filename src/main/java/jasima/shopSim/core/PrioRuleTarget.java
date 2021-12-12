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
package jasima.shopSim.core;

import jasima.core.util.ValueStore;

/**
 * Common abstract base class for {@link Job}s and {@link Batch}es.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class PrioRuleTarget implements ValueStore {

	public PrioRuleTarget() {
		super();
	}

	public abstract boolean isFuture();

	public abstract Operation getCurrentOperation();

	public abstract WorkStation getCurrMachine();

	public abstract Shop getShop();

	public abstract int getTaskNumber();

	public abstract int numOps();

	public abstract Operation[] getOps();

	public abstract double getArriveTime();

	public abstract double remainingProcTime();

	public abstract double getDueDate();

	public abstract double getWeight();

	public abstract double currProcTime();

	public abstract double getRelDate();

	public abstract int getJobNum();

	public abstract double getCurrentOperationDueDate();

	public abstract double procSum();

	public abstract int numOpsLeft();

	public abstract int numJobsInBatch();

	public abstract Job job(int i);

	public abstract boolean isBatch();

	public abstract String getName();
}
