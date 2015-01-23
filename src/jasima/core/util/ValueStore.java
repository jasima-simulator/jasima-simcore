/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
package jasima.core.util;

import java.util.HashMap;
import java.util.Set;

/**
 * This interface provides a simple get/put-mechanism to store a value
 * associated with a key object, similar to a @link {@link HashMap}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
public interface ValueStore {

	/**
	 * Puts a value in the value store, potentially overwriting an existing
	 * value associated with the same key.
	 * 
	 * @param key
	 *            The key name.
	 * @param value
	 *            value to assign to {@code key}.
	 * @see #valueStoreGet(Object)
	 */
	public void valueStorePut(Object key, Object value);

	/**
	 * Retrieves a value from the value store.
	 * 
	 * @param key
	 *            The entry to return, e.g., identified by a name.
	 * @return The value associated with {@code key}.
	 * 
	 * @see #valueStorePut(Object, Object)
	 */
	public Object valueStoreGet(Object key);

	/**
	 * Returns the number of keys in this value store.
	 */
	public int valueStoreGetNumKeys();

	/**
	 * Returns a list of all keys contained in this value store.
	 */
	public Set<Object> valueStoreGetAllKeys();

	/**
	 * Removes an entry from this value store.
	 * 
	 * @return The value previously associated with "key", or null, if no such
	 *         key was found.
	 */
	public Object valueStoreRemove(Object key);

}
