package jasima.core.simulation;

import static jasima.core.simulation.SimContext.I18nConsts.NESTED_FAILED;
import static jasima.core.simulation.SimContext.I18nConsts.NO_CONTEXT;
import static jasima.core.util.SimProcessUtil.simAction;
import static jasima.core.util.StandardExtensionImpl.JASIMA_CORE_RES_BUNDLE;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation.SimulationFailed;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimCallable;
import jasima.core.util.SimProcessUtil.SimRunnable;
import jasima.core.util.i18n.I18n;
import jasima.core.util.observer.ObservableValue;

/**
 * {@code SimContext} provides static versions of the most frequently used
 * simulation methods as well as static factory methods to create new
 * simulations. By statically importing the methods from {@code SimContext}, the
 * most important simulation methods can be accessed in a more concise way or in
 * contexts, where the current simulation can not be accessed easily.
 * <p>
 * The currently active simulation is stored internally in a {@code ThreadLocal}
 * variable. Most methods in this class call {@link #requireSimContext()} and
 * then delegate to the corresponding method in {@link Simulation} or the
 * simulation's currently active {@link SimProcess}. Exceptions to this are the
 * factory methods {@link #simulationOf(String, SimAction)}/
 * {@link #async(String, SimAction)}/ {@link #createSim(String, SimAction)}.
 * 
 * @author Torsten.Hildebrandt
 */
public class SimContext {

	private static ThreadLocal<Simulation> currentSim = new ThreadLocal<>();

	/**
	 * Returns the current {@link Simulation} or {@code null} if no simulation is
	 * currently assigned to the current thread.
	 */
	public static @Nullable Simulation currentSimulation() {
		return currentSim == null ? null : currentSim.get();
	}

	/**
	 * Returns the current {@link Simulation}. If there is no simulation, an
	 * {@link IllegalStateException} is thrown.
	 */
	public static Simulation requireSimContext() {
		Simulation s = currentSimulation();
		if (s == null) {
			throw new IllegalStateException(message(NO_CONTEXT));
		} else {
			return s;
		}
	}

	/**
	 * {@link Simulation#currentProcess()}
	 */
	public static SimProcess<?> currentProcess() {
		return requireSimContext().currentProcess();
	}

	/**
	 * {@link Simulation#simTime()}
	 */
	public static double simTime() {
		return requireSimContext().simTime();
	}

	/**
	 * {@link Simulation#toSimTime(long, TemporalUnit)}
	 */
	public static double toSimTime(long amount, TemporalUnit u) {
		return requireSimContext().toSimTime(amount, u);
	}

	/**
	 * {@link Simulation#toSimTime(Duration)}
	 */
	public static double toSimTime(Duration d) {
		return requireSimContext().toSimTime(d);
	}

	/**
	 * {@link Simulation#message(Enum)}
	 */
	public static String message(Enum<?> key) {
		return requireSimContext().message(key);
	}

	/**
	 * {@link Simulation#message(String)}
	 */
	public static String message(String keyName) {
		return requireSimContext().message(keyName);
	}

	/**
	 * {@link Simulation#formattedMessage(Enum, Object)}
	 */
	public static String formattedMessage(Enum<?> key, Object... params) {
		return requireSimContext().formattedMessage(key, params);
	}

	/**
	 * Activates the given {@link SimEntity} and returns its lifecycle process
	 * {@link Simulation#activateEntity(SimEntity)}.
	 */
	public static SimProcess<Void> activateEntity(SimEntity e) {
		return requireSimContext().activateEntity(e).getLifecycleProcess();
	}

	/**
	 * {@link Simulation#activate(SimRunnable)}
	 */
	public static SimProcess<Void> activate(SimRunnable r) {
		return requireSimContext().activate(r);
	}

	/**
	 * {@link Simulation#activate(String,SimRunnable)}
	 */
	public static SimProcess<Void> activate(String name, SimRunnable r) {
		return requireSimContext().activate(name, r);
	}

	/**
	 * {@link Simulation#activate(SimAction)}
	 */
	public static SimProcess<Void> activate(SimAction a) {
		return requireSimContext().activate(a);
	}

	/**
	 * {@link Simulation#activate(String, SimAction)}
	 */
	public static SimProcess<Void> activate(String name, SimAction a) {
		return requireSimContext().activate(name, a);
	}

	/**
	 * {@link Simulation#activateCallable(Callable)}
	 */
	public static <T> SimProcess<T> activateCallable(Callable<T> c) {
		return requireSimContext().activateCallable(c);
	}

	/**
	 * {@link Simulation#activateCallable(String, Callable)}
	 */
	public static <T> SimProcess<T> activateCallable(String name, Callable<T> c) {
		return requireSimContext().activateCallable(name, c);
	}

	/**
	 * {@link Simulation#activateCallable(SimCallable)}
	 */
	public static <T> SimProcess<T> activateCallable(SimCallable<T> a) {
		return requireSimContext().activateCallable(a);
	}

