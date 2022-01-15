package jasima.core.simulation;

import static jasima.core.simulation.SimContext.I18nConsts.NESTED_FAILED;
import static jasima.core.simulation.SimContext.I18nConsts.NO_CONTEXT;
import static jasima.core.util.SimProcessUtil.simAction;
import static jasima.core.util.StandardExtensionImpl.JASIMA_CORE_RES_BUNDLE;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation.SimulationFailed;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimCallable;
import jasima.core.util.SimProcessUtil.SimRunnable;
import jasima.core.util.i18n.I18n;
import jasima.core.util.observer.ObservableValue;
import jasima.core.util.observer.ObservableValues;

public class SimContext {

	private static ThreadLocal<Simulation> currentSim = new ThreadLocal<>();

	public static Simulation currentSimulation() {
		return currentSim == null ? null : currentSim.get();
	}

	public static Simulation requireSimContext() {
		Simulation s = currentSimulation();
		if (s == null) {
			throw new IllegalStateException(message(NO_CONTEXT));
		} else {
			return s;
		}
	}

	public static SimProcess<?> currentProcess() {
		return requireSimContext().currentProcess();
	}

	public static double simTime() {
		return requireSimContext().simTime();
	}

	public static double toSimTime(long amount, TemporalUnit u) {
		return requireSimContext().toSimTime(amount, u);
	}

	public static double toSimTime(Duration d) {
		return requireSimContext().toSimTime(d);
	}

	public static String message(Enum<?> key) {
		return I18n.message(locale(), key);
	}

	public static String message(String keyName) {
		return I18n.message(locale(), keyName);
	}

	public static String formattedMessage(Enum<?> key, Object... params) {
		return I18n.formattedMessage(locale(), key, params);
	}

	public static SimProcess<Void> activateEntity(SimEntity e) {
		return requireSimContext().activateEntity(e).getLifecycleProcess();
	}

	public static SimProcess<Void> activate(SimRunnable r) {
		return requireSimContext().activate(r);
	}

	public static SimProcess<Void> activate(String name, SimRunnable r) {
		return requireSimContext().activate(name, r);
	}

	public static SimProcess<Void> activate(SimAction a) {
		return requireSimContext().activate(a);
	}

	public static SimProcess<Void> activate(String name, SimAction a) {
		return requireSimContext().activate(name, a);
	}

	public static <T> SimProcess<T> activateCallable(Callable<T> c) {
		return requireSimContext().activateCallable(c);
	}

	public static <T> SimProcess<T> activateCallable(String name, Callable<T> c) {
		return requireSimContext().activateCallable(name, c);
	}

	public static <T> SimProcess<T> activateCallable(SimCallable<T> a) {
		return requireSimContext().activateCallable(a);
	}

	public static <T> SimProcess<T> activateCallable(String name, SimCallable<T> a) {
		return requireSimContext().activateCallable(name, a);
	}

	public static void waitFor(double deltaT) throws MightBlock {
		currentProcess().waitFor(deltaT);
	}

	public static void waitFor(long amount, TemporalUnit u) throws MightBlock {
		currentProcess().waitFor(amount, u);
	}

	public static void waitFor(Duration d) throws MightBlock {
		currentProcess().waitFor(d);
	}

	public static void waitUntil(double tAbs) throws MightBlock {
		currentProcess().waitUntil(tAbs);
	}

	public static void waitUntil(Instant instant) throws MightBlock {
		currentProcess().waitUntil(instant);
	}

