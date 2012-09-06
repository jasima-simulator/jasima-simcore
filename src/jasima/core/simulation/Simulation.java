/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.core.simulation;

import jasima.core.random.RandomFactory;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.util.Util;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierAdapter;
import jasima.core.util.observer.NotifierListener;

import java.util.Map;

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
 * @author Torsten Hildebrandt, 2012-02-08
 */
public class Simulation implements Notifier<Simulation, SimEvent> {

	/**
	 * Base class for notifier events (NOT simulation events, they are not
	 * handled by the event queue).
	 */
	public static class SimEvent {
	}

	// constants for default events thrown by a simulation
	public static final SimEvent SHOP_INIT = new SimEvent();
	public static final SimEvent SHOP_SIM_START = new SimEvent();
	public static final SimEvent SHOP_SIM_END = new SimEvent();
	public static final SimEvent SHOP_DONE = new SimEvent();
	public static final SimEvent COLLECT_RESULTS = new SimEvent();

	public enum SimMsgCategory {
		OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
	}

	public static class SimPrintEvent extends SimEvent {
		public SimPrintEvent(Simulation sim, SimMsgCategory category, String message) {
			super();
			this.sim = sim;
			this.category = category;
			this.message = message;
		}

		public final Simulation sim;
		public final SimMsgCategory category;
		public final String message;
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

	// fields used during event notification
	public Map<String, Object> resultMap;

	// ////////////// attributes/fields used during a simulation

	/**
	 * The current simulation time.
	 */
	private double simTime;

	private EventQueue eventList;
	// enforce FIFO-order of concurrent events with equal priorities
	private int eventNum;
	private boolean continueSim;
	private int numAppEvents;

	/**
	 * Performs all initializations required for a successful simulation
	 * {@link #run()}.
	 */
	public void init() {
		eventList = new EventHeap(103);
		simTime = 0.0d;
		eventNum = Integer.MIN_VALUE;
		numAppEvents = 0;

		// schedule simulation end
		if (getSimulationLength() > 0.0)
			schedule(new Event(getSimulationLength(), Event.EVENT_PRIO_LOWEST) {
				@Override
				public void handle() {
					end();
				}
			});

		if (numListener() > 0) {
			fire(SHOP_INIT);
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
	 * @see Event#isAppEvent()
	 */
	public void run() {
		if (numListener() > 0) {
			fire(SHOP_SIM_START);
		}

		continueSim = true;

		Event event;
		while ((event = eventList.extract()) != null && continueSim) {
			// Advance clock to time of next event
			simTime = event.getTime();

			event.handle();

			if (event.isAppEvent()) {
				if (--numAppEvents == 0)
					continueSim = false;
			}
		}

		if (numListener() > 0) {
			fire(SHOP_SIM_END);
		}
	}

	/**
	 * Performs clean-up, etc., after a simulation {@link #run()} finished.
	 */
	public void done() {
		if (numListener() > 0) {
			fire(SHOP_DONE);
		}
	}

	/**
	 * Schedules a new event.
	 */
	public void schedule(Event event) {
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

	public void setRndStreamFactory(RandomFactory rndStreamFactory) {
		this.rndStreamFactory = rndStreamFactory;
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
	 * @param listener
	 *            The shop listener to add.
	 * @param cloneIfPossible
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

	@Override
	public void fire(SimEvent event) {
		if (adapter != null)
			adapter.fire(event);
	}

	@Override
	public int numListener() {
		return adapter == null ? 0 : adapter.numListener();
	}

}
