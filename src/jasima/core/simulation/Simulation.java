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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntStream;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.TraceFileProducer;
import jasima.core.util.Util;

/**
 * Base class for a discrete event simulation. This class doesn't do much, but
 * only maintains an event queue and manages simulation time. Additionally it
 * offers a centralized place to initialize random number streams and to create
 * status and debug messages.
 * <p>
 * The typical life cycle of a simulation would be to create it, and
 * subsequently set any parameters. Afterwards {@link #init()} has to be called
 * before the actual simulation can be performed in {@link #run()}. After
 * completing a simulation the {@link #done()}-method should be called to
 * perform clean-up, collecting simulation results, etc. As a final step usually
 * {@link #produceResults(Map)} is called to allow the simulation and all
 * simulation components to report simulation results.
 * 
 * @author Torsten Hildebrandt
 */
public class Simulation {

	// constants for simTimeToMillisFactor used to convert simTime to an Instant
	public static final long MILLIS_PER_MINUTE = 60 * 1000;
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
	public static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;

	public static final String QUEUE_IMPL_KEY = "jasima.core.simulation.Simulation.queueImpl";
	public static final String QUEUE_IMPL_DEF = EventHeap.class.getName();

	/**
	 * {@link SimPrintMessage}s are produced whenever {@code print()} or
	 * {@code trace()} is called during a simulation to produce status/debug
	 * messages. They are passed to print listeners registered with a simulation for
	 * further processing.
	 * 
	 * @see ConsolePrinter
	 * @see TraceFileProducer
	 */
	public static class SimPrintMessage {

		private static final Object[] EMPTY = new Object[0];

		private final Simulation sim;
		private final MsgCategory category;
		private final double simTime;
		private final Object[] params;
		private String message;

		public SimPrintMessage(Simulation sim, MsgCategory category, String message) {
			this(sim, category, message, EMPTY);
			Objects.requireNonNull(message);
		}

		public SimPrintMessage(Simulation sim, MsgCategory category, Object... params) {
			this(sim, category, null, params);
			Objects.requireNonNull(params);
		}

		protected SimPrintMessage(Simulation sim, MsgCategory category, String msg, Object... params) {
			super();
			this.sim = sim;
			this.simTime = sim.simTime();
			this.category = category;
			this.params = params;

			this.message = msg;
		}

		public Simulation getSim() {
			return sim;
		}

		public MsgCategory getCategory() {
			return category;
		}

		public double getSimTime() {
			return simTime;
		}

		public Object[] getParams() {
			return params;
		}

		public String getMessage() {
			// lazy creation of message only when needed
			if (message == null) {
				StringBuilder sb = new StringBuilder();
				sb.append(getSimTime());
				for (Object o : getParams()) {
					sb.append('\t').append(String.valueOf(o));
				}
				message = sb.toString();
			}

			return message;
		}

		@Override
		public String toString() {
			return getMessage();
		}
	}

	/** Public interface of event queue implementations. */
	public static interface EventQueue {
		/** Insert an event in the queue. */
		public void insert(Event e);

		/** Extract the (chronologically) next event from the queue. */
		public Event extract();
	}

	// /////////// simulation parameters

	private double simulationLength = 0.0d;
	private double initialSimTime = 0.0d;
	private double statsResetTime = 0.0d;
	private RandomFactory rndStreamFactory;
	private String name = null;
	private long simTimeToMillisFactor = MILLIS_PER_MINUTE; // simulation time in minutes
	private Instant simTimeStartInstant;

	private SimComponentContainer<SimComponent> rootComponent;

	private MsgCategory printLevel = MsgCategory.INFO;
	private ArrayList<Consumer<SimPrintMessage>> printListener;

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

		events = createEventQueue();
		// set to dummy event
		currEvent = new Event(Double.NEGATIVE_INFINITY, Event.EVENT_PRIO_MIN) {
			@Override
			public void handle() {
			}
		};
		currPrio = Event.EVENT_PRIO_MAX;
		eventNum = Integer.MIN_VALUE;
		numAppEvents = 0;
		numEventsProcessed = 0;

