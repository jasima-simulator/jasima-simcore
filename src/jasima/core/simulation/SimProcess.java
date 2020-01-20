package jasima.core.simulation;

import static jasima.core.util.ComponentStates.requireAllowedState;
import static jasima.core.util.SimProcessUtil.continueWith;
import static jasima.core.util.SimProcessUtil.currentExecutor;
import static jasima.core.util.SimProcessUtil.pauseExecuting;
import static jasima.core.util.SimProcessUtil.startExecuting;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jasima.core.simulation.Simulation.ErrorHandler;
import jasima.core.util.SimProcessUtil;
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

	private final Simulation sim;
	private final Callable<R> action;
	private ErrorHandler localErrorHandler;
	private final String name;

	private ProcessState state;
	private R execResult;
	private Exception execFailure;

	private ArrayList<Consumer<SimProcess<R>>> completionNotifiers;
	private final SimEvent activateProcessEvent;
	private boolean wasSignaled;
	private boolean reactivated;
	private Thread executor;

	public SimProcess(Simulation sim, SimRunnable r) {
		this(sim, SimProcessUtil.callable(r), null, null);
	}

	public SimProcess(Simulation sim, Callable<R> action) {
		this(sim, action, null, null);
	}

	public SimProcess(Simulation sim, SimRunnable r, String name) {
		this(sim, SimProcessUtil.callable(r), null, name);
	}

	public SimProcess(Simulation sim, Callable<R> action, String name) {
		this(sim, action, null, name);
	}

	public SimProcess(Simulation sim, Callable<R> action, ErrorHandler exceptionHandler, String name) {
		super();

		this.sim = requireNonNull(sim);
		this.name = name != null ? name : SequenceNumberService.getFor(sim).nextFormattedValue("simProcess");
		this.action = action;
		this.localErrorHandler = exceptionHandler;

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
			requireAllowedState(state, ProcessState.RUNNING);
			logger.error("process started");

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

			runCompleteCallbacks();

			yield();
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			executor = null;
			SimContext.setThreadContext(null);
			logger.error("process finished");
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
			switchTo(this, sim.mainProcess());
		}
	}

	void activateProcess() {
		requireAllowedState(state, ProcessState.PASSIVE, ProcessState.SCHEDULED);

		state = ProcessState.RUNNING;
		sim.setCurrentProcess(this);
		reactivated = true;

		logger.error("activating " + this);

		SimProcess<?> current = sim.getEventLoopProcess();
		if (current != this) {
			// switch if we are running in the context of another process
			switchTo(current, this);
		}
	}

	private static void switchTo(SimProcess<?> from, SimProcess<?> to) {
		assert from.sim == to.sim;

		// start process "to"
		assert !to.hasFinished() || to.isMainProcess();
		to.sim.setEventLoopProcess(to);
		if (to.executor == null) {
			// start new
			startExecuting(to);
		} else {
			// resume
			to.wasSignaled = true;
			continueWith(to.executor);
		}

		// pause current process "from"
		if (from != null) {
			from.wasSignaled = false;
			while (!from.wasSignaled) { // guard against spurious wakeups
				pauseExecuting(from.executor);
			}
		}
	}

	private boolean isMainProcess() {
		return this == sim.mainProcess();
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
