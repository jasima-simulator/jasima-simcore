package jasima.core.simulation;

import static jasima.core.util.ComponentStates.requireAllowedState;
import static jasima.core.util.SimProcessUtil.currentExecutor;
import static jasima.core.util.SimProcessUtil.startExecuting;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jasima.core.simulation.Simulation.ErrorHandler;
import jasima.core.simulation.Simulation.SimExecState;
import jasima.core.simulation.util.SimEventMethodCall;
import jasima.core.util.SequenceNumberService;
import jasima.core.util.SimProcessUtil;
import jasima.core.util.SimProcessUtil.SimAction;
import jasima.core.util.SimProcessUtil.SimCallable;
import jasima.core.util.SimProcessUtil.SimRunnable;
import jasima.core.util.observer.ObservableValue;
import jasima.core.util.observer.ObservableValues;

/**
 * Process abstraction for the process-oriented simulation world view. A process
 * is similar to a Java Thread, but its execution can be interrupted/paused
 * until certain events occur or a certain point in simulation time is reached.
 * <p>
 * Behaviour of a SimProcess can be specified by either specifying a
 * {@link SimAction}/{@link SimCallable} or alternatively by sub-classing and
 * overriding the method {@link #lifecycle()}.
 * <p>
 * A {@link SimProcess} can be in one of the following states
 * ({@see ProcessState}):
 * <dl>
 * <dt>PASSIVE</dt>
 * <dd>A process that could be started or resumed by another process or event.
 * This is the initial state of a SimProcess.</dd>
 * <dt>SCHEDULED</dt>
 * <dd>A process that is scheduled for (re-)activation at a certain point in
 * simulation time.</dd>
 * <dt>RUNNING</dt>
 * <dd>A process that is currently executing its lifecycle. At each point in
 * time only a single process can be in state RUNNING.</dd>
 * <dt>TERMINATED</dt>
 * <dd>A process that has completed executing its lifecycle actions
 * normally.</dd>
 * <dt>ERROR</dt>
 * <dd>A processed that finished execution with an unhandled
 * {@code Exception}.</dd>
 * </dl>
 * 
 * @author torsten.hildebrandt@simplan.de
 * @since 3.0
 *
 * @param <R> The return type of the process. Can be {@link Void}.
 */
public class SimProcess<R> implements Runnable {

	private static final Logger log = LogManager.getLogger(SimProcess.class);

	/**
	 * Possible states of a SimProcess.
	 */
	public static enum ProcessState {
		PASSIVE, SCHEDULED, RUNNING, TERMINATED, ERROR;
	}

	/**
	 * Marker for all methods that might block, i.e., where execution might not
	 * finish at the same simulation time when it was started.
	 */
	public static class MightBlock extends Exception {

		private static final long serialVersionUID = 3091300075872193106L;

		/**
		 * Private constructor to prevent instantiation, this class is just a marker.
		 */
		private MightBlock() {
		}

	}

	private static final class TerminateProcess extends Error {
		private static final long serialVersionUID = 7242165456133430192L;
	}

	private final Simulation sim;
	private final SimCallable<R> action;
	private final String name;
	private ErrorHandler localErrorHandler;
	private SimComponent owner;
	private ArrayList<Consumer<SimProcess<R>>> completionNotifiers;

	private ProcessState state;
	private R execResult;
	private Exception execFailure;

	final SimEvent activateProcessEvent;
	volatile Thread executor;

	private volatile boolean wasSignaled;
	private boolean reactivated;

	public SimProcess(Simulation sim) {
		this(sim, (SimCallable<R>) null, null);
	}

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

