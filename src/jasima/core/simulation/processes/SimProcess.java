package jasima.core.simulation.processes;

import static jasima.core.simulation.processes.ComponentStates.requireAllowedState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import jasima.core.simulation.SimEventMethodCall;
import jasima.core.simulation.SimEvent;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.processes.SimProcessUtil.SimRunnable;

public class SimProcess<R> {

	static enum Messages {
		TEST1, TEST2
	}

	public static Coroutine startExecuting(SimProcess<?> p) {
		return ThreadBasedCoroutine.of(p);
	}

	private final Simulation sim;
	private final SimEvent activateProcessEvent;

	private Coroutine executor;

	private final Callable<R> action;
	private R execResult;
	private Exception execFailure;

	public static enum ProcessState implements ComponentState {
		PASSIVE, SCHEDULED, RUNNING, TERMINATED, ERROR;
	}

	private ProcessState state;

	private ArrayList<Consumer<SimProcess<?>>> completionNotifiers;

	private double timeTerminated;
	private double timeActivated;

	public SimProcess(Simulation sim, SimRunnable r) {
		this(sim, SimProcessUtil.callable(r));
	}

	public SimProcess(Simulation sim, Callable<R> supp) {
		super();

		this.sim = requireNonNull(sim);
		this.action = supp;
		this.executor = null;
		this.state = ProcessState.PASSIVE;
		this.timeTerminated = this.timeActivated = Double.NaN;
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

	void internalRun() {
		assert sim.currentProcess() == this;

		SimContext.setThreadContext(sim);
		try {
			execResult = doRun();
			state = ProcessState.TERMINATED;
		} catch (Exception e) {
			execFailure = e;
			state = ProcessState.ERROR;
		} finally {
			finished();
		}
	}

	private void finished() {
		assert sim.currentProcess() == this;

		executor = null;
		timeTerminated = sim.simTime();

		runCompleteCallbacks();

		SimContext.setThreadContext(null);

		try {
			backToMainThread();
		} catch (MightBlock e) {
			throw new AssertionError("Can't occur.", e);
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

	public void waitUntil(double tAbs) throws MightBlock {
		requireAllowedState(state, EnumSet.of(ProcessState.RUNNING));

		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;

		backToMainThread();
	}

	public void suspend() throws MightBlock {
		requireAllowedState(state, EnumSet.of(ProcessState.RUNNING));

		state = ProcessState.PASSIVE;

		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		backToMainThread();
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

		backToMainThread();
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

	private void backToMainThread() throws MightBlock {
		assert sim.currentProcess() == this;
		sim.setCurrentProcess(null);

		sim.activate(execFailure);
		if (executor != null) {
			executor.deactivate();
		}
	}

	private void activateProcess() {
		assert sim.currentProcess() == null;
		sim.setCurrentProcess(this);

		// start or resume execution of the process
		state = ProcessState.RUNNING;
		if (executor == null) {
			timeActivated = sim.simTime();
			executor = startExecuting(this);
		} else {
			executor.activate();
		}

		// deactivate sim-thread (running this code)
		sim.deactivate();
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

	public double getTimeTerminated() {
		return timeTerminated;
	}

	public double getTimeActivated() {
		return timeActivated;
	}

	public Simulation getSim() {
		return sim;
	}

	public ProcessState processState() {
		return state;
	}

}
