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

import static jasima.core.simulation.Simulation.SimExecState.BEFORE_RUN;
import static jasima.core.simulation.Simulation.SimExecState.INIT;
import static jasima.core.simulation.Simulation.SimExecState.INITIAL;
import static jasima.core.simulation.Simulation.SimExecState.PAUSED;
import static jasima.core.simulation.Simulation.SimExecState.RUNNING;
import static jasima.core.util.ComponentStates.requireAllowedState;
import static jasima.core.util.SimProcessUtil.simActionFromRunnable;
import static jasima.core.util.TypeUtil.createInstance;
import static jasima.core.util.i18n.I18n.defFormat;
import static java.util.Collections.unmodifiableList;
import static java.util.EnumSet.complementOf;
import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import jasima.core.experiment.Experiment;
import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation.SimLifecycleEvent;
import jasima.core.simulation.util.ProcessActivator;
import jasima.core.simulation.util.SimComponentRoot;
import jasima.core.simulation.util.SimOperations;
import jasima.core.util.MsgCategory;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimRunnable;
import jasima.core.util.TraceFileProducer;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;
import jasima.core.util.ValueStore;
import jasima.core.util.ValueStoreImpl;
import jasima.core.util.i18n.I18n;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierImpl;
import jasima.core.util.observer.ObservableValue;