	/**
	 * Waits (possibly forever) until some condition, represented by an
	 * ObservableValue<Boolean>, evaluates to {@code true}. The condition is first
	 * checked immediately upon calling this method and might therefore return
	 * immediately.
	 * 
	 * @param triggerCondition The condition to wait for.
	 * @return {@code true} if the condition was initially true (so no wait
	 *         happened), {@code false} otherwise.
	 * @throws MightBlock To mark potentially blocking behavior.
	 */
	public static boolean waitCondition(ObservableValue<Boolean> triggerCondition) throws MightBlock {
		// complicated formulation of true check below to interpret NULL value as false
		if (!Boolean.TRUE.equals(triggerCondition.get())) {
			SimProcess<?> p = currentProcess();
			ObservableValues.whenTrueExecuteOnce(triggerCondition, p::resume);
			p.suspend(); // wait until condition is true
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Waits until some condition becomes true. The condition can be an arbitrary
	 * function returning a boolean value, taking the value of an observable as its
	 * parameter. The condition (usually a lambda expression) is evaluated each time
	 * the observable value changes.
	 * 
	 * @param triggerCondition A function/expression producing a boolean result.
	 * @param observable       The value used in {@code triggerCondition}.
	 * @return {@code true} if the condition was initially true (so no wait
	 *         happened), {@code false} otherwise.
	 * @throws MightBlock To mark potentially blocking behavior.
	 * 
	 * @see #waitCondition(ObservableValue)
	 * @see #waitCondition(BiFunction, ObservableValue, ObservableValue)
	 */
	public static <T> boolean waitCondition(Function<T, Boolean> triggerCondition,
			ObservableValue<? extends T> observable) throws MightBlock {
		ObservableValue<Boolean> c = ObservableValues.fromUnaryOperation(triggerCondition, observable);
		return waitCondition(c);
	}

	/**
	 * Same as {@link #waitCondition(Function, ObservableValue)}, but condition can
	 * depend of two values instead of just one.
	 * 
	 * @see #waitCondition(Function, ObservableValue)
	 * @see #waitCondition(ObservableValue)
	 */
	public static <T1, T2> boolean waitCondition(BiFunction<T1, T2, Boolean> triggerCondition,
			ObservableValue<? extends T1> obs1, ObservableValue<? extends T2> obs2) throws MightBlock {
		ObservableValue<Boolean> c = ObservableValues.fromBinaryOperation(triggerCondition, obs1, obs2);
		return waitCondition(c);
	}

	public static void suspend() throws MightBlock {
		currentProcess().suspend();
	}

	public static void trace(Object... params) {
		requireSimContext().trace(params);
//		out.error(() -> formatMsg(params));
	}

	public static void end() {
		requireSimContext().end();
	}

	public static void addResult(String name, Object value) {
		requireSimContext().addResult(name, value);
	}

	// schedule simulation events, delegated to the simulation

	public static SimEvent schedule(SimEvent event) {
		return requireSimContext().schedule(event);
	}

	public static void scheduleAt(double time, int prio, Runnable method) {
		requireSimContext().scheduleAt(time, prio, method);
	}

	public static void scheduleAt(String description, double time, int prio, Runnable action) {
		requireSimContext().scheduleAt(description, time, prio, action);
	}

	public static void scheduleAt(Instant time, int prio, Runnable method) {
		requireSimContext().scheduleAt(time, prio, method);
	}

	public static void scheduleAt(String description, Instant time, int prio, Runnable method) {
		requireSimContext().scheduleAt(description, time, prio, method);
	}

	public static void scheduleIn(double time, int prio, Runnable method) {
		requireSimContext().scheduleIn(time, prio, method);
	}

	public static void scheduleIn(String description, double time, int prio, Runnable method) {
		requireSimContext().scheduleIn(description, time, prio, method);
	}

	public static void scheduleIn(Duration duration, int prio, Runnable method) {
		requireSimContext().scheduleIn(duration, prio, method);
	}

	public static void scheduleIn(String description, Duration duration, int prio, Runnable method) {
		requireSimContext().scheduleIn(description, duration, prio, method);
	}

	public static void scheduleIn(long numUnits, TemporalUnit unit, int prio, Runnable method) {
		requireSimContext().scheduleIn(numUnits, unit, prio, method);
	}

	public static void scheduleIn(String description, long numUnits, TemporalUnit unit, int prio, Runnable method) {
		requireSimContext().scheduleIn(description, numUnits, unit, prio, method);
	}

	public static <T extends DblSequence> T initRndGen(T s, String streamName) {
		return requireSimContext().initRndGen(s, streamName);
	}

	public static String formatMsg(Object... params) {
		StringBuilder sb = new StringBuilder();
		sb.append(simTime());
		for (Object o : params) {
			sb.append('\t').append(String.valueOf(o));
		}

		return sb.toString();
	}

	public static Map<String, Object> simulationOf(SimRunnable r) {
		return simulationOf(null, simAction(r));
	}

	public static Map<String, Object> simulationOf(String name, SimRunnable r) {
		return simulationOf(name, simAction(r));
	}

	public static Map<String, Object> simulationOf(SimComponent... components) {
		return simulationOf(null, components);
	}

	public static Map<String, Object> simulationOf(String name, SimComponent... components) {
		return simulationOf(name, sim -> sim.addComponent(components));
	}

	public static Map<String, Object> simulationOf(SimAction a) {
		return simulationOf(null, a);
	}

	public static Map<String, Object> simulationOf(String name, SimAction a) {
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

	public static Future<Map<String, Object>> async(SimRunnable r) {
		return async(null, simAction(r));
	}

	public static Future<Map<String, Object>> async(String name, SimRunnable r) {
		return async(name, simAction(r));
	}

	public static Future<Map<String, Object>> async(SimAction a) {
		return async(null, a);
	}

	public static Future<Map<String, Object>> async(String name, SimAction a) {
		Simulation sim = createSim(name, a);
		return sim.performRunAsync();
	}

	public static Simulation createSim(String name, SimAction a) {
		Simulation sim = new Simulation();
		sim.setName(name);
		sim.setMainProcessActions(a);
		return sim;
	}

	public static Locale locale() {
		Simulation sim = currentSimulation();
		return sim != null ? sim.getLocale() : I18n.DEF_LOCALE;
	}

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
