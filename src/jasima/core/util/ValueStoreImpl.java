package jasima.core.util;

import java.util.HashMap;
import java.util.Set;

/**
 * Implementation of the {@link ValueStore} interface backed by a simple
 * {@code HashMap}. This class can be used to implement {@code ValueStore}
 * functionality on behalf of a host object.
 */
public class ValueStoreImpl implements ValueStore, SilentCloneable<ValueStoreImpl> {

	private HashMap<Object, Object> valueStore;

	public ValueStoreImpl() {
		super();
		valueStore = new HashMap<>();
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
	 * @see #valueStoreGet(Object)
	 */
	@Override
	public void valueStorePut(Object key, Object value) {
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
	 * @return The value previously associated with "key", or null, if no such
	 *         key was found.
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
	public ValueStoreImpl clone() throws CloneNotSupportedException {
		ValueStoreImpl c = (ValueStoreImpl) super.clone();

		// deep clone of map but only shallow copy of entries
		c.valueStore = new HashMap<>();
		c.valueStore.putAll(valueStore);

		return c;
	}

}