	/**
	 * {@link Simulation#activateCallable(String, SimCallable)}
	 */
	public static <T> SimProcess<T> activateCallable(String name, SimCallable<T> a) {
		return requireSimContext().activateCallable(name, a);
	}

	/**
	 * @see Simulation#trace(Object...)
	 */
	public static void trace(Object... params) {
		requireSimContext().trace(params);
	}

	/**
	 * @see Simulation#end()
	 */
	public static void end() {
		requireSimContext().end();
	}

	/**
	 * @see Simulation#addResult(String, Object)
	 */
	public static void addResult(String name, Object value) {
		requireSimContext().addResult(name, value);
	}

	// methods related to SimProcess, delegated to the simulation's currently active
	// process.

	/**
	 * {@link SimProcess#waitFor(double)}
	 */
	public static void waitFor(double deltaT) throws MightBlock {
		currentProcess().waitFor(deltaT);
	}

	/**
	 * {@link SimProcess#waitFor(long,TemporalUnit)}
	 */
	public static void waitFor(long amount, TemporalUnit u) throws MightBlock {
		currentProcess().waitFor(amount, u);
	}

	/**
	 * {@link SimProcess#waitFor(Duration)}
	 */
	public static void waitFor(Duration d) throws MightBlock {
		currentProcess().waitFor(d);
	}

	/**
	 * {@link SimProcess#waitUntil(double)}
	 */
	public static void waitUntil(double tAbs) throws MightBlock {
		currentProcess().waitUntil(tAbs);
	}

	/**
	 * {@link SimProcess#waitUntil(Instant)}
	 */
	public static void waitUntil(Instant instant) throws MightBlock {
		currentProcess().waitUntil(instant);
	}

	/**
	 * @see SimProcess#waitCondition(ObservableValue)
	 */
	public static boolean waitCondition(ObservableValue<Boolean> triggerCondition) throws MightBlock {
		return currentProcess().waitCondition(triggerCondition);
	}

	/**
	 * @see SimProcess#waitCondition(Function, ObservableValue)
	 */
	public static <T> boolean waitCondition(Function<T, Boolean> triggerCondition,
			ObservableValue<? extends T> observable) throws MightBlock {
		return currentProcess().waitCondition(triggerCondition, observable);
	}

	/**
	 * @see SimProcess#waitCondition(BiFunction, ObservableValue, ObservableValue)
	 */
	public static <T1, T2> boolean waitCondition(BiFunction<T1, T2, Boolean> triggerCondition,
			ObservableValue<? extends T1> obs1, ObservableValue<? extends T2> obs2) throws MightBlock {
		return currentProcess().waitCondition(triggerCondition, obs1, obs2);
	}

	/**
	 * @see SimProcess#suspend()
	 */
	public static void suspend() throws MightBlock {
		currentProcess().suspend();
	}

	// schedule simulation events, delegated to the simulation

	/** @see Simulation#scheduleAt(SimEvent) */
	public static SimEvent schedule(SimEvent event) {
		return requireSimContext().schedule(event);
	}

	/** @see Simulation#scheduleAt(double, int, Runnable) */
	public static SimEvent scheduleAt(double time, int prio, Runnable method) {
		return requireSimContext().scheduleAt(time, prio, method);
	}

	/** @see Simulation#scheduleAt(String, double, int, Runnable) */
	public static SimEvent scheduleAt(String description, double time, int prio, Runnable action) {
		return requireSimContext().scheduleAt(description, time, prio, action);
	}

	/** @see Simulation#scheduleAt(Instant, int, Runnable) */
	public static SimEvent scheduleAt(Instant time, int prio, Runnable method) {
		return requireSimContext().scheduleAt(time, prio, method);
	}

	/** @see Simulation#scheduleAt(String, Instant, int, Runnable) */
	public static SimEvent scheduleAt(String description, Instant time, int prio, Runnable method) {
		return requireSimContext().scheduleAt(description, time, prio, method);
	}

	/** @see Simulation#scheduleIn(double, int, Runnable) */
	public static SimEvent scheduleIn(double time, int prio, Runnable method) {
		return requireSimContext().scheduleIn(time, prio, method);
	}

	/** @see Simulation#scheduleIn(String, double, int, Runnable) */
	public static SimEvent scheduleIn(String description, double time, int prio, Runnable method) {
		return requireSimContext().scheduleIn(description, time, prio, method);
	}

	/** @see Simulation#scheduleIn(Duration, int, Runnable) */
	public static SimEvent scheduleIn(Duration duration, int prio, Runnable method) {
		return requireSimContext().scheduleIn(duration, prio, method);
	}

	/** @see Simulation#scheduleIn(String, Duration, int, Runnable) */
	public static SimEvent scheduleIn(String description, Duration duration, int prio, Runnable method) {
		return requireSimContext().scheduleIn(description, duration, prio, method);
	}

