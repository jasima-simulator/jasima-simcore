package jasima.core.simulation.util;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import javax.annotation.Nullable;

import jasima.core.random.RandomFactory;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentContainer;
import jasima.core.simulation.SimEntity;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;

/**
 * Common location of simulation operations. Operations are either defined here
 * or are calling the corresponding method in {@link Simulation}. This allows to
 * access frequently used simulation methods in several places (e.g.,
 * {@link SimComponent} without having to duplicate code, redirecting to
 * simulation. Furthermore some code can be moved out of {@link Simulation} to
 * reduce its size.
 */
public interface SimOperations {

	Simulation getSim();

	default SimComponentContainer getRootComponent() {
		return getSim().getRootComponent();
	}

	/**
	 * Convenience method to add a one or more new component(s) to the root
	 * component of this simulation.
	 */
	default void addComponent(SimComponent... scs) {
		getRootComponent().addChild(scs);
	}

	/**
	 * @see Simulation#activateEntity(SimEntity)
	 */
	default <T extends SimEntity> T activateEntity(T e) {
		getSim().activateEntity(e);
		return e;
	}

	/**
	 * After calling end() the simulation is terminated (after handling the current
	 * event). This method might also be called from an external thread.
	 */
	default void end() {
		getSim().end();
	}

	/**
	 * Returns true, if {@link #end()} was called and the simulation run ends after
	 * processing the current event.
	 */
	default boolean isEndRequested() {
		return getSim().isEndRequested();
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
	 * Returns the current event's priority.
	 * 
	 * @see Simulation#currentPrio()
	 */
	default int currentPrio() {
		return getSim().currentPrio();
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

	//
	// scheduleAt(...) in different flavors
	//

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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(double time, int prio, Runnable method) {
		return scheduleAt(null, time, prio, method);
	}

	/**
	 * @see #scheduleAt(double, int, Runnable)
	 */
	default SimEvent scheduleAt(double time, Runnable method) {
		return scheduleAt(time, currentPrio(), method);
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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(@Nullable String description, double time, int prio, Runnable action) {
		return scheduleAt(description, time, prio, action, true);
	}

	/**
	 * @see #scheduleAt(String, double, int, Runnable)
	 */
	default SimEvent scheduleAt(@Nullable String description, double time, Runnable action) {
		return scheduleAt(description, time, currentPrio(), action);
	}

	/**
	 * @see #scheduleAt(String, double, int, Runnable, boolean)
	 */
	default SimEvent scheduleAt(double time, int prio, Runnable method, boolean isAppEvent) {
		return scheduleAt(null, time, prio, method, isAppEvent);
	}

	/**
	 * @see #scheduleAt(double, int, Runnable, boolean)
	 */
	default SimEvent scheduleAt(double time, Runnable method, boolean isAppEvent) {
		return scheduleAt(time, currentPrio(), method, isAppEvent);
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
	 * @param isAppEvent  If true (default), an event is an app event, otherwise a
	 *                    utility event. The simulation continues while there are
	 *                    app events in the event queue, utility events are ignored
	 *                    in this respect.
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(@Nullable String description, double time, int prio, Runnable action,
			boolean isAppEvent) {
		SimEvent e = new SimEventMethodCall(time, prio, description, action, isAppEvent);
		return schedule(e);
	}

	/**
	 * @see #scheduleAt(String, double, int, Runnable, boolean)
	 */
	default SimEvent scheduleAt(@Nullable String description, double time, Runnable action, boolean isAppEvent) {
		return scheduleAt(description, time, currentPrio(), action, isAppEvent);
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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(Instant time, int prio, Runnable method) {
		return scheduleAt(null, time, prio, method);
	}

	/**
	 * @see #scheduleAt(Instant, int, Runnable)
	 */
	default SimEvent scheduleAt(Instant time, Runnable method) {
		return scheduleAt(time, currentPrio(), method);
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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(@Nullable String description, Instant time, int prio, Runnable method) {
		return scheduleAt(description, toSimTime(time), prio, method);
	}

	/**
	 * @see #scheduleAt(String, Instant, int, Runnable)
	 */
	default SimEvent scheduleAt(@Nullable String description, Instant time, Runnable method) {
		return scheduleAt(description, time, currentPrio(), method);
	}

	/**
	 * Schedules a call to {@code method} at a certain amount of time specified by
	 * {@code numUnits} and {@code unit}.
	 * <p>
	 * Usually using {@link #scheduleAt(String, long, TemporalUnit, int, Runnable)}
	 * should be preferred.
	 * 
	 * @param numUnits The time duration.
	 * @param unit     The time unit of {@code numUnits}.
	 * @param prio     Priority of the event (to deterministically sequence events
	 *                 at the same time).
	 * @param method   The method to call at the given moment.
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(long numUnits, TemporalUnit unit, int prio, Runnable method) {
		return scheduleAt(null, numUnits, unit, prio, method);
	}

	/**
	 * @see #scheduleAt(long, TemporalUnit, int, Runnable)
	 */
	default SimEvent scheduleAt(long numUnits, TemporalUnit unit, Runnable method) {
		return scheduleAt(numUnits, unit, currentPrio(), method);
	}

	/**
	 * Schedules a call to {@code method} at a certain amount of time specified by
	 * {@code numUnits} and {@code unit}.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param numUnits    The time.
	 * @param unit        The time unit of {@code numUnits}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param method      The method to call at the given moment.
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleAt(@Nullable String description, long numUnits, TemporalUnit unit, int prio,
			Runnable method) {
		return scheduleAt(description, toSimTime(numUnits, unit), prio, method);
	}

	/**
	 * @see #scheduleIn(String, long, TemporalUnit, int, Runnable)
	 */
	default SimEvent scheduleAt(@Nullable String description, long numUnits, TemporalUnit unit, Runnable method) {
		return scheduleAt(description, numUnits, unit, currentPrio(), method);
	}

	//
	// scheduleIn(...) in different flavors
	//

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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(double time, int prio, Runnable method) {
		return scheduleIn(null, time, prio, method);
	}

	/**
	 * @see #scheduleIn(double, int, Runnable)
	 */
	default SimEvent scheduleIn(double time, Runnable method) {
		return scheduleIn(time, currentPrio(), method);
	}

	/**
	 * @see #scheduleIn(String, double, int, Runnable, boolean)
	 */
	default SimEvent scheduleIn(double time, int prio, Runnable method, boolean isAppEvent) {
		return scheduleIn(null, time, prio, method, isAppEvent);
	}

	/**
	 * @see #scheduleIn(double, int, Runnable, boolean)
	 */
	default SimEvent scheduleIn(double time, Runnable method, boolean isAppEvent) {
		return scheduleIn(time, currentPrio(), method, isAppEvent);
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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(@Nullable String description, double time, int prio, Runnable method) {
		return scheduleIn(description, time, prio, method, true);
	}

	/**
	 * @see #scheduleIn(String, double, int, Runnable)
	 */
	default SimEvent scheduleIn(@Nullable String description, double time, Runnable method) {
		return scheduleIn(description, time, currentPrio(), method);
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
	 * @param isAppEvent  If true (default), an event is an app event, otherwise a
	 *                    utility event. The simulation continues while there are
	 *                    app events in the event queue, utility events are ignored
	 *                    in this respect.
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(@Nullable String description, double time, int prio, Runnable method,
			boolean isAppEvent) {
		return scheduleAt(description, simTime() + time, prio, method, isAppEvent);
	}

	/**
	 * @see #scheduleIn(String, double, int, Runnable, boolean)
	 */
	default SimEvent scheduleIn(@Nullable String description, double time, Runnable method, boolean isAppEvent) {
		return scheduleIn(description, time, currentPrio(), method, isAppEvent);
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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(Duration duration, int prio, Runnable method) {
		return scheduleIn(null, duration, prio, method);
	}

	/**
	 * @see #scheduleIn(Duration, int, Runnable)
	 */
	default SimEvent scheduleIn(Duration duration, Runnable method) {
		return scheduleIn(duration, currentPrio(), method);
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
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(@Nullable String description, Duration duration, int prio, Runnable method) {
		return scheduleIn(description, toSimTime(duration), prio, method);
	}

	/**
	 * @see #scheduleIn(String, Duration, int, Runnable)
	 */
	default SimEvent scheduleIn(@Nullable String description, Duration duration, Runnable method) {
		return scheduleIn(description, duration, currentPrio(), method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time specified by
	 * {@code numUnits} and {@code unit}.
	 * <p>
	 * Usually using {@link #scheduleIn(String, long, TemporalUnit, int, Runnable)}
	 * should be preferred.
	 * 
	 * @param numUnits The time duration.
	 * @param unit     The time unit of {@code numUnits}.
	 * @param prio     Priority of the event (to deterministically sequence events
	 *                 at the same time).
	 * @param method   The method to call at the given moment.
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(long numUnits, TemporalUnit unit, int prio, Runnable method) {
		return scheduleIn(null, numUnits, unit, prio, method);
	}

	/**
	 * @see #scheduleIn(long, TemporalUnit, int, Runnable)
	 */
	default SimEvent scheduleIn(long numUnits, TemporalUnit unit, Runnable method) {
		return scheduleIn(numUnits, unit, currentPrio(), method);
	}

	/**
	 * Schedules a call to {@code method} in a certain amount of time specified by
	 * {@code numUnits} and {@code unit}.
	 * 
	 * @param description Some description that is added as an additional parameter
	 *                    to the Event object (makes debugging easier).
	 * @param numUnits    The time duration.
	 * @param unit        The time unit of {@code numUnits}.
	 * @param prio        Priority of the event (to deterministically sequence
	 *                    events at the same time).
	 * @param method      The method to call at the given moment.
	 * @return The {@link SimEvent} that was added to the event queue (to allow
	 *         usage in, e.g., {@link Simulation#unschedule(SimEvent)}).
	 */
	default SimEvent scheduleIn(@Nullable String description, long numUnits, TemporalUnit unit, int prio,
			Runnable method) {
		return scheduleIn(description, toSimTime(numUnits, unit), prio, method);
	}

	/**
	 * @see #scheduleIn(String, long, TemporalUnit, int, Runnable)
	 */
	default SimEvent scheduleIn(@Nullable String description, long numUnits, TemporalUnit unit, Runnable method) {
		return scheduleIn(description, numUnits, unit, currentPrio(), method);
	}

	//
	// schedule periodically
	//

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

	//
	// time conversion methods
	//

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

	/**
	 * @see Simulation#addResult(String, Object)
	 */
	default void addResult(String name, Object value) {
		getSim().addResult(name, value);
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
	 * Initializes the random number generator associated with the
	 * {@link DblSequence} {@code s}. This just delegates to the
	 * {@link RandomFactory} of a simulation.
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
