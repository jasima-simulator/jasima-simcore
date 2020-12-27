package jasima.core.simulation.util;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentContainer;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;

/**
 * Common location of simulation operations. Operations are either defined here
 * or are calling the corresponding method in {@link Simulation}. This allows to
 * access frequently used simulation methods in several places (e.g.,
 * {@link SimComponent} without having to duplicate code, redirecting to
 * simulation.
 */
public interface SimOperations {

	Simulation getSim();

	default SimComponentContainer getRootComponent() {
		return getSim().getRootComponent();
	}

	/**
	 * Returns the current simulation time.
	 * 
	 * @see Simulation#simTime()
	 */
	default double simTime() {
		return getSim().simTime();
	}

	/**
	 * Converts the current simulation time to a Java {@link Instant}.
	 * 
	 * @see #simTimeToInstant(double)
	 */
	default Instant simTimeAbs() {
		return simTimeToInstant(simTime());
	}

	/**
	 * Returns the given simulation time as an Instant.
	 * 
	 * @see Simulation#simTimeToInstant(double)
	 */
	default Instant simTimeToInstant(double time) {
		return getSim().simTimeToInstant(time);
	}

	// schedule simulation events, delegated to the simulation

	/**
	 * Schedules a new event.
	 * 
	 * @see Simulation#schedule(SimEvent)
	 */
	default SimEvent schedule(SimEvent event) {
		return getSim().schedule(event);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time. Instead of
	 * calling this method it is usually better to use
	 * {@link #scheduleAt(String, double, int, Runnable)} instead, as the additional
	 * description parameter usually makes debugging easier.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time).
	 * @param method The method to call at the given moment.
	 */
	default void scheduleAt(double time, int prio, Runnable method) {
		scheduleAt(null, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param time        The time when to call {@code method}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param action      The method to call at the given moment.
	 */
	default void scheduleAt(String description, double time, int prio, Runnable action) {
		SimEvent e = new SimEventMethodCall(time, prio, description, action);
		schedule(e);
	}

	/**
	 * Schedules a call to {@code method} at a certain point in time given as a Java
	 * Instant. Usually using {@link #scheduleAt(String, Instant, int, Runnable)}
	 * should be preferred.
	 * 
	 * @param time   The time when to call {@code method}.
	 * @param prio   Priority of the event (to deterministically sequence events at
	 *               the same time).
	 * @param method The method to call at the given moment.
	 */
	default void scheduleAt(Instant time, int prio, Runnable method) {
		scheduleAt(null, time, prio, method);
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
	default void scheduleAt(String description, Instant time, int prio, Runnable method) {
		scheduleAt(description, toSimTime(time), prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #scheduleAt(double, int, Runnable)} this method expects a relative
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
	default void scheduleIn(double time, int prio, Runnable method) {
		scheduleIn(null, time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #scheduleAt(double, int, Runnable)} this method expects a relative
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
		scheduleAt(description, simTime() + time, prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #scheduleAt(double, int, Runnable)} this method expects a relative
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
		scheduleIn(null, duration, prio, method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time. In contrast
	 * to {@link #scheduleAt(double, int, Runnable)} this method expects a relative
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
		scheduleAt(description, simTime() + toSimTime(duration), prio, method);
	}

	default void scheduleIn(long numUnits, TemporalUnit unit, int prio, Runnable method) {
		scheduleIn(null, toSimTime(numUnits, unit), prio, method);
	}

	default void scheduleIn(String description, long numUnits, TemporalUnit unit, int prio, Runnable method) {
		scheduleAt(description, simTime() + toSimTime(numUnits, unit), prio, method);
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

	/**
	 * @see Simulation#scheduleProcess(double, int, DoubleSupplier)
	 */
	default void scheduleProcess(double firstInvocation, int prio, DoubleSupplier method) {
		getSim().scheduleProcess(firstInvocation, prio, method);
	}

	// time convertion methods

	/**
	 * @see Simulation#toSimTime(Instant)
	 */
	default double toSimTime(Instant instant) {
		return getSim().toSimTime(instant);
	}

	/**
	 * @see Simulation#toSimTime(Duration)
	 */
	default double toSimTime(Duration d) {
		return getSim().toSimTime(d);
	}

	/**
	 * @see Simulation#toSimTime(long, TemporalUnit)
	 */
	default double toSimTime(long numUnits, TemporalUnit u) {
		return getSim().toSimTime(numUnits, u);
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
	 * Initializes the random number generator associated with the {@link DblSequence}
	 * {@code s}. This just delegates to the {@link RandomFactory} of a simulation.
	 *
	 * @see Simulation#initRndGen(DblSequence, String)
	 */
	default <T extends DblSequence> T initRndGen(T s, String streamName) {
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

}
