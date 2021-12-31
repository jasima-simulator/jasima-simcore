package jasima.core.simulation.util;

import static jasima.core.util.SimProcessUtil.simCallable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.Simulation;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimCallable;
import jasima.core.util.SimProcessUtil.SimRunnable;

public interface ProcessActivator {

	Simulation getSim();

	static final Logger log = LogManager.getLogger(ProcessActivator.class);

	public default SimProcess<Void> activate(SimRunnable r) {
		return activateCallable(null, simCallable(r));
	}

	public default SimProcess<Void> activate(String name, SimRunnable r) {
		return activateCallable(name, simCallable(r));
	}

	public default <T> SimProcess<T> activate(SimAction a) {
		return activateCallable(null, simCallable(a));
	}

	public default <T> SimProcess<T> activate(String name, SimAction a) {
		return activateCallable(name, simCallable(a));
	}

	public default <T> SimProcess<T> activateCallable(Callable<T> c) {
		return activateCallable(null, simCallable(c));
	}

	public default <T> SimProcess<T> activateCallable(String name, Callable<T> c) {
		return activateCallable(name, simCallable(c));
	}

	public default <T> SimProcess<T> activateCallable(SimCallable<T> a) {
		return activateCallable(null, a);
	}

	public default <T> SimProcess<T> activateCallable(String name, SimCallable<T> a) {
		Simulation ctxSim = SimContext.currentSimulation();
		if (ctxSim != null && ctxSim != getSim()) {
			throw new IllegalStateException(); // should not occur
		}

		SimProcess<T> p = new SimProcess<>(getSim(), a, name);

		FutureTask<SimProcess<T>> f = new FutureTask<>(() -> {
			p.awakeIn(0.0);
			return p;
		});

		if (ctxSim == null) {
			// called from some external thread
			getSim().runInSimThread(() -> f.run());
		} else {
			// called directly by sim thread
			f.run();
		}

		if (ctxSim != null) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				log.error("there was a problem activating a process", e);
				throw new RuntimeException(e);
			}
		}

		return p;
	}

}
