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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple container for Operations.
 */
public class Route {

	private Operation[] operations;

	public Route() {
		super();
		operations = new Operation[0];
	}

	public Operation operation(int i) {
		return operations[i];
	}

	public void addSequentialOperation(Operation op) {
		ArrayList<Operation> list = new ArrayList<Operation>(Arrays.asList(operations));
		list.add(op);
		operations = list.toArray(new Operation[list.size()]);
	}

	public int numOperations() {
		return operations.length;
	}

	public List<Operation> operations() {
		return Arrays.asList(operations);
	}

	public Operation[] ops() {
		return getOperations();
	}

	public Operation[] getOperations() {
		return operations;
	}

	public void setOperations(Operation[] ops) {
		if (ops == null)
			throw new IllegalArgumentException("'ops' mustn't be null.");
		operations = ops;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Route js = (Route) super.clone();
		if (operations != null)
			js.operations = operations.clone();
		return js;
	}

}