		LocalDate yearBeg = LocalDate.of(Year.now(Clock.systemUTC()).getValue(), 1, 1);
		simTimeStartInstant = yearBeg.atStartOfDay(ZoneOffset.UTC).toInstant();

		setRootComponent(new SimComponentContainerBase<SimComponent>() {
			@Override
			public void beforeRun() {
				super.beforeRun();

				trace("sim_start");
			}

			@Override
			public void afterRun() {
				super.afterRun();

				trace("sim_end");
			}
		});
	}

	public void addPrintListener(Consumer<SimPrintMessage> listener) {
		printListener.add(listener);
	}

	public boolean removePrintListener(Consumer<SimPrintMessage> listener) {
		return printListener.remove(listener);
	}

	public int numPrintListener() {
		return printListener.size();
	}

	public List<Consumer<SimPrintMessage>> printListener() {
		return Collections.unmodifiableList(printListener);
	}

	/**
	 * Performs all initializations required for a successful simulation
	 * {@link #run()}.
	 */
	public void init() {
		simTime = getInitialSimTime();

		initComponentTree(null, rootComponent);

		rootComponent.init();
	}

	/**
	 * Recursively initialize components before run by setting simulation and parent
	 * node.
	 * 
	 * @param parent
	 * @param child
	 */
	protected void initComponentTree(SimComponentContainer<?> parent, SimComponent child) {
		child.setParent(parent);
		child.setSim(this);

		if (child instanceof SimComponentContainer<?>) {
			SimComponentContainer<?> scc = (SimComponentContainer<?>) child;
			scc.getComponents().forEach(c -> initComponentTree(scc, c));
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
	 * 
	 * @see jasima.core.simulation.Event#isAppEvent()
	 */
	public void run() {
		resetStats();

		continueSim = numAppEvents > 0;

		// ensure time of first event is before initalSimTime, then put in event
		// queue again
		if (continueSim) {
			Event e = events.extract();
			if (e.getTime() < simTime) {
				throw new IllegalArgumentException(createErrorMsgEventInPast(e));
			}

			// everything is ok, reinsert first event
			events.insert(e);
		}

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
	}

	/**
	 * This method is called if an unhandled exception occurs during the main of a
	 * simulation run. The implementation here just prints an appropriate message
	 * and then rethrows the Exception, terminating the simulation run.
	 * 
	 * @param t The Error or RuntimeException that was triggered somewhere in
	 *          simulation code.
	 * @return Whether or not to rethrow the Exception after processing.
	 */
	protected boolean handleError(Throwable t) {
		String errorString = Util.exceptionToString(t);

		printFmt(MsgCategory.ERROR, "An uncaught exception occurred. Current event='%s', exception='%s'",
				currentEvent(), errorString);

		return true;
	}

	private String createErrorMsgEventInPast(Event e) {
		return String.format(Util.DEF_LOCALE,
				"Can't schedule an event that is in the past (time to schedule: %f, prio=%d, event=%s).", e.getTime(),
				e.getPrio(), e.toString());
	}

	/**
	 * Override this method to perform initializations after {@link #init()}, but
	 * before running the simulation. This method is usually used to schedule
	 * initial events. It is executed automatically at the beginning of the
	 * {@link #run()} method.
	 */
	public void beforeRun() {
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
	 * This method is called once after {@link #beforeRun()} and immediately before
	 * the main simulation loop starts. It is called a second time if the simulation
	 * has a a value for statsResetTime() set.
	 * <p>
	 * It should contain code to initialize statistics variables.
	 */
	protected void resetStats() {
		// schedule statistics reset
		if (getStatsResetTime() > getInitialSimTime()) {
			schedule(getStatsResetTime(), Event.EVENT_PRIO_LOWEST, rootComponent::resetStats);
		}

		// call once for each run
		rootComponent.resetStats();
	}

	/**
	 * Override this method to perform some action after running the simulation, but
	 * before {@link #done()} is called. It is executed automatically at the end of
	 * the {@link #run()} method.
	 */
	public void afterRun() {
		rootComponent.afterRun();
	}

	/**
	 * Performs clean-up etc., after a simulation's {@link #run()} method finished.
	 */
	public void done() {
		rootComponent.done();
	}

	/**
	 * Convenience method calling {@link #init()}, {@link #beforeRun()},
	 * {@link #run()}, {@link #afterRun()}, {@link #done()} and returning the
	 * results produced by {@link #produceResults(Map)} in a new {@code HashMap}.
	 * 
	 * @return The results produced by the simulation and its components.
	 */
	public Map<String, Object> performRun() {
		init();
		beforeRun();
		run();
		afterRun();
		done();

		HashMap<String, Object> res = new HashMap<>();
		produceResults(res);
		return res;
	}

	/**
	 * Schedules a new event.
	 * 
	 * @param event Some future event to be executed by the main event loop.
	 */
	public void schedule(Event event) {
		if (event.getTime() == simTime && event.getPrio() <= currPrio) {
			printFmt(MsgCategory.WARN, "Priority inversion (current: %d, scheduled: %d, event=%s).", currPrio,
					event.getPrio(), event.toString());
		}
		if (event.getTime() < simTime) {
			String msg = createErrorMsgEventInPast(event);
			printFmt(MsgCategory.ERROR, msg);
			throw new IllegalArgumentException(msg);
		}
		event.eventNum = eventNum++;
		if (event.isAppEvent())
			numAppEvents++;
		events.insert(event);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time. Instead of
	 * calling this method it is usually better to use
	 * {@link #schedule(String, double, int, Runnable)} instead, as the additional
	 * description parameter usually makes debugging easier.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time).
	 * @param method The method to call at the given moment.
	 */
	public void schedule(double time, int prio, Runnable method) {
		schedule(null, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param time        The time when to call {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param method      The method to call at the given moment.
	 */
	public void schedule(String description, double time, int prio, Runnable method) {
		Event e = new MethodCallEvent(time, prio, method, description);
		schedule(e);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time given as a Java
	 * Instant. Usually using {@link #schedule(String, Instant, int, Runnable)}
	 * should be preferred.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time).
	 * @param method The method to call at the given moment.
	 */
	public void schedule(Instant time, int prio, Runnable method) {
		schedule(null, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time given as a Java
	 * Instant.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param time        The time when to call {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param method      The method to call at the given moment.
	 */
	public void schedule(String description, Instant time, int prio, Runnable method) {
		schedule(description, instantToSimTime(time), prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #schedule(double, int, Runnable)} this method expects a relative
	 * time instead of an absolute one.
	 * <p>
	 * Usually using {@link #scheduleIn(String, double, int, Runnable)} should be
	 * preferred.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time).
	 * @param method The method to call at the given moment.
	 */
	public void scheduleIn(double time, int prio, Runnable method) {
		scheduleIn(null, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #schedule(double, int, Runnable)} this method expects a relative
	 * time instead of an absolute one.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param time        The time when to call {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param method      The method to call at the given moment.
	 */
	public void scheduleIn(String description, double time, int prio, Runnable method) {
		schedule(description, simTime() + time, prio, method);
	}

	/**
	 * Periodically calls a certain method. While this method returns true, a next
	 * invocation after the given time interval is scheduled.
	 */
	public void schedulePeriodically(double firstInvocation, double interval, int prio, BooleanSupplier method) {
		schedule(new Event(firstInvocation, prio) {
			private int n = 0;

			@Override
			public void handle() {
				if (method.getAsBoolean()) {
					// schedule next invocation reusing Event object
					setTime(firstInvocation + (++n) * interval);
					schedule(this);
				}
			}
		});
	}

	/**
	 * Periodically calls a certain method until the simulation terminates.
	 */
	public void schedulePeriodically(double firstInvocation, double interval, int prio, Runnable method) {
		schedule(new Event(firstInvocation, prio) {
			private int n = 0;

			@Override
			public void handle() {
				method.run();
				// schedule next invocation reusing Event object
				setTime(firstInvocation + (++n) * interval);
				schedule(this);
			}
		});
	}

	/**
	 * Calls a certain method at the times returned by the method itself. The first
	 * invocation is performed at the current time (asynchronously, i.e.,
	 * {@code scheduleProcess()} returns before {@code method} is called for the
	 * first time). Subsequent calls are scheduled at the absolute times returned by
	 * the previous method invocation. No more invocations are scheduled if
	 * {@code method} returned NaN or a negative value.
	 */
	public void scheduleProcess(int prio, DoubleSupplier method) {
		scheduleProcess(Math.max(simTime(), getInitialSimTime()), prio, method);
	}

	/**
	 * Calls a certain method at the times returned by the method itself. The first
	 * invocation is performed at {@code firstInvocation}. Subsequent calls are
	 * scheduled at the absolute times returned by the previous method invocation.
	 * No more invocations are scheduled if {@code method} returned NaN or a
	 * negative value.
	 */
	public void scheduleProcess(double firstInvocation, int prio, DoubleSupplier method) {
		schedule(new Event(firstInvocation, prio) {
			@Override
			public void handle() {
				double next = method.getAsDouble();
				if (next >= 0.0) {
					// schedule next invocation reusing Event object
					setTime(next);
					schedule(this);
				}
			}
		});
	}

	/**
	 * This class is used internally by {@link #schedule(double,int,Runnable)}.
	 */
	protected static final class MethodCallEvent extends Event {
		public final Runnable m;

		private MethodCallEvent(double time, int prio, Runnable method, String description) {
			super(time, prio, description);
			m = method;
		}

		@Override
		public void handle() {
			m.run();
		}

		@Override
		public String toString() {
			return getDescription() != null ? getDescription() : String.format("MethodCallEvent(%s)", m.toString());
		}
	}

	/**
	 * After calling end() the simulation is terminated (after handling the current
	 * event).
	 */
	public void end() {
		continueSim = false;
	}

	/** Returns the current simulation time. */
	public double simTime() {
		return simTime;
	}

	/**
	 * Converts the current simulation time to a Java {@link Instant}.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public Instant simTimeToInstant() {
		return simTimeToInstant(simTime());
	}

	/**
	 * Converts the given simulation time to a Java {@link Instant} (UTC time
	 * stamp). Conversion multiplies the time with the factor
	 * {@link #getSimTimeToMillisFactor()} and rounds the results to the closest
	 * integer to get the number of milliseconds since a simTime of 0. This amount
	 * of milliseconds is then added to {@link #getSimTimeStartInstant()} to get an
	 * absolute Java time stamp.
	 * 
	 * @see #setSimTimeStartInstant(Instant)
	 * @see #setSimTimeToMillisFactor(long)
	 */
	public Instant simTimeToInstant(double simTime) {
		long simTimeMillis = Math.round((simTime - getInitialSimTime()) * simTimeToMillisFactor);
		return getSimTimeStartInstant().plus(simTimeMillis, ChronoUnit.MILLIS);
	}

	/**
	 * Converts a given Java {@link Instant} (UTC time stamp) to the simulation time
	 * it corresponds to.
	 * 
	 * @param instant The instant to be converted to simulation time.
	 * @return The instant converter to simulation time.
	 */
	public double instantToSimTime(Instant instant) {
		long durationMillis = instant.toEpochMilli() - getSimTimeStartInstant().toEpochMilli();
		return durationMillis / simTimeToMillisFactor + getInitialSimTime();
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
	 * Convenience method to get a component by its name given a fully qualified
	 * name such as "container1.sub1.myMachine".
	 */
	public SimComponent getComponentByHierarchicalName(String hierarchicalName) {
		return getRootComponent().getComponentByHierarchicalName(hierarchicalName);
	}

	/**
	 * Triggers a print event for the given message of category "INFO".
	 * 
	 * @param message The message to print.
	 * @see #print(MsgCategory, String)
	 */
	public void print(String message) {
		print(MsgCategory.INFO, message);
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener is
	 * installed, this should produce an output of {@code message}.
	 * 
	 * @param message The message to print.
	 */
	public void print(MsgCategory category, String message) {
		if (numPrintListener() > 0 && category.ordinal() <= getPrintLevel().ordinal()) {
			print(new SimPrintMessage(this, category, message));
		}
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener is
	 * installed, this should produce an output of {@code message}.
	 */
	public void print(MsgCategory category, Object... params) {
		if (numPrintListener() > 0 && category.ordinal() <= getPrintLevel().ordinal()) {
			print(new SimPrintMessage(this, category, params));
		}
	}

	/**
	 * Prints a certain {@link SimPrintMessage} by passing it to the registered
	 * print listeners.
	 */
	protected void print(SimPrintMessage e) {
		printListener.forEach(l -> l.accept(e));
	}

	/**
	 * Produces a trace message (if there are any print listeners such as
	 * {@link TraceFileProducer} are registered that do something with such
	 * messages). A trace message consists of the simulation time and all parameters
	 * converted to Strings (separated by tabs).
	 * 
	 * @param params The components of the trace message.
	 */
	public void trace(Object... params) {
		print(MsgCategory.TRACE, params);
	}

	/**
	 * @return Whether or not trace messages are to be produced.
	 */
	public boolean isTraceEnabled() {
		return getPrintLevel().ordinal() >= MsgCategory.TRACE.ordinal();
	}

	/**
	 * 
	 * @return The current maximum print message category.
	 */
	public MsgCategory getPrintLevel() {
		return printLevel;
	}

	/**
	 * Sets the maximum print message category to be forwared to the print
	 * listeners. If this is set to e.g. INFO, then only messages of the categories
	 * ERROR, WARN and INFO are forwared to
	 * 
	 * @param printLevel
	 */
	public void setPrintLevel(MsgCategory printLevel) {
		Objects.requireNonNull(printLevel);
		this.printLevel = printLevel;
	}

	/**
	 * Triggers a print event of the given category with the message produced by a
	 * Java format String. If an appropriate listener is installed, this produces a
	 * message defined by the format string {@code messageFormatString} (used with
	 * the arguments given in {@code params}).
	 */
	public void printFmt(MsgCategory category, String messageFormatString, Object... params) {
		if (numPrintListener() > 0 && category.ordinal() <= getPrintLevel().ordinal()) {
			// lazy message creation
			Object msgProducer = new Object() {
				@Override
				public String toString() {
					return String.format(Util.DEF_LOCALE, messageFormatString, params);
				}
			};

			print(new SimPrintMessage(this, category, msgProducer));
		}
	}

	/**
	 * Same as {@link #printFmt(MsgCategory, String, Object...)}, but defaulting to
	 * category {@code INFO}.
	 */
	public void printFmt(String messageFormatString, Object... params) {
		printFmt(MsgCategory.INFO, messageFormatString, params);
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
		if (!(simulationLength >= 0.0)) {
			throw new IllegalArgumentException("" + simulationLength);
		}

		this.simulationLength = simulationLength;
	}

	/**
	 * @return The maximum simulation time; a value of 0.0 means no such limit.
	 */
	public double getSimulationLength() {
		return simulationLength;
	}

	/** @return The RandomFactory used to create random number streams. */
	public RandomFactory getRndStreamFactory() {
		return rndStreamFactory;
	}

	/**
	 * Sets the random factory to use to create random number streams for stochastic
	 * simulations.
	 */
	public void setRndStreamFactory(RandomFactory rndStreamFactory) {
		this.rndStreamFactory = rndStreamFactory;
		rndStreamFactory.setSim(this);
	}

	/**
	 * Initializes the random number generator associated with the {@link DblStream}
	 * {@code s}. This just delegates to the {@link RandomFactory} of this
	 * simulation.
	 */
	public DblStream initRndGen(DblStream s, String streamName) {
		return getRndStreamFactory().initRndGen(s, streamName);
	}

	/**
	 * Initializes the random number generator associated with the {@link IntStream}
	 * {@code s}. This just delegates to the {@link RandomFactory} of this
	 * simulation.
	 */
	public IntStream initRndGen(IntStream s, String streamName) {
		return (IntStream) getRndStreamFactory().initRndGen(s, streamName);
	}

	/**
	 * Creates an instance of Java's {@code Random} class initialized with a seed
	 * derived from the parameter {@code streamName}. This just delegates to the
	 * method {@link RandomFactory#createInstance(String)} of this simulation.
	 */
	public Random initRndGen(String streamName) {
		return getRndStreamFactory().createInstance(streamName);
	}

	/** @return The name of this simulation. */
	public String getName() {
		return name;
	}

	/**
	 * Sets a name for this simulation.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The root component of all {@link SimComponent}s contained in the
	 *         simulation.
	 */
	public SimComponentContainer<SimComponent> getRootComponent() {
		return rootComponent;
	}

	/**
	 * Sets the root component containing all permanent {@link SimComponent}s
	 * contained in this simulation.
	 * 
	 * @param rootComponent The new root component.
	 */
	protected void setRootComponent(SimComponentContainer<SimComponent> rootComponent) {
		if (this.rootComponent != null) {
			this.rootComponent.setSim(null);
		}

		this.rootComponent = rootComponent;
		rootComponent.setSim(this);
	}

	public double getInitialSimTime() {
		return initialSimTime;
	}

	/** Sets the initial value of the simulation clock. */
	public void setInitialSimTime(double initialSimTime) {
		this.initialSimTime = initialSimTime;
	}

	/**
	 * Returns the {@link Instant} corresponding to simulation time 0.
	 * 
	 * @see #setSimTimeStartInstant(Instant)
	 */
	public Instant getSimTimeStartInstant() {
		return simTimeStartInstant;
	}

	/**
	 * Sets the {@link Instant} corresponding to the a simulation time of 0. The
	 * default setting is to use the beginning of the current year.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public void setSimTimeStartInstant(Instant simTimeStartInstant) {
		this.simTimeStartInstant = simTimeStartInstant;
	}

	/**
	 * Returns the factor used to convert the (double-valued) simulation time to
	 * milli-seconds since {@link #getSimTimeStartInstant()}.
	 * 
	 * @see #setSimTimeToMillisFactor(long)
	 */
	public long getSimTimeToMillisFactor() {
		return simTimeToMillisFactor;
	}

	/**
	 * Returns the statistics reset time.
	 */
	public double getStatsResetTime() {
		return statsResetTime;
	}

	/**
	 * Sets the time when to perform a statistics reset. The value set here will
	 * only have an effect before the simulation is started.
	 * 
	 * @param statsResetTime The new statistics reset time.
	 */
	public void setStatsResetTime(double statsResetTime) {
		this.statsResetTime = statsResetTime;
	}

	/**
	 * Sets the factor used to convert the (double-valued) simulation time to
	 * milli-seconds since {@link #getSimTimeStartInstant()}. The default value is
	 * 60*1000=60000, assuming simulation time to be in minutes.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public void setSimTimeToMillisFactor(long simTimeToMillisFactor) {
		this.simTimeToMillisFactor = simTimeToMillisFactor;
	}

	@Override
	protected Simulation clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(); // not implemented yet
	}

}
