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
package jasima.shopSim.core;

public abstract class PrioRuleTarget {
	public abstract boolean isFuture();

	public abstract Operation getCurrentOperation();

	public abstract WorkStation getCurrMachine();

	public abstract JobShop getShop();

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

	public abstract void proceed();

	public abstract boolean isBatch();

	public abstract String getName();
}