	/** @see Simulation#scheduleIn(long, TemporalUnit, int, Runnable) */
	public static SimEvent scheduleIn(long numUnits, TemporalUnit unit, int prio, Runnable method) {
		return requireSimContext().scheduleIn(numUnits, unit, prio, method);
	}

	/** @see Simulation#scheduleIn(String, long, TemporalUnit, int, Runnable) */
	public static SimEvent scheduleIn(String description, long numUnits, TemporalUnit unit, int prio, Runnable method) {
		return requireSimContext().scheduleIn(description, numUnits, unit, prio, method);
	}

	/** @see Simulation#initRndGen(DblSequence, String) */
	public static <T extends DblSequence> T initRndGen(T s, String streamName) {
		return requireSimContext().initRndGen(s, streamName);
	}

//	public static String formatMsg(Object... params) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(simTime());
//		for (Object o : params) {
//			sb.append('\t').append(String.valueOf(o));
//		}
//
//		return sb.toString();
//	}

	// factory methods to run a simulation from several main components and return
	// the results

	/** @see #simulationOf(String, SimAction) */
	public static Map<String, Object> simulationOf(SimRunnable r) {
		return simulationOf(null, simAction(r));
	}

	/** @see #simulationOf(String, SimAction) */
	public static Map<String, Object> simulationOf(@Nullable String name, SimRunnable r) {
		return simulationOf(name, simAction(r));
	}

	/** @see #simulationOf(String, SimAction) */
	public static Map<String, Object> simulationOf(SimComponent... components) {
		return simulationOf(null, components);
	}

	/** @see #simulationOf(String, SimAction) */
	public static Map<String, Object> simulationOf(@Nullable String name, SimComponent... components) {
		return simulationOf(name, sim -> sim.addComponent(components));
	}

	/** @see #simulationOf(String, SimAction) */
	public static Map<String, Object> simulationOf(SimAction a) {
		return simulationOf(null, a);
	}

	/**
	 * Create a new simulation and immediately executes it. This method only returns
	 * when the simulation is finished.
	 * 
	 * @param name The simulation's name (can be null)
	 * @param a    {@link SimAction} defining the simulation's behavior.
	 * @return The new simulation's result map.
	 * 
	 * @see Simulation#performRun()
	 * @see #async(String, SimAction)
	 * @see #createSim(String, SimAction)
	 */
	public static Map<String, Object> simulationOf(@Nullable String name, SimAction a) {
		Map<String, Object> res;
		if (currentSimulation() != null) {
			// execute in a new thread, current thread waits until finished
			Future<Map<String, Object>> resFuture = async(name, a);
			try {
				res = resFuture.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SimulationFailed(message(NESTED_FAILED), e);
			} catch (ExecutionException e) {
				Throwable c = e.getCause();
				if (c instanceof RuntimeException) {
					throw (RuntimeException) c;
				} else {
					throw new SimulationFailed(message(NESTED_FAILED), c);
				}
			}
		} else {
			// execute synchronously in current thread
			Simulation sim = createSim(name, a);
			res = sim.performRun();
		}
		return res;
	}

	/** @see #async(String, SimAction) */
	public static Future<Map<String, Object>> async(SimRunnable r) {
		return async(null, simAction(r));
	}

	/** @see #async(String, SimAction) */
	public static Future<Map<String, Object>> async(@Nullable String name, SimRunnable r) {
		return async(name, simAction(r));
	}

	/** @see #async(String, SimAction) */
	public static Future<Map<String, Object>> async(SimAction a) {
		return async(null, a);
	}

	/**
	 * Create a new simulation and run it asynchronously in a background thread.
	 * 
	 * @param name The simulation's name (can be null)
	 * @param a    {@link SimAction} defining the simulation's behavior.
	 * @return A {@link Future} to the simulation's result map.
	 * 
	 * @see Simulation#performRunAsync()
	 */
	public static Future<Map<String, Object>> async(@Nullable String name, SimAction a) {
		Simulation sim = createSim(name, a);
		return sim.performRunAsync();
	}

	/**
	 * Create a new simulation, but does not yet execute it.
	 * 
	 * @param name The simulation's name (can be null)
	 * @param a    {@link SimAction} defining the simulation's behavior.
	 * @return The new simulation.
	 */
	public static Simulation createSim(@Nullable String name, SimAction a) {
		Simulation sim = new Simulation();
		sim.setName(name);
		sim.setMainProcessActions(a);
		return sim;
	}

	/** Internal method to set the current Thread's simulation. */
	static void setThreadContext(Simulation sim) {
		if (sim != null && currentSim.get() != null && sim != currentSim.get()) {
			throw new IllegalStateException(); // old context not properly cleared?
		}
		currentSim.set(sim);
	}

	static enum I18nConsts {
		NO_CONTEXT, NESTED_FAILED;
	}

	static {
		I18n.requireResourceBundle(JASIMA_CORE_RES_BUNDLE, I18nConsts.class);
	}

}
