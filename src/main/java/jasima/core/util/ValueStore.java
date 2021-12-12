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
package jasima.core.util;

import java.util.Map;
import java.util.Set;

/**
 * This interface provides a simple get/put-mechanism to store a value
 * associated with a key object, similar to a @link {@link Map}.
 * <p>
 * 
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
	 * @return returns {@code value}
	 * @see #valueStoreGet(Object)
	 */
	default Object valueStorePut(Object key, Object value) {
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
