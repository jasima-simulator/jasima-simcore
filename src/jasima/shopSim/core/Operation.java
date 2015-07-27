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

/**
 * Operations specify what to process.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id: Operation.java 550 2015-01-23 15:07:23Z thildebrandt@gmail.com$"
 */
public class Operation implements Cloneable {

	public WorkStation machine;
	public double procTime = Double.NaN;
	public int setupState = WorkStation.DEF_SETUP;
	public String batchFamily = WorkStation.BATCH_INCOMPATIBLE;
	public int maxBatchSize = 1;

	@Override
	public Operation clone() throws CloneNotSupportedException {
		return (Operation) super.clone();
	}

}
