/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
