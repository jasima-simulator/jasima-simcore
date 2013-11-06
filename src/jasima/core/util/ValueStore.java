package jasima.core.util;

import java.util.HashMap;

/**
 * This interface provides a simple get/put-mechanism to store a value
 * associated with a key object, similar to a @link {@link HashMap}.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
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
	 * @see #valueStoreGet(String)
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

}
