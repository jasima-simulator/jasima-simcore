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
package jasima.core.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import jasima.core.random.RandomFactory;
import jasima.core.util.MsgCategory;
import jasima.core.util.Util;

/**
 * Base class for a discrete event simulation. This class doesn't do much, but
 * only maintains an event queue and manages simulation time.
 * <p>
 * The typical life cycle of a simulation would be to create it, and
 * subsequently set any parameters. Afterwards {@link #init()} has to be called
 * before the actual simulation can be performed in {@link #run()}. After
 * completing a simulation the {@link #done()}-method should be called to
 * perform clean-up, collecting simulation results, etc.
 * 
 * @author Torsten Hildebrandt
 */
public class Simulation {

	public static final String QUEUE_IMPL_KEY = "jasima.core.simulation.Simulation.queueImpl";
	public static final String QUEUE_IMPL_DEF = EventHeap.class.getName();

	public static class SimPrintEvent {

		public final Simulation sim;
		public final MsgCategory category;
		private String messageFormatString;
		private Object[] params;
		private String message;

		public SimPrintEvent(Simulation sim, MsgCategory category, String message) {
			super();

			Objects.requireNonNull(message);

			this.sim = sim;
			this.category = category;
			this.message = message;
		}

		public SimPrintEvent(Simulation sim, MsgCategory category, String messageFormatString, Object... params) {
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
				message = String.format(Util.DEF_LOCALE, messageFormatString, params);
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

	@FunctionalInterface
	public interface SimMethod {
		void handle();
	}

	// /////////// simulation parameters

	private double simulationLength = 0.0d;
	private RandomFactory rndStreamFactory;
	private String name = null;
	private SimComponentContainer<SimComponent> rootComponent;
	private MsgCategory printLevel = MsgCategory.INFO;
	private ArrayList<Consumer<SimPrintEvent>> printListener;

	// ////////////// attributes/fields used during a simulation

	// the current simulation time.
	private double simTime;
	private int currPrio;
	private Event currEvent;
	private long numEventsProcessed;

	// event queue
	private EventQueue events;
	// eventNum is used to enforce FIFO-order of concurrent events with equal
	// priorities
	private int eventNum;
	private boolean continueSim;
	private int numAppEvents;

	public Simulation() {
		super();

		printListener = new ArrayList<>();

		RandomFactory randomFactory = RandomFactory.newInstance();
		setRndStreamFactory(randomFactory);

		setRootComponent(new SimComponentContainerBase<SimComponent>() {
			@Override
			public void beforeRun() {
				super.beforeRun();

				trace("%s\tsim_start", simTime());
			}

			@Override
			public void afterRun() {
				super.afterRun();

				trace("%s\tsim_end", simTime());
			}
		});
	}

	public void addPrintListener(Consumer<SimPrintEvent> listener) {
		printListener.add(listener);
	}

	public boolean removePrintListener(Consumer<SimPrintEvent> listener) {
		return printListener.remove(listener);
	}

	public int numPrintListener() {
		return printListener.size();
	}

	public List<Consumer<SimPrintEvent>> printListener() {
		return Collections.unmodifiableList(printListener);
	}

	/**
	 * Performs all initializations required for a successful simulation
	 * {@link #run()}.
	 */
	public void init() {
		init0();

		rootComponent.init();
	}

	protected void init0() {
		events = createEventQueue();
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
		numEventsProcessed = 0;
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

		do {
			try {
				// main event loop
				while (continueSim) {
					currEvent = events.extract();

					// Advance clock to time of next event
					simTime = currEvent.getTime();
					currPrio = currEvent.getPrio();

					currEvent.handle();

					if (currEvent.isAppEvent()) {
						if (--numAppEvents == 0)
							continueSim = false;
					}

					numEventsProcessed++;
				}
			} catch (Throwable t) {
				boolean rethrow = handleError(t);

				if (rethrow) {
					if (t instanceof RuntimeException) {
						throw (RuntimeException) t;
					} else if (t instanceof Error) {
						throw (Error) t;
					} else
						// can't occur
						throw new AssertionError();
				} else {
					// do nothing
				}
			}
		} while (continueSim);

		afterRun();
	}

	/**
	 * This method is called if an unhandled exception occurs during the main of
	 * a simulation run. The implementation here just prints an appropriate
	 * message and then rethrows the Exception, terminating the simulation run.
	 * 
	 * @param t
	 *            The Error or RuntimeException that was triggered somewhere in
	 *            simulation code.
	 * @return Whether or not to rethrow the Exception after processing.
	 */
	protected boolean handleError(Throwable t) {
		String errorString = Util.exceptionToString(t);

		print(MsgCategory.ERROR, "An uncaught exception occurred. Current event='%s', exception='%s'", currentEvent(),
				errorString);

		return true;
	}

	/**
	 * Override this method to perform initializations after {@link #init()},
	 * but before running the simulation. This method is usually used to
	 * schedule initial events.
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

		rootComponent.beforeRun();
	}

	/**
	 * Override this method to perform some action after running the simulation,
	 * but before {@link #done()} is called.
	 */
	protected void afterRun() {
		rootComponent.afterRun();
	}

	/**
	 * Performs clean-up etc., after a simulation's {@link #run()} method
	 * finished.
	 */
	public void done() {
		rootComponent.done();
	}

	/**
	 * Schedules a new event.
	 */
	public void schedule(Event event) {
		if (event.getTime() == simTime && event.getPrio() <= currPrio)
			print(MsgCategory.WARN, "Priority inversion (current: %d, scheduled: %d, event=%s).", currPrio,
					event.getPrio(), event.toString());
		if (event.getTime() < simTime) {
			print(MsgCategory.ERROR,
					"Can't schedule an event that is in the past (time to schedule: %f, prio=%d, event=%s).",
					event.getTime(), event.getPrio(), event.toString());
			end();
		}
		event.eventNum = eventNum++;
		if (event.isAppEvent())
			numAppEvents++;
		events.insert(event);
	}

	/**
	 * Schedules a call to {@code method} at certain point in time.
	 * 
	 * @param time
	 *            The time when to call {@code method}.
	 * @param prio
	 *            Priority of the event (to deterministically sequence events at
	 *            the same time.
	 * @param method
	 *            The method to call at the given moment.
	 */
	public void schedule(double time, int prio, SimMethod method) {
		Event e = new MethodCallEvent(time, prio, method);
		schedule(e);
	}

	/**
	 * Periodically calls a certain method. While this method returns true, a
	 * next invocation after the given time interval is scheduled.
	 */
	public void schedulePeriodically(double firstInvocation, double interval, int prio, BooleanSupplier method) {
		schedule(new Event(firstInvocation, prio) {
			@Override
			public void handle() {
				if (method.getAsBoolean()) {
					// schedule next invocation reusing Event object
					setTime(simTime() + interval);
					schedule(this);
				}
			}
		});
	}

	/**
	 * Calls a certain method at the times returned by the method itself. The
	 * first invocation is performed at the current time (asynchronously, i.e.,
	 * {@code scheduleProcess()} returns before {@code method} is called for the
	 * first time). Subsequent calls are scheduled at the absolute times
	 * returned by the previous method invocation. No more invocations are
	 * scheduled if {@code method} returned NaN or a negative value.
	 */
	public void scheduleProcess(int prio, DoubleSupplier method) {
		schedule(new Event(simTime(), prio) {
			@Override
			public void handle() {
				double next = method.getAsDouble();
				if (!(next < 0.0)) {
					// schedule next invocation reusing Event object
					setTime(next);
					schedule(this);
				}
			}
		});
	}

	public Map<String, Object> runSim() {
		init();
		run();
		done();

		Map<String, Object> res = new HashMap<>();
		produceResults(res);
		return res;
	}

	/**
	 * This class is used internally by {@link #schedule(double,int,SimMethod)}.
	 */
	private static final class MethodCallEvent extends Event {
		public final SimMethod m;

		private MethodCallEvent(double time, int prio, SimMethod method) {
			super(time, prio);
			m = method;
		}

		@Override
		public void handle() {
			m.handle();
		}
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
	 * Returns the priority of the currently processed event.
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

	/**
	 * Returns the number of events processed by the main simulation loop.
	 */
	public long numEventsProcessed() {
		return numEventsProcessed;
	}

	/**
	 * Populates the given HashMap with results produced in the simulation run.
	 */
	public void produceResults(Map<String, Object> res) {
		res.put("simTime", simTime());
		rootComponent.produceResults(res);
	}

	/**
	 * Convenience method to add a new component to the root component of this
	 * simulation.
	 */
	public void addComponent(SimComponent sc) {
		getRootComponent().addComponent(sc);
	}

	/**
	 * Triggers a print event of category "INFO".
	 * 
	 * @param message
	 *            The message to print.
	 * @see #print(MsgCategory, String)
	 */
	public void print(String message) {
		print(MsgCategory.INFO, message);
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this should produce an output of {@code message}.
	 * 
	 * @param message
	 *            The message to print.
	 */
	public void print(MsgCategory category, String message) {
		if (numPrintListener() > 0) {
			print(new SimPrintEvent(this, category, message));
		}
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this produces a message defined by the format string
	 * {@code messageFormatString} (used with the arguments given in
	 * {@code params}).
	 */
	public void print(MsgCategory category, String messageFormatString, Object... params) {
		if (numPrintListener() > 0) {
			print(new SimPrintEvent(this, category, messageFormatString, params));
		}
	}

	/**
	 * Same as {@link #print(MsgCategory, String, Object...)}, but defaulting to
	 * category {@code INFO}
	 */
	public void print(String messageFormatString, Object... params) {
		print(MsgCategory.INFO, messageFormatString, params);
	}

	/**
	 * Prints a certain {@link SimPrintEvent} by passing it to the registered
	 * print listeners.
	 */
	protected void print(SimPrintEvent e) {
		printListener.forEach(l -> l.accept(e));
	}

	public MsgCategory getPrintLevel() {
		return printLevel;
	}

	public void setPrintLevel(MsgCategory printLevel) {
		Objects.requireNonNull(printLevel);
		this.printLevel = printLevel;
	}

	public void trace(String messageFormatString, Object... params) {
		print(MsgCategory.TRACE, messageFormatString, params);
	}

	public void trace(String message) {
		print(MsgCategory.TRACE, message);
	}

	public boolean isTraceEnabled() {
		return getPrintLevel().ordinal() >= MsgCategory.TRACE.ordinal();
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
		rndStreamFactory.setSim(this);
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

	public SimComponentContainer<SimComponent> getRootComponent() {
		return rootComponent;
	}

	protected void setRootComponent(SimComponentContainer<SimComponent> rootComponent) {
		if (this.rootComponent != null) {
			this.rootComponent.setSim(null);
		}

		this.rootComponent = rootComponent;
		rootComponent.setSim(this);
	}

	@Override
	protected Simulation clone() throws CloneNotSupportedException {
		Simulation sim = (Simulation) super.clone();
		return sim;
	}

}
