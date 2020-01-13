package jasima.core.simulation.processes;

import java.util.concurrent.Callable;

import jasima.core.simulation.Simulation;
import jasima.core.simulation.processes.SimProcessUtil.SimRunnable;

public class SimContext {

	private static ThreadLocal<Simulation> currentSim = new ThreadLocal<>();

	public static Simulation currentSimulation() {
		return currentSim.get();
	}

	public static SimProcess<?> currentProcess() {
		return requireSimContext().currentProcess();
	}

	public static double simTime() {
		return requireSimContext().simTime();
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

	public static Simulation requireSimContext() {
		Simulation s = currentSimulation();
		if (s == null) {
			throw new IllegalStateException("No active simulation context found.");
		} else {
			return s;
		}
	}

	public static void setThreadContext(Simulation sim) {
		if (sim != null && currentSim.get() != null) {
			throw new IllegalStateException(); // old context not properly cleared?
		}
		currentSim.set(sim);
	}

}