	public SimProcess(Simulation sim, SimAction a, String name) {
		this(sim, SimProcessUtil.simCallable(a), name);
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
		this.sim.processNew(this);
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
	protected R lifecycle() throws Exception {
		if (action != null) {
			return action.call(sim);
		} else {
			return null;
		}
	}

	private boolean handleError(Exception e, boolean skipLocal) {
		boolean shouldRethrow = true;

		if (localErrorHandler != null && !skipLocal) {
			shouldRethrow = localErrorHandler.test(e);
		}

		if (shouldRethrow) {
			shouldRethrow = sim.handleError(e);
		}

		return shouldRethrow;
	}

	/**
	 * Don't call this method directly. It is just an internal method and only
	 * public to implement {@link Runnable}.
	 */
	@Override
	public void run() {
		requireAllowedState(state, ProcessState.RUNNING);

		executor = currentExecutor();
		String oldName = executor.getName();
		executor.setName(getName());

		SimContext.setThreadContext(sim);
		assert sim.currentProcess() == this;
		assert sim.getEventLoopProcess() == this;

		log.trace("process started: {}", getName());

		try {
			try {
				execResult = lifecycle();
				execFailure = null;
				state = ProcessState.TERMINATED;
				sim.processTerminated(this);
			} catch (Exception e) {
				execResult = null;
				execFailure = e;
				state = ProcessState.ERROR;
				sim.processTerminated(this);
				if (handleError(e, false)) {
					sim.terminateWithException(e); // unrecoverable error
				}
			}

			log.trace("actions finished, " + (completionNotifiers == null ? "null" : "" + completionNotifiers.size()));

			runCompleteCallbacks();

			log.trace("callbacks run");

			// yield after finish to process next events in current Thread
			yield();

			log.trace("yielded");
		} catch (TerminateProcess tp) {
			log.trace("process terminated: " + getName() + "  " + sim.currentProcess() + "  "
					+ sim.getEventLoopProcess());
		} catch (Throwable t) {
			System.err.println(Thread.currentThread() + " " + t);
			t.printStackTrace();
			throw t;
		} finally {
			log.trace("process finished: {}", getName());

			executor.setName(oldName);
			executor = null;
			SimContext.setThreadContext(null);
		}
	}

	/**
	 * Give control to some other process.
	 */
	private void yield() {
		assert sim.getEventLoopProcess() == this;
		assert SimContext.currentSimulation() == sim;
		sim.setCurrentProcess(null);

		// run the event loop in the current Thread (until it switches to a new one)
		reactivated = false;
		while (!reactivated && sim.continueSim()) {
			try {
				sim.handleNextEvent();
			} catch (RuntimeException e) {
				if (handleError(e, true)) {
					sim.terminateWithException(e); // unrecoverable error
				}
			}
		}

		// event loop can be finished because whole sim is finished or current process
		// is supposed to continue (either in its doRun method or after yield in run()).
		if (!reactivated && !sim.continueSim()) {
			log.trace("backtomain");
			sim.state.set(SimExecState.TERMINATING);
			if (isMainProcess()) {
				throw new TerminateProcess();
			} else {
				sim.mainProcess().activateProcess();
			}
		}
	}

	/**
	 * Activate the current process. This method is called internally by the
	 * activateProcessEvent.
	 */
	void activateProcess() {
		if (sim.state() != SimExecState.TERMINATING) {
			requireAllowedState(state, ProcessState.PASSIVE, ProcessState.SCHEDULED);
			state = ProcessState.RUNNING;
		}
		sim.setCurrentProcess(this);

		log.trace("process activating: {}", getName());

		SimProcess<?> current = sim.getEventLoopProcess();
		if (current != this) {
			// switch Threads if we are running in the context of another process
			sim.setEventLoopProcess(this);

			this.start();

			current.pause();
		} else {
			// Thread stays the same
			reactivated = true; // stop event loop after resuming from pause
		}
	}

	void terminateWaiting() {
		// this method is called once from the main simulation thread when simulation is
		// terminating
		log.trace("trying to terminate " + getName() + " in state " + state + ", executor=" + executor);
		assert sim.state() == SimExecState.TERMINATING;
		this.start(); // will throw TerminateProcess
	}

	private void start() {
		assert !hasFinished() || isMainProcess() || sim.state() == SimExecState.TERMINATING;
		if (executor == null) {
			requireAllowedState(state, ProcessState.RUNNING);
			// start new
			startExecuting(this);
		} else {
			// resume
			reactivated = true;
			wasSignaled = true;

			SimProcessUtil.continueWith(executor);
		}
	}

	private void pause() throws TerminateProcess {
		if (hasFinished() && !isMainProcess()) {
			reactivated = true;
			return; // do nothing
		}

		while (!wasSignaled) { // guard against spurious wakeups
			SimProcessUtil.pauseExecuting(executor);
		}
		wasSignaled = false;

		if (sim.state() == SimExecState.TERMINATING) {
			throw new TerminateProcess();
		}
	}

	private boolean isMainProcess() {
		return this == sim.mainProcess();
	}

	/**
	 * Resumes execution of a PASSIVE process after the current event finished.
	 */
	public void resume() {
		awakeAt(sim.simTime());
		log.trace("process {} resuming", getName());
	}

	/**
	 * Awakes a PASSIVE process after a certain amount of time.
	 */
	public void awakeIn(double deltaT) {
		awakeAt(sim.simTime() + deltaT);
	}

	/**
	 * Awakes a PASSIVE process after a certain amount of time.
	 */
	public void awakeIn(long amount, TemporalUnit u) {
		awakeIn(sim.simTime() + sim.toSimTime(amount, u));
	}

	/**
	 * Awakes a PASSIVE process after a certain amount of time.
	 */
	public void awakeIn(Duration d) {
		awakeIn(sim.simTime() + sim.toSimTime(d));
	}

	/**
	 * Awakes a PASSIVE process at a certain time.
	 */
	public void awakeAt(double tAbs) {
		requireAllowedState(state, ProcessState.PASSIVE);
		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;
		log.trace("process {} awaking at {}", getName(), tAbs);
	}

	/**
	 * Awakes a PASSIVE process at a certain time.
	 */
	public void awakeAt(Instant instant) {
		awakeAt(sim.toSimTime(instant));
	}

	/**
	 * Cancels execution of a SCHEDULED process and puts it into PASSIVE state.
	 */
	public SimProcess<R> cancel() {
		requireAllowedState(state, ProcessState.SCHEDULED);

		sim.unschedule(activateProcessEvent);
		state = ProcessState.PASSIVE;

		log.trace("waiting process canceled: {}", getName());

		return this;
	}

	/**
	 * Pauses execution of the currently RUNNING process for a certain amount of
	 * time.
	 */
	public SimProcess<R> waitFor(double deltaT) throws MightBlock {
		waitUntil(sim.simTime() + deltaT);
		return this;
	}

	/**
	 * Pauses execution of the currently RUNNING process for a certain amount of
	 * time.
	 */
	public SimProcess<R> waitFor(long amount, TemporalUnit u) throws MightBlock {
		waitUntil(sim.simTime() + sim.toSimTime(amount, u));
		return this;
	}

	/**
	 * Pauses execution of the currently RUNNING process for a certain amount of
	 * time.
	 */
	public SimProcess<R> waitFor(Duration d) throws MightBlock {
		waitUntil(sim.simTime() + sim.toSimTime(d));
		return this;
	}

	/**
	 * Pauses execution of the currently RUNNING process until a certain absolute
	 * time.
	 */
	public SimProcess<R> waitUntil(double tAbs) throws MightBlock {
		requireAllowedState(state, ProcessState.RUNNING);
		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		scheduleReactivateAt(tAbs);
		state = ProcessState.SCHEDULED;

		log.trace("process {} waiting until {}", getName(), tAbs);

		yield();
		return this;
	}

	/**
	 * Pauses execution of the currently RUNNING process until a certain absolute
	 * time.
	 */
	public SimProcess<R> waitUntil(Instant instant) throws MightBlock {
		waitUntil(sim.toSimTime(instant));
		return this;
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
	public boolean waitCondition(ObservableValue<Boolean> triggerCondition) throws MightBlock {
		// complicated formulation of true check below to interpret NULL value as false
		if (!Boolean.TRUE.equals(triggerCondition.get())) {
			ObservableValues.whenTrueExecuteOnce(triggerCondition, this::resume);
			suspend(); // wait until condition is true
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
	public <T> boolean waitCondition(Function<T, Boolean> triggerCondition, ObservableValue<? extends T> observable)
			throws MightBlock {
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
	public <T1, T2> boolean waitCondition(BiFunction<T1, T2, Boolean> triggerCondition,
			ObservableValue<? extends T1> obs1, ObservableValue<? extends T2> obs2) throws MightBlock {
		ObservableValue<Boolean> c = ObservableValues.fromBinaryOperation(triggerCondition, obs1, obs2);
		return waitCondition(c);
	}

	/**
	 * Puts the current process into PASSIVE state, waiting re-activation by some
	 * other component or event.
	 * 
	 * @return {@code this} to allow chaining of calls.
	 */
	public SimProcess<R> suspend() throws MightBlock {
		requireAllowedState(state, ProcessState.RUNNING);

		assert sim.currentEvent() == activateProcessEvent;
		assert sim.currentProcess() == this;

		state = ProcessState.PASSIVE;
		log.trace("process suspended: {}", getName());

		yield();
		return this;
	}

	/**
	 * Blocks the calling process (puts it into PASSIVE state) until the process,
	 * this method is called on, has finished.
	 * 
	 * @return {@code this} to allow chaining of calls.
	 */
	public SimProcess<R> join() throws MightBlock {
		if (hasFinished()) {
			return this;
		}

		SimProcess<?> current = sim.currentProcess();
		if (current == null) {
			throw new UnsupportedOperationException(); // call from plain event / sim-thread
		}
		if (current == this) {
			throw new IllegalStateException("A process can't wait for its own completion.");
		}

		// schedule re-activation of "current"
		addCompletionNotifier(p -> {
			current.scheduleReactivateAt(sim.simTime());
		});

		// put current process in passive state
		current.state = ProcessState.PASSIVE;
		log.trace("process {} joining {}", current.getName(), getName());
		current.yield();

		return this;
	}

	/**
	 * Returns the result of any computation this process might have completed. This
	 * method might only be called after the process has finished. If the process
	 * terminated with an Exception, then this Exception is re-thrown wrapped in a
	 * RuntimeException.
	 * 
	 * @throws IllegalStateException If the process has not finished yet.
	 * @throws RuntimeException      Wraps any unhandled exception that might have
	 *                               occurred.
	 * @return The result of this computation.
	 */
	public @Nullable R get() {
		if (!hasFinished()) {
			throw new IllegalStateException("Process not finished yet.");
		}
		if (execFailure != null) {
			throw new RuntimeException(execFailure);
		}
		return execResult;
	}

	/**
	 * Returns true if the process has finished (either normally or withn an
	 * unhandled Exception).
	 */
	public boolean hasFinished() {
		return state == ProcessState.TERMINATED || state == ProcessState.ERROR;
	}

	private void scheduleReactivateAt(double t) {
		scheduleReactivateAt(t, activateProcessEvent.getPrio());
	}

	private void scheduleReactivateAt(double t, int prio) {
		activateProcessEvent.setPrio(prio);
		activateProcessEvent.setTime(t);
		sim.schedule(activateProcessEvent);
	}

	public synchronized void addCompletionNotifier(Consumer<SimProcess<R>> callback) {
		if (completionNotifiers == null) {
			completionNotifiers = new ArrayList<>();
		}

		completionNotifiers.add(callback);
	}

	private synchronized void runCompleteCallbacks() {
		try {
			if (completionNotifiers != null) {
				completionNotifiers.forEach(callback -> callback.accept(this));
			}
		} finally {
			// notifiers are executed exactly once
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
		String prefix = null;
		if (isMainProcess()) {
			prefix = sim.getName();
		} else if (getOwner() != null) {
			prefix = getOwner().getHierarchicalName();
		}
		return prefix != null ? prefix + '.' + name : name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public SimComponent getOwner() {
		return owner;
	}

	public void setOwner(SimComponent owner) {
		this.owner = owner;
	}

}
