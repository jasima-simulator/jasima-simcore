package jasima.core.simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntStream;
import jasima.core.util.SilentCloneable;
import jasima.core.util.ValueStore;
import jasima.core.util.observer.Notifier;

/**
 * This interface provides basic functionality for simulation components.
 * 
 * @author Torsten Hildebrandt
 * @see Simulation
 */
public interface SimComponent extends Notifier<SimComponent, Object>, ValueStore, SilentCloneable<SimComponent> {

	/**
	 * Base class for messages send by a {@link SimComponent} to registered
	 * listeners.
	 * 
	 * @author Torsten Hildebrandt
	 * @see SimComponent#addListener(jasima.core.util.observer.NotifierListener)
	 */
	public static class SimComponentLifeCycleMessage {

		private final String name;

		public SimComponentLifeCycleMessage(String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

		public static final SimComponentLifeCycleMessage INIT = new SimComponentLifeCycleMessage("INIT");
		public static final SimComponentLifeCycleMessage BEFORE_RUN = new SimComponentLifeCycleMessage("BEFORE_RUN");
		public static final SimComponentLifeCycleMessage RESET_STATS = new SimComponentLifeCycleMessage("RESET_STATS");
		public static final SimComponentLifeCycleMessage AFTER_RUN = new SimComponentLifeCycleMessage("AFTER_RUN");
		public static final SimComponentLifeCycleMessage DONE = new SimComponentLifeCycleMessage("DONE");
	}

	/**
	 * Message send when a {@link SimComponent} is requested to produce results.
	 * 
	 * @author Torsten Hildebrandt
	 */
	public static class ProduceResultsMessage extends SimComponentLifeCycleMessage {

		public final Map<String, Object> resultMap;

		public ProduceResultsMessage(Map<String, Object> resultMap) {
			super("ProduceResultsEvent");
			this.resultMap = resultMap;
		}

	}

	/**
	 * Returns the simulation this component is associated with.
	 */
	Simulation getSim();

	/**
	 * Sets the simulation this component is part of.
	 */
	void setSim(Simulation sim);

	/**
	 * Returns the current simulation time.
	 * 
	 * @see Simulation#simTime()
	 */
	default double simTime() {
		return getSim().simTime();
	}

	/**
	 * Returns the current simulation time as an Instant.
	 * 
	 * @see Simulation#simTimeToInstant()
	 */
	default Instant simTimeToInstant() {
		return getSim().simTimeToInstant();
	}

	/**
	 * Returns the given simulation time as an Instant.
	 * 
	 * @see Simulation#simTimeToInstant(double)
	 */
	default Instant simTimeToInstant(double time) {
		return getSim().simTimeToInstant(time);
	}

	/**
	 * Returns the container this component is contained in.
	 */
	SimComponentContainer<?> getParent();

	/**
	 * Sets the container this component is contained in.
	 */
	void setParent(SimComponentContainer<?> p);

	/**
	 * Gets the name of this component (must not be changed once set).
	 */
	String getName();

	void setName(String name);

	default boolean isValidName(String name) {
		return name != null && name.length() > 0 && name.indexOf('.') < 0;
	}

	// default implementations of lifecycle messages/events

	default void init() {
		fire(SimComponentLifeCycleMessage.INIT);
	}

	default void beforeRun() {
		fire(SimComponentLifeCycleMessage.BEFORE_RUN);
	}

	default void resetStats() {
		fire(SimComponentLifeCycleMessage.RESET_STATS);
	}

	default void afterRun() {
		fire(SimComponentLifeCycleMessage.AFTER_RUN);
	}

	default void done() {
		fire(SimComponentLifeCycleMessage.DONE);
	}

	default void produceResults(Map<String, Object> res) {
		fire(new ProduceResultsMessage(res));
	}

	// schedule simulation events, delegated to the simulation

	/**
	 * Schedules a new event.
	 * 
	 * @see Simulation#schedule(SimEvent)
	 */
	default void schedule(SimEvent event) {
		getSim().schedule(event);
	}

