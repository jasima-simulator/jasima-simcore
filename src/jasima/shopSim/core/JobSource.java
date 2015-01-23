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
package jasima.shopSim.core;

import jasima.core.simulation.Event;
import jasima.core.util.ValueStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * A job source is an abstract base class for classes producing {@link Job}s.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public abstract class JobSource implements ValueStore {

	// bigger than WorkStation.DEPART_PRIO but smaller than
	// WorkStation.SELECT_PRIO
	public static final int ARRIVE_PRIO = Event.EVENT_PRIO_HIGH;

	private JobShop shop;
	private HashMap<Object, Object> valueStore;

	public boolean stopArrivals;
	public int jobsStarted;
	public int index; // index in shop.sources

	public JobSource() {
		super();
	}

	public void init() {
		stopArrivals = false;
		jobsStarted = 0;

		Event arriveEvent = new Event(0.0d, ARRIVE_PRIO) {

			private Job nextJob; // next job to be released

			@Override
			public void handle() {
				if (stopArrivals)
					return;

				// create new job
				Job job = createNextJob();

				if (job != null) {
					if (job.getRelDate() < getShop().simTime())
						throw new IllegalStateException(
								"arrival time is in the past: " + job);

					// schedule next arrival reusing this Event object
					this.setTime(job.getRelDate());
					getShop().schedule(this);
				}

				// release "nextJob"
				if (nextJob != null) {
					getShop().startJob(nextJob);
				}

				nextJob = job;
			}

		};
		// schedule first arrival
		arriveEvent.setTime(getShop().simTime());
		getShop().schedule(arriveEvent);
	}

	public abstract Job createNextJob();

	/**
	 * Factory method used in {@link #createNextJob()} to create a new job
	 * instance.
	 */
	protected Job newJobInstance() {
		return new Job(getShop());
	}

	public JobShop getShop() {
		return shop;
	}

	public void setShop(JobShop shop) {
		this.shop = shop;
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

	/**
	 * Returns the number of keys in the value store.
	 */
	@Override
	public int valueStoreGetNumKeys() {
		return (valueStore == null) ? 0 : valueStore.size();
	}

	/**
	 * Returns a list of all keys contained in the value store.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<Object> valueStoreGetAllKeys() {
		if (valueStore == null)
			return Collections.EMPTY_SET;
		else
			return valueStore.keySet();
	}

	/**
	 * Removes an entry from the value store.
	 * 
	 * @return The value previously associated with "key", or null, if no such
	 *         key was found.
	 */
	@Override
	public Object valueStoreRemove(Object key) {
		if (valueStore == null)
			return null;
		else
			return valueStore.remove(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		JobSource js = (JobSource) super.clone();
		if (valueStore != null)
			js.valueStore = (HashMap<Object, Object>) valueStore.clone();
		return js;
	}

}
