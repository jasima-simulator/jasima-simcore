package jasima.core.simulation.processes;

import static jasima.core.simulation.processes.ComponentStates.requireAllowedState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import jasima.core.simulation.SimEvent;
import jasima.core.simulation.SimEventMethodCall;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.processes.SimProcessUtil.SimRunnable;

public class SimProcess<R> implements Runnable {

	private static final ExecutorService exec = Executors.newCachedThreadPool(SimProcess::newWorkerThread);
	private static ThreadGroup simThreads = null;

	private static Thread newWorkerThread(Runnable r) {
		if (simThreads == null || simThreads.isDestroyed()) {
			simThreads = new ThreadGroup("jasimaSimThreads");
			simThreads.setDaemon(true);
		}
		return new Thread(simThreads, r);
	}

	public static enum ProcessState implements ComponentState {
		PASSIVE, SCHEDULED, RUNNING, TERMINATED, ERROR;
	}

	private final Simulation sim;
	private final Callable<R> action;
	private final SimEvent activateProcessEvent;

	private Thread executor;

	private ArrayList<Consumer<SimProcess<?>>> completionNotifiers;

	private ProcessState state;
	private R execResult;
	private Exception execFailure;
	private boolean wasSignaled;

	public SimProcess(Simulation sim, SimRunnable r) {
		this(sim, SimProcessUtil.callable(r));
	}

	public SimProcess(Simulation sim, Callable<R> action) {
		super();

		this.sim = requireNonNull(sim);
		this.action = action;

		this.executor = null;
		this.state = ProcessState.PASSIVE;
		this.activateProcessEvent = new SimEventMethodCall(sim.simTime(), sim.currentPrio() + 1, "ActivateProcess",
				this::activateProcess);
	}

	/**
	 * This is the method to do the real work of the process, i.e., defining its
	 * behaviour. If a {@link SimRunnable} or {@link Callable<R>} was specified,
	 * then these will be executed. If a {@link SimProcess} is created by
	 * sub-classing, then override this method to define its behaviour.
	 * 
	 * @return The process's return value, if any (return null if not needed).
	 * @throws Exception If there is any uncaught exception during execution.
	 */
	protected R doRun() throws Exception {
		if (action != null) {
			return action.call();
		} else {
			return null;
		}
	}

	private void activateProcess() {
		SimProcess<?> current = sim.currentProcess();
		state = ProcessState.RUNNING;

		if (current != this) {
			// switch if we are running in the context of another process
			switchTo(current, this);
		}
	}

	@Override
	public void run() {
		executor = Thread.currentThread();
		assert sim.currentProcess() == this;

		SimContext.setThreadContext(sim);
		try {
			execResult = doRun();
			state = ProcessState.TERMINATED;
		} catch (Exception e) {
			execFailure = e;
			state = ProcessState.ERROR;
		} finally {
			executor = null;
			runCompleteCallbacks();

			if (caller != null) {
				switchTo(this, caller);
			}
		}
	}

	private void yield() throws MightBlock {
		assert sim.currentProcess() == this;
		sim.setCurrentProcess(null);

		// run the event loop in the current Thread (until it switches to a new one)
		sim.runEventLoop();
	}

	private static void switchTo(SimProcess<?> from, SimProcess<?> to) {
		// start process "to"
		assert !to.hasFinished();
		if (to.executor == null) {
			// start execution
			exec.submit(to);
		} else {
			// resume
			to.wasSignaled = true;
			LockSupport.unpark(to.executor);
		}

		// pause current process "from"
		if (!from.hasFinished()) {
			assert from.executor == Thread.currentThread();

			from.wasSignaled = false;
			while (!from.wasSignaled) { // guard against spurious wakeups
				LockSupport.park();
			}
		}
	}

	public void awakeIn(double deltaT) {
		awakeAt(sim.simTime() + deltaT);
	}

	public void awakeAt(double tAbs) {
		requireAllowedState(state, EnumSet.of(ProcessState.PASSIVE));

		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;
	}

	public void waitFor(double deltaT) throws MightBlock {
		waitUntil(sim.simTime() + deltaT);
	}

	public void resume() {
		requireAllowedState(state, EnumSet.of(ProcessState.PASSIVE));

		scheduleReactivateAt(sim.simTime());
	}

	public void cancel() {
		requireAllowedState(state, EnumSet.of(ProcessState.SCHEDULED));

		sim.unschedule(activateProcessEvent);
		state = ProcessState.PASSIVE;
	}

	public void waitUntil(double tAbs) throws MightBlock {
		requireAllowedState(state, EnumSet.of(ProcessState.RUNNING));

		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;

		yield();
	}

	public void suspend() throws MightBlock {
		requireAllowedState(state, EnumSet.of(ProcessState.RUNNING));

		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		state = ProcessState.PASSIVE;

		yield();
	}

	public void join() throws MightBlock {
		if (hasFinished()) {
			return;
		}

		SimProcess<?> current = sim.currentProcess();
		if (current == null) {
			throw new UnsupportedOperationException(); // call from plain event / sim-thread
		}
		if (current == this) {
			throw new IllegalStateException("A process can't wait for its own results.");
		}

		addCompletionNotifier(p -> current.scheduleReactivateAt(sim.simTime()));

		yield();
	}

	public R get() {
		if (!hasFinished()) {
			throw new IllegalStateException();
		}
		if (execFailure != null) {
			throw new RuntimeException(execFailure);
		}
		return execResult;
	}

	public boolean hasFinished() {
		return state == ProcessState.TERMINATED || state == ProcessState.ERROR;
	}

	private void scheduleReactivateAt(double t) {
		activateProcessEvent.setTime(t);
		sim.schedule(activateProcessEvent);
	}

	private void addCompletionNotifier(Consumer<SimProcess<?>> callback) {
		if (completionNotifiers == null) {
			completionNotifiers = new ArrayList<>();
		}

		completionNotifiers.add(callback);
	}

	private void runCompleteCallbacks() {
		try {
			if (completionNotifiers != null) {
				completionNotifiers.forEach(callback -> callback.accept(this));
			}
		} finally {
			completionNotifiers = null;
		}
	}

	// boring getter / setter below

	public Simulation getSim() {
		return sim;
	}

	public ProcessState processState() {
		return state;
	}

}