	/**
	 * Schedules a call to {@code method} at certain point in time.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time.
	 * @param method The method to call at the given moment.
	 * 
	 * @see Simulation#schedule(double, int, Runnable)
	 */
	default void schedule(double time, int prio, Runnable method) {
		getSim().schedule(time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} at certain point in time.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param time        The time when to call {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time.
	 * @param method      The method to call at the given moment.
	 * 
	 * @see Simulation#schedule(double, int, Runnable)
	 */
	default void schedule(String description, double time, int prio, Runnable method) {
		getSim().schedule(description, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #schedule(double, int, Runnable)} this method expects a relative
	 * time instead of an absolute one.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time).
	 * @param method The method to call at the given moment.
	 */
	default void scheduleIn(double time, int prio, Runnable method) {
		getSim().scheduleIn(time, prio, method);
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
	default void scheduleIn(String description, double time, int prio, Runnable method) {
		getSim().scheduleIn(description, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #schedule(double, int, Runnable)} this method expects a relative
	 * time specified by a {@link Duration} instead of an absolute one.
	 * <p>
	 * Usually using {@link #scheduleIn(String, Duration, int, Runnable)} should be
	 * preferred.
	 * 
	 * @param duration The duration from the current simulation time when to call
	 *                 {@code method}.
	 * @param prio     Priority of the event (to deterministically sequence events
	 *                 at the same time).
	 * @param method   The method to call at the given moment.
	 */
	default void scheduleIn(Duration duration, int prio, Runnable method) {
		getSim().scheduleIn(duration,  prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #schedule(double, int, Runnable)} this method expects a relative
	 * time specified by a {@link Duration} instead of an absolute one.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param duration    The duration from the current simulation time when to call
	 *                    {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param method      The method to call at the given moment.
	 */
	default void scheduleIn(String description, Duration duration, int prio, Runnable method) {
		getSim().scheduleIn(description, duration,  prio, method);
	}

	/**
	 * Schedules a call to {@code method} at certain point in time.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time.
	 * @param method The method to call at the given moment.
	 * 
	 * @see Simulation#schedule(Instant, int, Runnable)
	 */
	default void schedule(Instant time, int prio, Runnable method) {
		getSim().schedule(time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} at certain point in time.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param time        The time when to call {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time.
	 * @param method      The method to call at the given moment.
	 * 
	 * @see Simulation#schedule(Instant, int, Runnable)
	 */
	default void schedule(String description, Instant time, int prio, Runnable method) {
		getSim().schedule(description, time, prio, method);
	}

	/**
	 * Periodically calls a certain method. While this method returns true, a next
	 * invocation after the given time interval is scheduled.
	 * 
	 * @see Simulation#schedulePeriodically(double, double, int, BooleanSupplier)
	 */
	default void schedulePeriodically(double firstInvocation, double interval, int prio, BooleanSupplier method) {
		getSim().schedulePeriodically(firstInvocation, interval, prio, method);
	}

	/**
	 * Periodically calls a certain method until the simulation terminates.
	 * 
	 * @see Simulation#schedulePeriodically(double, double, int, Runnable)
	 */
	default void schedulePeriodically(double firstInvocation, double interval, int prio, Runnable method) {
		getSim().schedulePeriodically(firstInvocation, interval, prio, method);
	}

	/**
	 * Calls a certain method at the times returned by the method itself. The first
	 * invocation is performed at the current time (asynchronously, i.e.,
	 * {@code scheduleProcess()} returns before {@code method} is called for the
	 * first time). Subsequent calls are scheduled at the absolute times returned by
	 * the previous method invocation. No more invocations are scheduled if
	 * {@code method} returned NaN or a negative value.
	 * 
	 * @see Simulation#scheduleProcess(int, DoubleSupplier)
	 */
	default void scheduleProcess(int prio, DoubleSupplier method) {
		getSim().scheduleProcess(prio, method);
	}

	// event tracing

	/**
	 * Produces a trace message.
	 * 
	 * @see Simulation#trace(Object...)
	 */
	default void trace(Object... params) {
		getSim().trace(params);
	}

	/**
	 * Returns true is trace messages should be produced.
	 * 
	 * @see Simulation#isTraceEnabled()
	 */
	default boolean isTraceEnabled() {
		return getSim().isTraceEnabled();
	}

	/**
	 * Initializes the random number generator associated with the {@link DblStream}
	 * {@code s}. This just delegates to the {@link RandomFactory} of a simulation.
	 *
	 * @see Simulation#initRndGen(DblStream, String)
	 */
	default DblStream initRndGen(DblStream s, String streamName) {
		return getSim().initRndGen(s, streamName);
	}

	/**
	 * Initializes the random number generator associated with the {@link IntStream}
	 * {@code s}. This just delegates to the {@link RandomFactory} of a simulation.
	 *
	 * @see Simulation#initRndGen(IntStream, String)
	 */
	default IntStream initRndGen(IntStream s, String streamName) {
		return getSim().initRndGen(s, streamName);
	}

	/**
	 * Creates an instance of Java's {@code Random} class initialized with a seed
	 * derived from the parameter {@code streamName}. This just delegates to the
	 * method {@link RandomFactory#createInstance(String)} of a simulation.
	 *
	 * @see Simulation#initRndGen(String)
	 */
	default Random initRndGen(String streamName) {
		return getSim().initRndGen(streamName);
	}

	/**
	 * Returns a base name for a SimConponent consisting of the hierarchical
	 * representation of the parent ({@link #getParent()}) if it exists and the
	 * (simple) name of the component's class.
	 */
	String getHierarchicalName();

	// event notification, delegate to adapter

	/**
	 * {@code SimComponent}s can notify registered listeners of certain
	 * events/messages occurring. The default implementation of {@link SimComponent}
	 * informs listeners of lifecycle events such as INIT, DONE, etc.
	 */
	@Override
	Notifier<SimComponent, Object> notifierImpl();

	// ValueStore, delegate implementation

	/**
	 * {@code SimComponent}s provide a {@link ValueStore} to attach arbitrary
	 * key/value-pairs with them. This can be used as a simple extension mechanism
	 * without having to use inheritance.
	 */
	@Override
	ValueStore valueStoreImpl();

	// cloning

	/**
	 * Public clone method. Implementing classes should implement a suitable
	 * functionality or throw a {@link CloneNotSupportedException}.
	 */
	@Override
	SimComponent clone() throws CloneNotSupportedException;

}
