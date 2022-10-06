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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This interface provides a simple get/put-mechanism to store a value
 * associated with a key object, similar to a @link {@link Map}.
 * <p>
 * 
 * @author Torsten Hildebrandt
 */
public interface ValueStore {

	/**
	 * Puts a value in the value store, potentially overwriting an existing value
	 * associated with the same key.
	 * 
	 * @param key   The key name.
	 * @param value value to assign to {@code key}.
	 * @return returns {@code value} to allow chaining calls.
	 * @see #valueStoreGet(Object)
	 */
	default <T> T valueStorePut(Object key, T value) {
		return valueStoreImpl().valueStorePut(key, value);
	}

	/**
	 * Retrieves a value from the value store.
	 * 
	 * @param key The entry to return, e.g., identified by a name.
	 * @return The value associated with {@code key}.
	 * 
	 * @see #valueStorePut(Object, Object)
	 */
	default Object valueStoreGet(Object key) {
		return valueStoreImpl().valueStoreGet(key);
	}

	/**
	 * Retrieves a value from the value store. If no such value is contained in the
	 * valueStore or the value null is associated with {@code key}, the default
	 * value is returned.
	 * 
	 * @param key          The entry to return, e.g., identified by a name.
	 * @param defaultValue The default value to use.
	 * @return The value associated with {@code key} or {@code defaultValue} if no
	 *         value was associated.
	 * 
	 * @see #valueStorePut(Object, Object)
	 */
	default Object valueStoreGet(Object key, Object defaultValue) {
		Object value = valueStoreGet(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Applies the given function to the old value associated with {@code key} (null
	 * if not existing), and stores the new, computed value in the value store.
	 * 
	 * @param key  The key to access.
	 * @param func The function to apply to the old value, calculating the new one.
	 * @return The new value as produced by {@code func}.
	 */
	default Object valueStoreUpdate(Object key, Function<Object, Object> func) {
		Object oldValue = valueStoreGet(key);
		Object newValue = func.apply(oldValue);
		if (newValue != null)
			valueStorePut(key, newValue);
		else {
			valueStoreRemove(key);
		}

		return newValue;
	}

	/**
	 * Check for the existence of a certain key.
	 * 
	 * @param key The key, usually identified by a name.
	 * @return Returns true, if a non-null value is associated with {@code key}.
	 */
	default boolean valueStoreContains(Object key) {
		return valueStoreGet(key) != null;
	}

	/**
	 * Returns the number of keys in this value store.
	 */
	default int valueStoreGetNumKeys() {
		return valueStoreImpl().valueStoreGetNumKeys();
	}

	/**
	 * Returns a list of all keys contained in this value store.
	 */
	default Set<Object> valueStoreGetAllKeys() {
		return valueStoreImpl().valueStoreGetAllKeys();
	}

	/**
	 * Removes an entry from this value store.
	 * 
	 * @return The value previously associated with "key", or null, if no such key
	 *         was found.
	 */
	default Object valueStoreRemove(Object key) {
		return valueStoreImpl().valueStoreRemove(key);
	}

	/**
	 * Returns the implementation to use for adding ValueStore functionality.
	 */
	ValueStore valueStoreImpl();

	/**
	 * Copies a value (shallow copy) from one value store to another using the same
	 * name.
	 */
	public static boolean copy(ValueStore from, ValueStore to, String valueName) {
		if (!from.valueStoreContains(valueName)) {
			return false;
		}
		Object value = from.valueStoreGet(valueName);
		to.valueStorePut(valueName, value);
		return true;
	}

}