/**
 * Base class for a discrete event simulation. This class mainly maintains the
 * event queue and manages simulation time. Additionally it offers a centralized
 * place to initialize random number streams and to create status and debug
 * messages.
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
public class Simulation
		implements ValueStore, SimOperations, ProcessActivator, Notifier<Simulation, SimLifecycleEvent> {

	public interface SimLifecycleEvent {
	}

	public enum StdSimLifecycleEvents implements SimLifecycleEvent {
		INIT, SIM_START, RESET_STATS, SIM_END, DONE
	}

	/**
	 * Message send when all {@link SimComponent}s are requested to produce results.
	 * 
	 * @author Torsten Hildebrandt
	 */
	public static class ProduceResultsMessage implements SimLifecycleEvent {

		public final Map<String, Object> resultMap;

		public ProduceResultsMessage(Map<String, Object> resultMap) {
			this.resultMap = resultMap;
		}

		@Override
		public String toString() {
			return "ProduceResultsMsg";
		}

	}

	// event notification
	private NotifierImpl<Simulation, SimLifecycleEvent> notifierAdapter;

	@Override
	public NotifierImpl<Simulation, SimLifecycleEvent> notifierImpl() {
		if (notifierAdapter == null)
			notifierAdapter = new NotifierImpl<>(this);

		return notifierAdapter;
	}

	// result value name for simulation time at end of simulation run
	public static final String SIM_TIME = "simTime";

	public static final String QUEUE_IMPL_KEY = "jasima.core.simulation.Simulation.queueImpl";
	public static final Class<? extends EventQueue> queueImpl = TypeUtil.getClassFromSystemProperty(QUEUE_IMPL_KEY,
			EventQueue.class, EventHeap.class);

	@FunctionalInterface
	public interface ErrorHandler extends Predicate<Exception> {
		boolean test(Exception e);
	}

	public static class SimulationFailed extends RuntimeException {

		private static final long serialVersionUID = 4068987513637601189L;

		SimulationFailed(String msg, Throwable cause) {
			super(msg, cause);
		}
	}

	public enum SimExecState {
		INITIAL, INIT, BEFORE_RUN, RUNNING, PAUSED, TERMINATING, FINISHED, ERROR
	}

	///////////// static methods, delegating to SimContext

	public static Map<String, Object> of(SimRunnable r) {
		return SimContext.simulationOf(r);
	}

	public static Map<String, Object> of(String name, SimRunnable r) {
		return SimContext.simulationOf(name, r);
	}

	public static Map<String, Object> of(SimComponent... components) {
		return SimContext.simulationOf(components);
	}

	public static Map<String, Object> of(String name, SimComponent... components) {
		return SimContext.simulationOf(name, components);
	}

	public static Map<String, Object> of(SimAction a) {
		return SimContext.simulationOf(a);
	}

	public static Map<String, Object> of(String name, SimAction a) {
		return SimContext.simulationOf(name, a);
	}

	///////////// simulation parameters

	private double simulationLength = 0.0d;
	private double initialSimTime = 0.0d;
	private int initialEventPriority = SimEvent.EVENT_PRIO_NORMAL;
	private double statsResetTime = 0.0d;
	private RandomFactory rndStreamFactory;
	private String name = null;
	private long simTimeToMillisFactor = Duration.ofMinutes(1).toMillis(); // simulation time in minutes
	private Instant simTimeStartInstant;
	private ErrorHandler errorHandler = null;

	// delegate ValueStore functionality
	private ValueStoreImpl valueStore;

	private SimComponentRoot rootComponent;

	private MsgCategory printLevel = MsgCategory.INFO;
	private ArrayList<Consumer<SimPrintMessage>> printListener;

	private Locale locale = I18n.DEF_LOCALE;
	private ZoneId zoneId = ZoneId.of("UTC");

	private SimAction mainProcessActions = null;

	// ////////////// attributes/fields used during a simulation run

	private double simTime;
	private int currPrio;
	private SimEvent currEvent;
	private long numEventsProcessed;

	private Clock clock; // optionally access simulation time as Java Clock

	private boolean awakePausedWorker;
	private Thread pausedWorkerThread;

	private ConcurrentLinkedQueue<SimAction> runInSimThread;

	// event queue
	private EventQueue events;
	// eventNum is used to enforce FIFO-order of concurrent events with equal
	// priorities
	private int eventNum;
	private int numAppEvents;

	final ObservableValue<SimExecState> state;
	private volatile boolean endRequested;

	private AtomicInteger pauseRequests;

	private Exception execFailure;

	private SimEvent simEndEvent;

	private SimProcess<?> mainProcess;
	private Set<SimProcess<?>> runnableProcesses;
	private SimProcess<?> currentProcess;
	private SimProcess<?> eventLoopProcess;

	private Map<String, Object> additionalResults;
	private Map<String, Object> res; // set only temporarily during produceResults

	public Simulation() {
		super();

		state = new ObservableValue<>(INITIAL);

		pauseRequests = new AtomicInteger(0);
		runInSimThread = new ConcurrentLinkedQueue<>();
		printListener = new ArrayList<>();
		valueStore = new ValueStoreImpl();
		runnableProcesses = new HashSet<>();

		RandomFactory randomFactory = RandomFactory.newInstance();
		setRndStreamFactory(randomFactory);

		events = createEventQueue();
		// set to dummy event
		currEvent = new SimEvent(Double.NEGATIVE_INFINITY, SimEvent.EVENT_PRIO_MAX) {
			@Override
			public void handle() {
			}
		};
		currPrio = currEvent.getPrio();
		simTime = currEvent.getTime();
		eventNum = Integer.MIN_VALUE;
		numAppEvents = 0;
		numEventsProcessed = 0;

		LocalDate yearBeg = LocalDate.of(Year.now(Clock.systemUTC()).getValue(), 1, 1);
		simTimeStartInstant = yearBeg.atStartOfDay(ZoneOffset.UTC).toInstant();

		rootComponent = new SimComponentRoot();
		addListener(rootComponent);
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
		requireAllowedState(state.get(), INITIAL);
		state.set(SimExecState.INIT);
		simTime = getInitialSimTime();
		currPrio = getInitialEventPriority();
		additionalResults = new LinkedHashMap<String, Object>();

		initComponentTree(null, rootComponent);
		fire(StdSimLifecycleEvents.INIT);
	}

	/**
	 * Recursively initialize components before run by setting simulation and parent
	 * node.
	 * 
	 * @param parent
	 * @param child
	 */
	protected void initComponentTree(SimComponent parent, SimComponent child) {
		child.setParent(parent);
		child.setSim(this);

		child.getChildren().forEach(c -> initComponentTree(child, c));
	}

	/**
	 * Runs the main simulation loop. This means:
	 * <ol>
	 * <li>taking an event from the event queue,
	 * <li>advancing simulation time, and
	 * <li>triggering event processing.
	 * </ol>
	 * A simulation is terminated if either the maximum simulation length is
	 * reached, there are no more application events in the queue, or the method
	 * {@link #end()} was called.
	 * 
	 * @see jasima.core.simulation.SimEvent#isAppEvent()
	 */
	public void run() {
		requireAllowedState(state.get(), SimExecState.BEFORE_RUN);

		mainProcess = new SimProcess<>(this, getMainProcessActions(), "simMain");
		currEvent = mainProcess.activateProcessEvent;
		setCurrentProcess(mainProcess);
		setEventLoopProcess(mainProcess);

		execFailure = null;

		state.set(SimExecState.RUNNING);
		resetStats();

		checkInitialEventTime();

		// dummy event so we have enough time to process runInSimThread list
//		scheduleIn(0.0, currentPrio() + 1, () -> {
//		});

		// we have to call run() to initialize 'mainProcess' properly in order to start
		// executing the main event loop
		mainProcess.activateProcess();
		mainProcess.run();

		terminateRunningProcesses();

		if (execFailure == null) {
			state.set(SimExecState.FINISHED);
		} else {
			state.set(SimExecState.ERROR);
			throw new SimulationFailed("There was an unrecoverable error during simulation run.", execFailure);
		}
	}

	private void terminateRunningProcesses() {
		for (SimProcess<?> p : runnableProcesses()) {
			if (p != mainProcess() && p.executor != null) {
				p.terminateWaiting();
				while (p.executor != null)
					; // active wait until finished (should be very quick)
			}
		}
	}

	void handleNextEvent() {
		// run additional actions that might come from external threads
		SimAction r;
		while ((r = runInSimThread.poll()) != null) {
			try {
				r.run(this);
			} catch (MightBlock e) {
				throw new AssertionError(); // ignore marker Exception
			}
		}

		if (!continueSim()) {
			return;
		}

		// determine next event
		SimEvent evt = events.extract();
		currEvent = evt;

		// Advance clock to time of next event
		simTime = evt.getTime();
		currPrio = evt.getPrio();
		if (evt.isAppEvent()) {
			--numAppEvents;
		}
		numEventsProcessed++;

		runEventHandler(evt);
	}

	/**
	 * Runs the handle-method of {@code evt} (see {@link SimEvent#handle()}. This
	 * method exists to allow executing custom code before and after each event by
	 * sub-classing {@link Simulation}.
	 * 
	 * @param evt The event to execute
	 */
	protected void runEventHandler(SimEvent evt) {
		evt.handle();
	}

	private void checkInitialEventTime() {
		// ensure time of first event is before initalSimTime, then put in event
		// queue again; this is done just once to move the check outside the main loop.
		if (continueSim()) {
			SimEvent e = events.extract();
			if (e.getTime() < simTime) {
				throw new IllegalArgumentException(createErrorMsgEventInPast(e, simTime));
			}

			// everything is ok, reinsert first event
			events.insert(e);
		}
	}

	/**
	 * This method is called if an unhandled exception occurs during the run phase
	 * of a simulation run. The implementation here just prints an appropriate
	 * message and then rethrows the exception, terminating the simulation run.
	 * 
	 * @param e The Exception that was triggered somewhere in simulation code.
	 * @return Whether or not to rethrow the Exception after processing.
	 */
	boolean handleError(Exception e) {
		return (getErrorHandler() != null) ? getErrorHandler().test(e) : defaultErrorHandler(e);
	}

	protected boolean defaultErrorHandler(Exception e) {
		String errorString = Util.exceptionToString(e);

		printFmt(MsgCategory.ERROR, "An uncaught exception occurred. Current event='%s', exception='%s'",
				currentEvent(), errorString);

		return true;
	}

	private String createErrorMsgEventInPast(SimEvent e, double simTime) {
		return defFormat(
				"Can't schedule an event that is in the past (time to schedule: %f, simTime: %f, prio=%d, event=%s).",
				e.getTime(), simTime, e.getPrio(), e.toString());
	}

	/**
	 * Override this method to perform initializations after {@link #init()}, but
	 * before running the simulation. This method is usually used to schedule
	 * initial events. It is executed automatically at the beginning of the
	 * {@link #run()} method.
	 */
	public void beforeRun() {
		requireAllowedState(state.get(), SimExecState.INIT);
		state.set(SimExecState.BEFORE_RUN);

		simEndEvent = new SimEvent(getInitialSimTime() + getSimulationLength(), SimEvent.EVENT_PRIO_MIN) {
			@Override
			public void handle() {
				// check again because simLength might have changed during the simulation run
				if (simTime() == getTime()) {
					end();
				}
			}
		};
		if (getSimulationLength() > 0.0) {
			schedule(simEndEvent);
		}

		fire(StdSimLifecycleEvents.SIM_START);
	}

	/**
	 * This method is called once after {@link #beforeRun()} and immediately before
	 * the main simulation loop starts. It is called a second time if the simulation
	 * has a a value for statsResetTime() set.
	 * <p>
	 * It should contain code to initialize statistics variables.
	 */
	protected void resetStats() {
		requireAllowedState(state.get(), SimExecState.RUNNING);

		// schedule statistics reset
		if (getStatsResetTime() > getInitialSimTime()) {
			// TODO: change prio to HIGHEST?
			scheduleAt("statsReset", getStatsResetTime(), SimEvent.EVENT_PRIO_LOWEST,
					() -> fire(StdSimLifecycleEvents.RESET_STATS));
		}

		// call once for each run
		fire(StdSimLifecycleEvents.RESET_STATS);
	}

	/**
	 * Override this method to perform some action after running the simulation, but
	 * before {@link #done()} is called. It is executed automatically at the end of
	 * the {@link #run()} method.
	 */
	public void afterRun() {
		fire(StdSimLifecycleEvents.SIM_END);
	}

	/**
	 * Performs clean-up etc., after a simulation's {@link #run()} method finished.
	 */
	public void done() {
		fire(StdSimLifecycleEvents.DONE);
	}

	/**
	 * Convenience method calling {@link #init()}, {@link #beforeRun()},
	 * {@link #run()}, {@link #afterRun()}, {@link #done()} and returning the
	 * results produced by {@link #produceResults(Map)} in a new {@code HashMap}.
	 * 
	 * @return The results produced by the simulation and its components.
	 */
	public Map<String, Object> performRun() {
		SimContext.setThreadContext(this);
		try {
			long runTimeReal = System.currentTimeMillis();

			init();
			beforeRun();
			run();
			afterRun();
			done();

			runTimeReal = System.currentTimeMillis() - runTimeReal;

			Map<String, Object> res = new LinkedHashMap<>();
			res.put(Experiment.RUNTIME, runTimeReal / 1000.0);
			produceResults(res);

			return res;
		} finally {
			SimContext.setThreadContext(null);
		}
	}

	/**
	 * Call the {@link #performRun()} method in an asynchronous way.
	 * 
	 * @param pool The {@link ExecutorService} to use.
	 * @return A {@link Future} to obtain the simulation results.
	 * @see #performRun()
	 * @see #performRunAsync()
	 */
	public Future<Map<String, Object>> performRunAsync(ExecutorService pool) {
		return pool.submit(this::performRun);
	}

	/**
	 * Trigger asynchronous execution of the simulation in the default thread pool.
	 * 
	 * @return A {@link Future} to obtain the simulation results.
	 * @see #performRun()
	 * @see #performRunAsync(ExecutorService)
	 */
	public Future<Map<String, Object>> performRunAsync() {
		return performRunAsync(Util.DEF_POOL);
	}

	/**
	 * Removes the given event object from the event queue.
	 * 
	 * @param event the event to remove
	 * @return {@code true} if the operation was present in the event queue and
	 *         could be successfully removed, {@code false} otherwise
	 */
	public boolean unschedule(SimEvent event) {
		return events.remove(event);
	}

	/**
	 * Schedules a new event.
	 * 
	 * @param event Some future event to be executed by the main event loop.
	 */
	@Override
	public SimEvent schedule(SimEvent event) {
		if (event.getTime() == simTime && event.getPrio() <= currPrio) {
			printFmt(MsgCategory.WARN, "Priority inversion (current: %d, scheduled: %d, event=%s).", currPrio,
					event.getPrio(), event.toString());
		}
		if (event.getTime() < simTime) {
			String msg = createErrorMsgEventInPast(event, simTime);
			printFmt(MsgCategory.ERROR, msg);
			throw new IllegalArgumentException(msg);
		}

		event.eventNum = eventNum++;
		if (event.isAppEvent())
			numAppEvents++;
		events.insert(event);

		return event;
	}

	/**
	 * Periodically calls a certain method. While this method returns true, a next
	 * invocation after the given time interval is scheduled.
	 */
	public void schedulePeriodically(double firstInvocation, double interval, int prio, BooleanSupplier method) {
		schedule(new SimEvent(firstInvocation, prio) {
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
		schedule(new SimEvent(firstInvocation, prio) {
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
		schedule(new SimEvent(firstInvocation, prio) {
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
	 * {@inheritDoc}
	 */
	@Override
	public void end() {
		endRequested = true;
		if (pauseRequests.get() > 0) {
			awakePausedWorker = true;
			LockSupport.unpark(pausedWorkerThread);
			pausedWorkerThread = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEndRequested() {
		return endRequested;
	}

	/**
	 * After calling {@link #pause()} the simulation is paused. This means, the
	 * {@link #run()} method returns after handling the current event.
	 * <p>
	 * This method might also be called from an external thread.
	 */
	public void pause() {
		requireAllowedState(state.get(), complementOf(EnumSet.of(SimExecState.FINISHED, SimExecState.ERROR)));

		if (pauseRequests.incrementAndGet() == 1) {
			awakePausedWorker = false;
			runInSimThread(() -> {
				// check again because it might have changed while waiting to be processed
				if (pauseRequests.get() > 0) {
					assert state.get() == SimExecState.RUNNING;

					state.set(SimExecState.PAUSED);
					pausedWorkerThread = Thread.currentThread();
					while (!(awakePausedWorker || pausedWorkerThread.isInterrupted())) {
						LockSupport.park();
					}

					state.set(SimExecState.RUNNING);
				}
			});
		}
	}

	/**
	 * After calling {@link #unpause()} a paused simulation is continued. Internally
	 * each pause request increases a counter that has to be followed by an unpause
	 * request. Simulation only resumes if the pause counter reaches zero.
	 * <p>
	 * This method might also be called from an external thread.
	 */
	public void unpause() {
		requireAllowedState(state.get(), SimExecState.PAUSED, SimExecState.RUNNING);
		if (pauseRequests.decrementAndGet() == 0) {
			if (pausedWorkerThread != null) {
				awakePausedWorker = true;
				LockSupport.unpark(pausedWorkerThread);
				pausedWorkerThread = null;
			}
		}
	}

	/** Returns the current simulation time. */
	@Override
	public double simTime() {
		if (state.get() == INITIAL)
			throw new IllegalStateException("simTime() is undefined for an uninitialized simulation.");
		return simTime;
	}

	/**
	 * Returns a Java {@link Clock} object using the simulation time and its time
	 * zone.
	 */
	public Clock clock() {
		if (clock != null) {
			clock = new SimulationClock(this, zoneId);
		}
		return clock;
	}

	/**
	 * Converts the current simulation time to a Java {@link LocalDateTime}. This
	 * converts the current simulation time to an {@code Instant} first (see
	 * {@link #simTimeAbs()}) and then converts it to the local time for this
	 * simulation's zone id ({@link #getZoneId()}.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public LocalDateTime simTimeToLocalDateTime() {
		return simTimeToLocalDateTime(simTimeToInstant(simTime()));
	}

	/**
	 * Converts a simulation time to a Java {@link LocalDateTime}. The simulation
	 * time is converted to an {@code Instant} first (see {@link #simTimeAbs()}) and
	 * then converted to the local time for this simulation's zone id (see
	 * {@link #getZoneId()}.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public LocalDateTime simTimeToLocalDateTime(double simTime) {
		return simTimeToLocalDateTime(simTimeToInstant(simTime));
	}

	/**
	 * Converts the given {@code Instant} to a local date/time at this simulation's
	 * zone id ({@link #getZoneId()}.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public LocalDateTime simTimeToLocalDateTime(Instant instant) {
		return LocalDateTime.ofInstant(requireNonNull(instant), getZoneId());
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
	@Override
	public Instant simTimeToInstant(double simTime) {
		long simTimeMillis = Math.round((simTime - getInitialSimTime()) * simTimeToMillisFactor);
		return getSimTimeStartInstant().plus(simTimeMillis, ChronoUnit.MILLIS);
	}

	/**
	 * Converts the given simulation time span to a Java {@link Duration}.
	 */
	public Duration simTimeToDuration(double simTime) {
		double millis = simTime * simTimeToMillisFactor;
		return Duration.of(Math.round(millis), ChronoUnit.MILLIS);
	}

	/**
	 * Converts a given Java {@link Instant} (absolute UTC time stamp) to the
	 * simulation time it corresponds to.
	 * 
	 * @param instant The instant to be converted to simulation time.
	 * @return The instant converted to simulation time.
	 */
	@Override
	public double toSimTime(Instant instant) {
		double durationMillis = instant.toEpochMilli() - getSimTimeStartInstant().toEpochMilli();
		return durationMillis / simTimeToMillisFactor + getInitialSimTime();
	}

	/**
	 * Converts a given Java {@link Duration} (i.e., a time span) to the
	 * corresponding (relative) simulation time.
	 * 
	 * @param d The duration to be converted to simulation time.
	 * @return The amount of simulation time.
	 */
	@Override
	public double toSimTime(Duration d) {
		double millis = d.toMillis();
		return millis / simTimeToMillisFactor;
	}

	/**
	 * Converts a given number of {@link TemporalUnit}s to the corresponding
	 * simulation time, like
	 * {@code double time = sim.toSimTime(5, ChronoUnit.MINUTE)}. Internally this
	 * creates a temporary {@link Duration} object and then calls
	 * {@link #toSimTime(Duration)}.
	 * 
	 * @param numUnits the amount of time units
	 * @param u        the temporal unit to use; will usually be on from
	 *                 {@link ChronoUnit}
	 * @return The amount of simulation time.
	 */
	@Override
	public double toSimTime(long numUnits, TemporalUnit u) {
		return toSimTime(Duration.of(numUnits, u));
	}

	/**
	 * Returns the priority of the currently processed event.
	 */
	public int currentPrio() {
		if (state.get() == INITIAL)
			throw new IllegalStateException("currentPrio() is undefined for an uninitialized simulation.");
		return currPrio;
	}

	/**
	 * Returns the {@link SimEvent} object that is currently processed.
	 */
	public SimEvent currentEvent() {
		return currEvent;
	}

	/**
	 * Returns the currently active {@link SimProcess}.
	 */
	public @Nullable SimProcess<?> currentProcess() {
		return currentProcess;
	}

	/**
	 * Returns the current simulation execution state.
	 */
	public SimExecState state() {
		return state.get();
	}

	/**
	 * Returns the current simulation execution state.
	 */
	public ObservableValue<SimExecState> observableState() {
		return state;
	}

	/**
	 * Returns the number of events processed by the main simulation loop.
	 */
	public long numEventsProcessed() {
		return numEventsProcessed;
	}

	/**
	 * Returns the number of normal, i.e., application events currently contained in
	 * the event queue.
	 */
	public long numAppEvents() {
		return numAppEvents;
	}

	/**
	 * Returns the total number of events (both application and utility events)
	 * currently contained in the event queue.
	 */
	public long numEvents() {
		return events.size();
	}

	/**
	 * Returns an ordered list of all events currently in the event queue. Use with
	 * care, this is an expensive operation. The list does not include the current
	 */
	public List<SimEvent> scheduledEvents() {
		return events.allEvents();
	}

	/**
	 * Populates the given HashMap with results produced in the simulation run.
	 */
	public void produceResults(Map<String, Object> res) {
		this.res = res;
		try {
			res.putAll(additionalResults);
			res.put(SIM_TIME, simTime());

			fire(new ProduceResultsMessage(res));
		} finally {
			res = null;
		}
	}

	/**
	 * Adds a certain result to the simulation's result Map.
	 * 
	 * @param name  Name of the result.
	 * @param value Result value.
	 */
	@Override
	public void addResult(String name, Object value) {
		if (res != null)
			// called during produceResults()
			res.put(name, value);
		else
			// anytime else
			additionalResults.put(name, value);
	}

	/**
	 * Activates the given entity but does not add it to the components tree.
	 * Therefore they will not be notified of any future simulation lifecycle events
	 * such as {@code produceResults}. It is therefore intended for temporary
	 * {@code SimEntity}s only.
	 */
	@Override
	public <T extends SimEntity> T activateEntity(T e) {
		return activateComponent(e);
	}

	/**
	 * Calls all lifecycle events on "sc" to be in sync with the simulation it is
	 * added to. This should happen automatically if a component was added to the
	 * simulation before the run or using the {@link #addComponent(SimComponent...)}
	 * method, but has to be called manually when components are added dynamically
	 * while the simulation is ongoing.
	 * <p>
	 * This method is primarily intended to be used by temporary components, in
	 * particular non-permanent {@link SimEntity}s. As they are not added to the
	 * component tree, they will not be notified of any future simulation lifecycle
	 * events such as {@code produceResults}.
	 * 
	 * @return same as parameter {@code sc} to allow chaining
	 */
	<T extends SimComponent> T activateComponent(T sc) {
		activateComponents(sc);
		return sc;
	}

	/**
	 * Same as {@link #activateComponent(SimComponent)}, but adds multiple
	 * components simultaneously.
	 * 
	 * @param scs The components to add.
	 */
	void activateComponents(SimComponent... scs) {
		requireAllowedState(state.get(), INIT, BEFORE_RUN, RUNNING, PAUSED);
		for (SimComponent sc : scs)
			sc.setSim(this);
		switch (state.get()) {
		case INITIAL:
			break; // do nothing
		case INIT:
			for (SimComponent sc : scs)
				sc.inform(this, StdSimLifecycleEvents.INIT);
			break;
		case BEFORE_RUN:
			// whenever more than one event is triggered it is important to call all init()s
			// first before calling simStart()
			for (SimComponent sc : scs)
				sc.inform(this, StdSimLifecycleEvents.INIT);
			for (SimComponent sc : scs)
				sc.inform(this, StdSimLifecycleEvents.SIM_START);
			break;
		case RUNNING:
		case PAUSED:
			for (SimComponent sc : scs)
				sc.inform(this, StdSimLifecycleEvents.INIT);
			for (SimComponent sc : scs)
				sc.inform(this, StdSimLifecycleEvents.SIM_START);
			break;
		default:
			throw new AssertionError();
		}
	}

	/**
	 * Convenience method to get a component by its name given a fully qualified
	 * name such as "container1.sub1.myMachine".
	 */
	public SimComponent getComponentByHierarchicalName(String hierarchicalName) {
		return getRootComponent().getByHierarchicalName(hierarchicalName);
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
		requireNonNull(printLevel);
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
					return defFormat(messageFormatString, params);
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
		return createInstance(queueImpl);
	}

	/** Sets the maximum simulation time. A value of 0.0 means no such limit. */
	public void setSimulationLength(double simulationLength) {
		if (!(simulationLength >= 0.0)) {
			throw new IllegalArgumentException("" + simulationLength);
		}

		this.simulationLength = simulationLength;

		if (simEndEvent != null) {
			double simEndTime = simTime() + getSimulationLength();
			if (simEndTime != simEndEvent.getTime()) {
				// schedule another invocation at the correct time
				simEndEvent.setTime(simEndTime);
				schedule(simEndEvent);
			}
		}
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
	 * Initializes the random number generator associated with the
	 * {@link DblSequence} {@code s}. This just delegates to the
	 * {@link RandomFactory} of this simulation.
	 */
	public <T extends DblSequence> T initRndGen(T s, String streamName) {
		return getRndStreamFactory().initRndGen(s, streamName);
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
	@Override
	public SimComponentRoot getRootComponent() {
		return rootComponent;
	}

//	/**
//	 * Sets the root component containing all permanent {@link SimComponent}s
//	 * contained in this simulation.
//	 * 
//	 * @param rootComponent The new root component.
//	 */
//	protected void setRootComponent(SimComponentContainer rootComponent) {
//		if (this.rootComponent != null) {
//			this.rootComponent.setSim(null);
//		}
//
//		this.rootComponent = rootComponent;
//		rootComponent.setSim(this);
//	}

	public double getInitialSimTime() {
		return initialSimTime;
	}

	/** Sets the initial value of the simulation clock. */
	public void setInitialSimTime(double initialSimTime) {
		requireAllowedState(state.get(), SimExecState.INIT, SimExecState.INITIAL);
		this.initialSimTime = initialSimTime;
	}

	public int getInitialEventPriority() {
		return initialEventPriority;
	}

	/**
	 * Sets the initial priority value (default=0, i.e.,
	 * {@code SimEvent.EVENT_PRIO_NORMAL}).
	 */
	public void setInitialEventPriority(int initialEventPriority) {
		this.initialEventPriority = initialEventPriority;
	}

	/**
	 * Returns the {@link Instant} corresponding to the initial simulation time.
	 * 
	 * @see #setSimTimeStartInstant(Instant)
	 * @see #getInitialSimTime()
	 */
	public Instant getSimTimeStartInstant() {
		return simTimeStartInstant;
	}

	/**
	 * Sets the {@link Instant} corresponding to the a simulation time of 0. The
	 * default setting is to use the beginning of the current year.
	 * <p>
	 * The Instant will be truncated to milliseconds, so any nano-second part will
	 * be cleared.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	public void setSimTimeStartInstant(Instant simTimeStartInstant) {
		long epochMillis = simTimeStartInstant.toEpochMilli();
		this.simTimeStartInstant = Instant.ofEpochMilli(epochMillis);
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
	 * Returns the currently set {@code Locale}, i.e., language and region.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the current {@code Locale}.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public ZoneId getZoneId() {
		return zoneId;
	}

	/**
	 * Sets the current time zone (used by {@link #simTimeToLocalDateTime()}.
	 */
	public void setZoneId(ZoneId zone) {
		this.zoneId = zone;
	}

	public String message(Enum<?> key) {
		return I18n.message(getLocale(), key);
	}

	public String message(String keyName) {
		return I18n.message(getLocale(), keyName);
	}

	public String formattedMessage(Enum<?> key, Object... params) {
		return I18n.formattedMessage(getLocale(), key, params);
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
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

	/**
	 * Specifies the time unit of the (double-valued) simulation time. The default
	 * value is ChronoUnit.MINUTES.
	 *
	 * @see #simTimeToInstant(double)
	 * @see #setSimTimeToMillisFactor(long)
	 */
	public void setSimTimeToMillisFactor(TemporalUnit u) {
		setSimTimeToMillisFactor(Duration.of(1, u).toMillis());
	}

	public boolean continueSim() {
		return numAppEvents > 0 && !endRequested;
	}

	public void runInSimThread(Runnable r) {
		runInSimThread(simActionFromRunnable(r));
	}

	public void runInSimThread(SimAction a) {
		runInSimThread.add(a);
	}

	SimProcess<?> mainProcess() {
		return mainProcess;
	}

	void setCurrentProcess(SimProcess<?> p) {
		this.currentProcess = p;
	}

	void terminateWithException(Exception e) {
		execFailure = requireNonNull(e);
		endRequested = true;
	}

	SimProcess<?> getEventLoopProcess() {
		return eventLoopProcess;
	}

	void setEventLoopProcess(SimProcess<?> eventLoopProcess) {
		this.eventLoopProcess = eventLoopProcess;
	}

	// ValueStore implementation
	@Override
	public ValueStore valueStoreImpl() {
		return valueStore;
	}

	// SimOperation implementation
	@Override
	public Simulation getSim() {
		return this;
	}

	void processTerminated(SimProcess<?> simProcess) {
		synchronized (runnableProcesses) {
			boolean removeRes = runnableProcesses.remove(simProcess);
			assert removeRes;
		}
	}

	void processNew(SimProcess<?> simProcess) {
		synchronized (runnableProcesses) {
			runnableProcesses.add(simProcess);
		}
	}

	public int numRunnableProcesses() {
		synchronized (runnableProcesses) {
			return runnableProcesses.size();
		}
	}

	public List<SimProcess<?>> runnableProcesses() {
		synchronized (runnableProcesses) {
			return unmodifiableList(new ArrayList<>(runnableProcesses));
		}
	}

	public SimAction getMainProcessActions() {
		return mainProcessActions;
	}

	public void setMainProcessActions(SimRunnable r) {
		setMainProcessActions(SimProcessUtil.simAction(r));
	}

	public void setMainProcessActions(SimAction mainProcessActions) {
		requireAllowedState(state.get(), INITIAL, INIT);
		this.mainProcessActions = mainProcessActions;
	}

}
