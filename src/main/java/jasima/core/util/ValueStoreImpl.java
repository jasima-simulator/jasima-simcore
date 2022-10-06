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
package jasima.core.util;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of the {@link ValueStore} interface backed by a simple
 * {@code HashMap}. This class can be used to implement {@code ValueStore}
 * functionality on behalf of a host object.
 */
public class ValueStoreImpl implements ValueStore, Cloneable {

	private HashMap<Object, Object> valueStore;

	public ValueStoreImpl() {
		super();
		valueStore = new HashMap<>();
	}

	/**
	 * Offers a simple get/put-mechanism to store and retrieve information as a kind
	 * of global data store. This can be used as a simple extension mechanism.
	 * 
	 * @param key   The key name.
	 * @param value value to assign to {@code key}. Mustn't be null (otherwise a
	 *              {@link NullPointerException} is thrown).
	 * @return returns {@code value}
	 * @see #valueStoreGet(Object)
	 */
	@Override
	public <T> T valueStorePut(Object key, T value) {
		valueStore.put(key, Objects.requireNonNull(value));
		return value;
	}

	/**
	 * Retrieves a value from the value store.
	 * 
	 * @param key The entry to return, e.g., identified by a name.
	 * @return The value associated with {@code key}.
	 * @see #valueStorePut(Object, Object)
	 */
	@Override
	public Object valueStoreGet(Object key) {
		return valueStore.get(key);
	}

	/**
	 * Returns the number of keys in this value store.
	 */
	@Override
	public int valueStoreGetNumKeys() {
		return valueStore.size();
	}

	/**
	 * Returns a list of all keys contained in this value store.
	 */
	@Override
	public Set<Object> valueStoreGetAllKeys() {
		return valueStore.keySet();
	}

	/**
	 * Removes an entry from this value store.
	 * 
	 * @return The value previously associated with "key", or null, if no such key
	 *         was found.
	 */
	@Override
	public Object valueStoreRemove(Object key) {
		return valueStore.remove(key);
	}

	@Override
	public ValueStore valueStoreImpl() {
		return this;
	}

	@Override
	public ValueStoreImpl clone() {
		try {
			ValueStoreImpl c = (ValueStoreImpl) super.clone();

			// deep clone of map but only shallow copy of entries
			c.valueStore = new HashMap<>();
			c.valueStore.putAll(valueStore);

			return c;
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

}
