package jasima.core.simulation;

import static jasima.core.util.ComponentStates.requireAllowedState;
import static jasima.core.util.SimProcessUtil.continueWith;
import static jasima.core.util.SimProcessUtil.currentExecutor;
import static jasima.core.util.SimProcessUtil.pauseExecuting;
import static jasima.core.util.SimProcessUtil.startExecuting;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jasima.core.simulation.Simulation.ErrorHandler;
import jasima.core.simulation.Simulation.SimExecState;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimCallable;
import jasima.core.util.SimProcessUtil.SimRunnable;

public class SimProcess<R> implements Runnable {

	private static final Logger logger = LogManager.getLogger("jasima.output");

	public static enum ProcessState {
		PASSIVE, SCHEDULED, RUNNING, TERMINATED, ERROR;
	}

	/**
	 * Marker for all methods that might block, i.e., where execution might not
	 * finish at the same simulation time when it was started.
	 */
	public static class MightBlock extends Exception {

		private static final long serialVersionUID = 3091300075872193106L;

		private MightBlock() { // prevent instantiation, this class is just a maker
		}

	}

	private static final class TerminateProcess extends Error {
		private static final long serialVersionUID = 7242165456133430192L;
	}

	private final Simulation sim;
	private final SimCallable<R> action;
	private final String name;
	private ErrorHandler localErrorHandler;

	private final SimEvent activateProcessEvent;

	private ProcessState state;
	private R execResult;
	private Exception execFailure;

	private ArrayList<Consumer<SimProcess<R>>> completionNotifiers;
	private volatile boolean wasSignaled;
	private volatile boolean reactivated;
	volatile Thread executor;

	public SimProcess(Simulation sim, SimRunnable r) {
		this(sim, SimProcessUtil.simCallable(r), null);
	}

	public SimProcess(Simulation sim, Callable<R> c) {
		this(sim, SimProcessUtil.simCallable(c), null);
	}

	public SimProcess(Simulation sim, SimAction a) {
		this(sim, SimProcessUtil.simCallable(a), null);
	}

	public SimProcess(Simulation sim, SimCallable<R> c) {
		this(sim, c, null);
	}

	public SimProcess(Simulation sim, SimRunnable r, String name) {
		this(sim, SimProcessUtil.simCallable(r), name);
	}

	public SimProcess(Simulation sim, Callable<R> c, String name) {
		this(sim, SimProcessUtil.simCallable(c), name);
	}

