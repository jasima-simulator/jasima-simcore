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
package jasima.core.simulation;

import jasima.core.random.RandomFactory;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.util.Util;
import jasima.core.util.ValueStore;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierAdapter;
import jasima.core.util.observer.NotifierListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for a discrete event simulation. This class doesn't do much, but
 * only maintains an event queue and manages simulation time.
 * <p />
 * The typical life cycle of a simulation would be to create it, and
 * subsequently set any parameters. Afterwards {@link #init()} has to be called
 * before the actual simulation can be performed in {@link #run()}. After
 * completing a simulation the {@link #done()}-method should be called to
 * perform clean-up, collecting simulation results, etc.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-02-08
 * @version 
 *          "$Id$"
 */
public class Simulation implements Notifier<Simulation, SimEvent>, ValueStore {

	public static final String QUEUE_IMPL_KEY = "jasima.core.simulation.Simulation.queueImpl";
	public static final String QUEUE_IMPL_DEF = EventHeap.class.getName();

	/**
	 * Base class for notifier events (NOT simulation events, they are not
	 * handled by the event queue, just send to listeners).
	 */
	public static class SimEvent {
	}

	// constants for default events thrown by a simulation
	public static final SimEvent SIM_INIT = new SimEvent();
	public static final SimEvent SIM_START = new SimEvent();
	public static final SimEvent SIM_END = new SimEvent();
	public static final SimEvent SIM_DONE = new SimEvent();
	public static final SimEvent COLLECT_RESULTS = new SimEvent();

	public enum SimMsgCategory {
		OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
	}

	public static class SimPrintEvent extends SimEvent {
		public final Simulation sim;
		public final SimMsgCategory category;
		private String messageFormatString;
		private Object[] params;
		private String message;

		public SimPrintEvent(Simulation sim, SimMsgCategory category,
				String message) {
			super();

			if (message == null)
				throw new NullPointerException();

			this.sim = sim;
			this.category = category;
			this.message = message;
		}

		public SimPrintEvent(Simulation sim, SimMsgCategory category,
				String messageFormatString, Object... params) {
			super();
			this.sim = sim;
			this.category = category;
			this.messageFormatString = messageFormatString;
			this.params = params;
			this.message = null;
		}

		public String getMessage() {
			// lazy creation of message only when needed
			if (message == null) {
				message = String.format(messageFormatString, params);
				messageFormatString = null;
				params = null;
			}

			return message;
		}

		@Override
		public String toString() {
			return getMessage();
		}
	}

	public static interface EventQueue {
		/** Insert an event in the queue. */
		public void insert(Event e);

		/** Extract the (chronologically) next event from the queue. */
		public Event extract();
	}

	// /////////// simulation parameters
	private double simulationLength = 0.0d;
	private RandomFactory rndStreamFactory;

	private HashMap<Object, Object> valueStore;

	private String name = null;

	// fields used during event notification
	public Map<String, Object> resultMap;

	// ////////////// attributes/fields used during a simulation

	// the current simulation time.
	private double simTime;
	private int currPrio;
	private Event currEvent;

	// event queue
	private EventQueue eventList;
	// eventNum is used to enforce FIFO-order of concurrent events with equal
	// priorities
	private int eventNum;
	private boolean continueSim;
	private int numAppEvents;

	/**
	 * Performs all initializations required for a successful simulation
	 * {@link #run()}.
	 */
	protected void init() {
		eventList = createEventQueue();
		// set to dummy event
		currEvent = new Event(Double.NEGATIVE_INFINITY, Event.EVENT_PRIO_MIN) {

			@Override
			public void handle() {
			}
		};
		simTime = 0.0d; // TODO: check initialization with some parameter for t0
		currPrio = Event.EVENT_PRIO_MAX;
		eventNum = Integer.MIN_VALUE;
		numAppEvents = 0;

		if (numListener() > 0) {
			fire(SIM_INIT);
		}
	}

	/**
	 * Runs the main simulation loop. This means:
	 * <ol>
	 * <li>taking an event from the event queue,
	 * <li>advancing simulation time, and
	 * <li>triggering event processing.
	 * </ol>
	 * A simulation is terminated if either the maximum simulation length is
	 * reached, there are no more application events in the queue, or some other
	 * code called {@link #end()}.
	 * <p>
	 * 
	 * @see jasima.core.simulation.Event#isAppEvent()
	 */
	public void run() {
		beforeRun();

		continueSim = numAppEvents > 0;

		// main event loop
		while (continueSim) {
			currEvent = eventList.extract();

			// Advance clock to time of next event
			simTime = currEvent.getTime();
			currPrio = currEvent.getPrio();

			currEvent.handle();

			if (currEvent.isAppEvent()) {
				if (--numAppEvents == 0)
					continueSim = false;
			}
		}

		afterRun();
	}

	/**
	 * Override this method to perform initializations after {@link #init()},
	 * but before running the simulation.
	 */
	protected void beforeRun() {
		// schedule simulation end
		if (getSimulationLength() > 0.0)
			schedule(new Event(getSimulationLength(), Event.EVENT_PRIO_LOWEST) {
				@Override
				public void handle() {
					end();
				}
			});

		if (numListener() > 0) {
			fire(SIM_START);
		}
	}

	/**
	 * Override this method to perform some action after running the simulation,
	 * but before {@link #done()} is called.
	 */
	protected void afterRun() {
		if (numListener() > 0) {
			fire(SIM_END);
		}
	}

	/**
	 * Performs clean-up etc., after a simulation's {@link #run()} method
	 * finished.
	 */
	protected void done() {
		if (numListener() > 0) {
			fire(SIM_DONE);
		}
	}

	/**
	 * Schedules a new event.
	 */
	public void schedule(Event event) {
		if (event.getTime() == simTime && event.getPrio() <= currPrio)
			print(SimMsgCategory.WARN,
					"Priority inversion (current: %d, scheduled: %d, event=%s).",
					currPrio, event.getPrio(), event.toString());
		if (event.getTime() < simTime) {
			print(SimMsgCategory.ERROR,
					"Can't schedule an event that is in the past (time to schedule: %f, prio=%d, event=%s).",
					event.getTime(), event.getPrio(), event.toString());
			end();
		}
		event.eventNum = eventNum++;
		if (event.isAppEvent())
			numAppEvents++;
		eventList.insert(event);
	}

	/**
	 * After calling end() the simulation is terminated (after handling the
	 * current event).
	 */
	public void end() {
		continueSim = false;
	}

	/** Returns the current simulation time. */
	public double simTime() {
		return simTime;
	}

	/**
	 * Returns the priority of the currently processed event;
	 */
	public int currentPrio() {
		return currPrio;
	}

	/**
	 * Returns the {@link Event} object that is currently processed.
	 */
	public Event currentEvent() {
		return currEvent;
	}

	public void produceResults(Map<String, Object> res) {
		res.put("simTime", simTime());

		resultMap = res;
		fire(COLLECT_RESULTS);
		resultMap = null;
	}

	/**
	 * Triggers a print event of category "normal".
	 * 
	 * @param message
	 *            The message to print.
	 * @see #print(SimMsgCategory, String)
	 */
	public void print(String message) {
		print(SimMsgCategory.INFO, message);
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this should produce an output of {@code message}.
	 * 
	 * @param message
	 *            The message to print.
	 */
	public void print(SimMsgCategory category, String message) {
		if (numListener() > 0) {
			fire(new SimPrintEvent(this, category, message));
		}
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this produces a message defined by the format string
	 * {@code messageFormatString} (used with the arguments given in
	 * {@code params}).
	 */
	public void print(SimMsgCategory category, String messageFormatString,
			Object... params) {
		if (numListener() > 0) {
			fire(new SimPrintEvent(this, category, messageFormatString, params));
		}
	}

	/**
	 * Same as {@link #print(SimMsgCategory, String, Object...)}, but defaulting
	 * to category {@code INFO}
	 */
	public void print(String messageFormatString, Object... params) {
		print(SimMsgCategory.INFO, messageFormatString, params);
	}

	/**
	 * Factory method to create a new event queue.
	 * 
	 * @return The event queue to use in this simulation.
	 */
	protected EventQueue createEventQueue() {
		String queueImpl = System.getProperty(QUEUE_IMPL_KEY, QUEUE_IMPL_DEF);
		Class<?> qClass;
		try {
			qClass = Class.forName(queueImpl);
			return (EventQueue) qClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Sets the maximum simulation time. A value of 0.0 means no such limit. */
	public void setSimulationLength(double simulationLength) {
		this.simulationLength = simulationLength;
	}

	public double getSimulationLength() {
		return simulationLength;
	}

	public RandomFactory getRndStreamFactory() {
		return rndStreamFactory;
	}

	/**
	 * Sets the random factory to use to create random number streams for
	 * stochastic simulations.
	 */
	public void setRndStreamFactory(RandomFactory rndStreamFactory) {
		this.rndStreamFactory = rndStreamFactory;
	}

	public String getName() {
		return name;
	}

	/**
	 * Sets a name for this simulation.
	 */
	public void setName(String name) {
		this.name = name;
	}

	// event notification

	private NotifierAdapter<Simulation, SimEvent> adapter = null;

	@Override
	public void addNotifierListener(
			NotifierListener<Simulation, SimEvent> listener) {
		if (adapter == null)
			adapter = new NotifierAdapter<Simulation, SimEvent>(this);
		adapter.addNotifierListener(listener);
	}

	/**
	 * Adds a listener to this simulation. This method only differs from
	 * {@link #addNotifierListener(NotifierListener)} in its ability to
	 * (optionally) clone the listener (using
	 * {@link Util#cloneIfPossible(Object)}) before installing it.
	 * 
	 * @param l
	 *            The shop listener to add.
	 * @param cloneIfPossbile
	 *            whether to try to clone a new instance for each machine using
	 *            {@link Util#cloneIfPossible(Object)}.
	 */
	public NotifierListener<Simulation, SimEvent> installSimulationListener(
			NotifierListener<Simulation, SimEvent> l, boolean cloneIfPossbile) {
		if (cloneIfPossbile)
			l = Util.cloneIfPossible(l);
		addNotifierListener(l);
		return l;
	}

	@Override
	public NotifierListener<Simulation, SimEvent> getNotifierListener(int index) {
		return adapter.getNotifierListener(index);
	}

	@Override
	public void removeNotifierListener(
			NotifierListener<Simulation, SimEvent> listener) {
		adapter.removeNotifierListener(listener);
	}

	protected void fire(SimEvent event) {
		if (adapter != null)
			adapter.fire(event);
	}

	@Override
	public int numListener() {
		return adapter == null ? 0 : adapter.numListener();
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

	/**
	 * Returns the number of keys in this simulation's value store.
	 */
	@Override
	public int valueStoreGetNumKeys() {
		return (valueStore == null) ? 0 : valueStore.size();
	}

	/**
	 * Returns a list of all keys contained in this simulation's value store.
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
	 * Removes an entry from this simulation's value store.
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
		Simulation sim = (Simulation) super.clone();
		if (valueStore != null) {
			sim.valueStore = (HashMap<Object, Object>) valueStore.clone();
		}
		if (adapter != null) {
			sim.adapter = adapter.clone();
			sim.adapter.setNotifier(sim);
		}
		return sim;
	}

}
