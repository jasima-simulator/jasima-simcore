package jasima.core.simulation;

import static jasima.core.simulation.Simulation.I18nConsts.NO_CONTEXT;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.SimProcessUtil.SimRunnable;
import jasima.core.util.i18n.I18n;

public class SimContext {

	private static ThreadLocal<Simulation> currentSim = new ThreadLocal<>();

	public static Simulation currentSimulation() {
		return currentSim.get();
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

	public static String message(Enum<?> key) {
		return I18n.message(locale(), key);
	}

	public static String formattedMessage(Enum<?> key, Object... params) {
		return I18n.formattedMessage(locale(), key, params);
	}

	public static SimProcess<Void> activate(SimRunnable r) {
		return activate(SimProcessUtil.callable(r));
	}

	public static <T> SimProcess<T> activate(Callable<T> c) {
		SimProcess<T> p = new SimProcess<>(requireSimContext(), c);
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

	public static Map<String, Object> of(SimRunnable r, String name) {
		return of(SimProcessUtil.callable(r), name);
	}

	public static Map<String, Object> of(Callable<?> actions, String name) {
		Simulation sim = new Simulation();
		sim.setName(name);

		SimProcess<?> mainProcess = new SimProcess<>(sim, actions);
		mainProcess.awakeIn(0.0);

		return sim.performRun();
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

}
