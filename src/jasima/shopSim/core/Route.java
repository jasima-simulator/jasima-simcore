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
package jasima.shopSim.core;

import jasima.core.util.ValueStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Simple container for Operations.
 * 
 * @version "$Id$"
 */
public class Route implements ValueStore {

	private Operation[] operations;
	private HashMap<Object, Object> valueStore;

	public Route() {
		super();
		operations = new Operation[0];
	}

	public Operation operation(int i) {
		return operations[i];
	}

	public void addSequentialOperation(Operation op) {
		ArrayList<Operation> list = new ArrayList<Operation>(
				Arrays.asList(operations));
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

	/**
	 * Offers a simple get/put-mechanism to store and retrieve information as a
	 * kind of global data store. This can be used as a simple extension
	 * mechanism.
	 * 
	 * @param key
	 *            The key name.
	 * @param value
	 *            value to assign to {@code key}.
	 * @see #valueStoreGet(String)
	 */
	@Override
	public void valueStorePut(Object key, Object value) {
		if (valueStore == null)
			valueStore = new HashMap<Object, Object>();
		valueStore.put(key, value);
	}

	/**
	 * Retrieves a value from the value store.
	 * 
	 * @param key
	 *            The entry to return, e.g., identified by a name.
	 * @return The value associated with {@code key}.
	 * @see #valueStorePut(Object, Object)
	 */
	@Override
	public Object valueStoreGet(Object key) {
		if (valueStore == null)
			return null;
		else
			return valueStore.get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Route js = (Route) super.clone();
		if (operations != null)
			js.operations = operations.clone();
		if (valueStore != null)
			js.valueStore = (HashMap<Object, Object>) valueStore.clone();
		return js;
	}

}