	public SimProcess(Simulation sim, SimCallable<R> action, String name) {
		super();

		this.sim = requireNonNull(sim);
		this.name = name != null ? name : SequenceNumberService.getFor(sim).nextFormattedValue("simProcess");
		this.action = action;
		this.localErrorHandler = null;
		this.executor = null;
		this.state = ProcessState.PASSIVE;
		this.activateProcessEvent = new SimEventMethodCall(sim.simTime(), sim.currentPrio() + 1, "ActivateProcess",
				this::activateProcess);
		this.sim.processActivated(this);
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
			return action.call(sim);
		} else {
			return null;
		}
	}

	protected boolean handleError(Exception e) {
		boolean shouldRethrow = true;

		if (localErrorHandler != null) {
			shouldRethrow = localErrorHandler.test(e);
		}

		if (shouldRethrow) {
			shouldRethrow = sim.handleError(e);
		}

		return shouldRethrow;
	}

	@Override
	public void run() {
		try {
			logger.error("process started: " + getName());

			requireAllowedState(state, ProcessState.RUNNING);

			executor = currentExecutor();
			executor.setName(getName());
			SimContext.setThreadContext(sim);
			assert sim.currentProcess() == this;

			try {
				execResult = doRun();
				execFailure = null;
				state = ProcessState.TERMINATED;
			} catch (Exception e) {
				execResult = null;
				execFailure = e;
				state = ProcessState.ERROR;
				if (handleError(e)) {
					sim.terminateWithException(e); // unrecoverable error
				}
			}

			logger.error("actions finished");

			runCompleteCallbacks();

			yield();

			sim.processTerminated(this);
		} catch (TerminateProcess tp) {
			logger.error("process terminated: " + getName() + "  " + sim.currentProcess() + "  "
					+ sim.getEventLoopProcess());
		} catch (Throwable t) {
			System.err.println(Thread.currentThread() + " " + t);
			t.printStackTrace();
			throw t;
		} finally {
			executor = null;
			SimContext.setThreadContext(null);
			logger.error("process finished: " + getName());
		}
	}

	private void yield() {
		assert sim.currentProcess() == this;
		assert SimContext.currentSimulation() == sim;
		sim.setCurrentProcess(null);

		logger.error("yield1");

		// run the event loop in the current Thread (until it switches to a new one)
		reactivated = false;
		while (!reactivated && sim.continueSim()) {
			try {
				sim.handleNextEvent();
			} catch (RuntimeException e) {
				if (handleError(e)) {
					sim.terminateWithException(e); // unrecoverable error
				}
			}
		}

		logger.error("yield2");

		// event loop can be finished because whole sim is finished or current process
		// is supposed to continue (either in its doRun method or after yield in run()).
		if (!reactivated && !sim.continueSim() && !isMainProcess()) {
			logger.error("backtomain");
			sim.mainProcess().start();
			this.pause();
		}
	}

	void activateProcess() {
		requireAllowedState(state, ProcessState.PASSIVE, ProcessState.SCHEDULED);

		reactivated = true; // stop event loop after resuming from pause
		state = ProcessState.RUNNING;
		sim.setCurrentProcess(this);

		logger.error("activating " + this);

		SimProcess<?> current = sim.getEventLoopProcess();
		if (current != this) {
			// switch if we are running in the context of another process
			start();
			current.reactivated = true;
			if (!current.hasFinished() || current.isMainProcess()) {
				current.pause();
			}
		}
	}

	void terminateWaiting() {
		// this method is called once from the main simulation thread when simulation is
		// terminating
		logger.error("trying to terminate " + getName());
		assert sim.state() == SimExecState.TERMINATING;
		this.start(); // will throw TerminateProcess
	}

	private void start() {
		assert !hasFinished() || isMainProcess() || sim.state() == SimExecState.TERMINATING;
		sim.setEventLoopProcess(this);
		if (executor == null) {
			logger.error("start1 " + getName());
			requireAllowedState(state, ProcessState.RUNNING);

			// start new
			startExecuting(this);
		} else {
			logger.error("start2 " + getName());
			// resume
			wasSignaled = true;
			continueWith(executor);
		}
	}

	private void pause() throws TerminateProcess {
		wasSignaled = false;
		while (!wasSignaled) { // guard against spurious wakeups
			pauseExecuting(executor);
		}
		if (sim.state() == SimExecState.TERMINATING) {
			throw new TerminateProcess();
		}
	}

	private boolean isMainProcess() {
		return this == sim.mainProcess();
	}

	public void awakeIn(double deltaT) {
		awakeAt(sim.simTime() + deltaT);
	}

	public void awakeAt(double tAbs) {
		requireAllowedState(state, ProcessState.PASSIVE);
		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;
	}

	public void waitFor(double deltaT) throws MightBlock {
		waitUntil(sim.simTime() + deltaT);
	}

	public void resume() {
		requireAllowedState(state, ProcessState.PASSIVE);

		scheduleReactivateAt(sim.simTime());
	}

	public void cancel() {
		requireAllowedState(state, ProcessState.SCHEDULED);

		sim.unschedule(activateProcessEvent);
		state = ProcessState.PASSIVE;
	}

	public void waitUntil(double tAbs) throws MightBlock {
		requireAllowedState(state, ProcessState.RUNNING);

		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;

		yield();
	}

	public void suspend() throws MightBlock {
		requireAllowedState(state, ProcessState.RUNNING);

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

		current.state = ProcessState.PASSIVE;
		current.yield();
	}

	public R get() {
		if (!hasFinished()) {
			throw new IllegalStateException("Process not finished yet.");
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

	private void addCompletionNotifier(Consumer<SimProcess<R>> callback) {
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

	public ErrorHandler getLocalErrorHandler() {
		return localErrorHandler;
	}

	public void setLocalErrorHandler(ErrorHandler h) {
		localErrorHandler = h;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

}
