package jasima.core.simulation;

import static jasima.core.simulation.SimContext.I18nConsts.NESTED_FAILED;
import static jasima.core.simulation.SimContext.I18nConsts.NO_CONTEXT;
import static jasima.core.util.SimProcessUtil.simAction;
import static jasima.core.util.SimProcessUtil.simCallable;
import static jasima.core.util.StandardExtensionImpl.JASIMA_CORE_RES_BUNDLE;

import java.time.temporal.TemporalUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.simulation.Simulation.SimulationFailed;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimCallable;
import jasima.core.util.SimProcessUtil.SimRunnable;
import jasima.core.util.i18n.I18n;

public class SimContext {

	private static final Logger out = LogManager.getLogger("jasima");

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

	public static String message(Enum<?> key) {
		return I18n.message(locale(), key);
	}

	public static String message(String keyName) {
		return I18n.message(locale(), keyName);
	}

	public static String formattedMessage(Enum<?> key, Object... params) {
		return I18n.formattedMessage(locale(), key, params);
	}

	public static SimProcess<Void> activate(SimRunnable r) {
		return activateCallable(null, simCallable(r));
	}

	public static SimProcess<Void> activate(String name, SimRunnable r) {
		return activateCallable(name, simCallable(r));
	}

	public static <T> SimProcess<T> activate(SimAction a) {
		return activateCallable(null, simCallable(a));
	}

	public static <T> SimProcess<T> activate(String name, SimAction a) {
		return activateCallable(name, simCallable(a));
	}

	public static <T> SimProcess<T> activateCallable(Callable<T> c) {
		return activateCallable(null, simCallable(c));
	}

	public static <T> SimProcess<T> activateCallable(String name, Callable<T> c) {
		return activateCallable(name, simCallable(c));
	}

	public static <T> SimProcess<T> activateCallable(SimCallable<T> a) {
		return activateCallable(null, a);
	}

	public static <T> SimProcess<T> activateCallable(String name, SimCallable<T> a) {
		SimProcess<T> p = new SimProcess<>(requireSimContext(), a, name);
		p.awakeIn(0.0);
		return p;
	}

	public static void waitFor(double deltaT) throws MightBlock {
		currentProcess().waitFor(deltaT);
	}

	public static void waitUntil(double tAbs) throws MightBlock {
		currentProcess().waitUntil(tAbs);
	}

	public static void suspend() throws MightBlock {
		currentProcess().suspend();
	}

	public static void trace(Object... params) {
		out.error(() -> formatMsg(params));
	}

	public static String formatMsg(Object... params) {
		StringBuilder sb = new StringBuilder();
		sb.append(simTime());
		for (Object o : params) {
			sb.append('\t').append(String.valueOf(o));
		}

		return sb.toString();
	}

	public static Map<String, Object> of(SimRunnable r) {
		return of(null, simAction(r));
	}

	public static Map<String, Object> of(String name, SimRunnable r) {
		return of(name, simAction(r));
	}

	public static Map<String, Object> of(SimAction a) {
		return of(null, a);
	}

	public static Map<String, Object> of(String name, SimAction a) {
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
		if (sim != null && currentSim.get() != null) {
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
