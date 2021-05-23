package jasima.core.util;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jasima.core.simulation.Simulation;

/**
 * Manage series (identified by their name) of non-negative consecutive
 * integers. This class is usually instantiated via the static method
 * {@link #getFor(Simulation)} to access a single instance for a given
 * simulation. Internally the simulation's {@link ValueStore} is used to store
 * the instance.
 * 
 * @author Torsten Hildebrandt
 */
public class SequenceNumberService {

	/**
	 * Returns the instance of this class associated with the given simulation. If
	 * no such instance exists, a new one will be created and stored in the
	 * simulation's valueStore to be picked up by later calls to this method.
	 */
	public static SequenceNumberService getFor(Simulation sim) {
		String serviceName = SequenceNumberService.class.getName();
		SequenceNumberService s = (SequenceNumberService) sim.valueStoreGet(serviceName);
		if (s == null) {
			s = (SequenceNumberService) sim.valueStorePut(serviceName, new SequenceNumberService());
		}
		return s;
	}

	private final Map<String, AtomicInteger> values;

	/**
	 * Parameterless constructor. Usually you will use the method
	 * {@link #getFor(Simulation)} to (re-)use a global instance of this class
	 * associated with a certain simulation.
	 */
	public SequenceNumberService() {
		super();
		values = new HashMap<>();
	}

	/**
	 * Looks up the next value associated with key. If "key" is used for the first
	 * time, 1 will be returned.
	 * 
	 * @param key The key to use (musn't be null).
	 * @return The next value to use or "key".
	 */
	public int nextValue(String key) {
		AtomicInteger v = requireCounter(key);
		return v.incrementAndGet();
	}

	/**
	 * Returns a String consisting of the key and the next value in the sequence,
	 * separated by '-'. This means, calling this method for the key "job" would
	 * result in "job-1" where the number at the end is the value returned by
	 * {@link #nextValue(String)}.
	 * 
	 * @param key The name to use.
	 * @return A formatted String containing the key, a single dash and the sequence
	 *         number.
	 */
	public String nextFormattedValue(String key) {
		return key + '-' + nextValue(key);
	}

	/**
	 * Returns the last value returned for "key", i.e., the last value returned by
	 * {@link #nextValue(String)} for this key. If the key was not used before, then
	 * -1 will be returned.
	 */
	public int get(String key) {
		AtomicInteger v = values.get(key);
		return v != null ? v.get() : -1;
	}

	/**
	 * Explicitly sets the current value associated with a certain key. The value
	 * used will be returned by the next call to {@link #nextValue(String)} for this
	 * key.
	 * <p>
	 * To create a series starting by "1" this method does not have to be called,
	 * just start calling {@link #nextValue(String)}.
	 * 
	 * @param key        the name to use for the series, mustn't be null.
	 * @param startValue the next value to return for the series, mustn't be
	 *                   negative
	 * @throws IllegalArgumentException if either {@code key} was null or
	 *                                  {@code startValue} was <0
	 */
	public void set(String key, int startValue) {
		if (startValue < 0 || key == null) {
			throw new IllegalArgumentException();
		}
		AtomicInteger v = requireCounter(key);
		v.set(startValue - 1);
	}

	/**
	 * Gets or creates a counter associated with "key".
	 */
	private AtomicInteger requireCounter(String key) {
		AtomicInteger v = values.get(requireNonNull(key));
		if (v == null) {
			v = new AtomicInteger(0);
			values.put(key, v);
		}
		return v;
	}

}
