/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.experiment;

import static jasima.core.util.observer.ObservableValues.observable;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.experiment.ExperimentMessage.ExpPrintMessage;
import jasima.core.random.RandomFactory;
import jasima.core.run.ConsoleRunner;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;
import jasima.core.util.ValueStore;
import jasima.core.util.ValueStoreImpl;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierImpl;
import jasima.core.util.observer.ObservableValue;

/**
 * An Experiment is something that produces results depending on various
 * parameters. The usual lifecycle is to create an experiment, set parameters to
 * their proper values, execute the experiment by calling it's
 * {@code runExperiment()} method. After execution a set of results are
 * available using the {@code getResults()} method.
 * <p>
 * Experiments are not supposed to be executed more than once, e.g., to run
 * multiple replications of an experiment (see
 * {@link MultipleReplicationExperiment}) you have to create multiple instances.
 * Therefore experiments should be cloneable.
 * <p>
 * This class is intended as the base class for Experiments doing something
 * useful. Besides a name this class only has a single parameter "initialSeed"
 * (see {@link #getInitialSeed()}/{@link #setInitialSeed(long)}). This parameter
 * is supposed to be used as the starting value for all (pseudo) random number
 * generation activities at experiment runtime. This means two experiments
 * having the same {@code initialSeed} and all other experiment parameters being
 * the same should behave deterministically and produce exactly the same
 * results.
 * <p>
 * The only results produced by this class are "runTime" (type Double; measuring
 * the real time required to execute an experiment) and "expAborted" (type
 * Integer, a value &gt;0 indicates some problems causing early termination).
 * <p>
 * Experiments can have listeners registered (derived from
 * {@link ExperimentListener}), which are informed of various events such as an
 * experiment's start and completion and can be used by subclasses to provide
 * additional events.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class Experiment
		implements Notifier<Experiment, ExperimentMessage>, ValueStore, Cloneable, Serializable {

	/**
	 * Just an arbitrary default seed.
	 */
	public static final long DEFAULT_SEED = 0xd23284FEA3L;

	private static final long serialVersionUID = -5981694222402234985L;

	public static final String RUNTIME = "runTime";
	public static final String EXP_ABORTED = "expAborted";
	public static final String EXCEPTION = "exception";
	public static final String EXCEPTION_MESSAGE = "exceptionMessage";

	public enum ExperimentState {
		INITIAL, ABOUT_TO_START, RUNNING, FINISHED, ERROR
	}

	// fields to store parameters
	private String name = null;
	private long initialSeed = DEFAULT_SEED;
	private MsgCategory logLevel = MsgCategory.INFO;
	private NotifierImpl<Experiment, ExperimentMessage> notifierAdapter;
	private ValueStoreImpl valueStore;

	// fields used during run
	private transient int nestingLevel = 0;
	private transient long runTimeReal;
	protected transient volatile int aborted;
	private transient volatile boolean isCancelled;
	protected transient Map<String, Object> resultMap;
	protected transient Throwable error;
	private transient volatile ObservableValue<ExperimentState> state;

	@Deprecated
	public static class UniqueNamesCheckingHashMap extends LinkedHashMap<String, Object> {
		private static final long serialVersionUID = -6783419937586790463L;
		private boolean disableCheck;

		@Override
		public Object put(String key, Object value) {
			if (!isDisableCheck() && containsKey(key)) {
				throw new RuntimeException("Map already contains value '" + key + "'.");
			}
			return super.put(key.intern(), value);
		}

		public boolean isDisableCheck() {
			return disableCheck;
		}

		public void setDisableCheck(boolean disableCheck) {
			this.disableCheck = disableCheck;
		}
	}

	public Experiment() {
		super();

		state = observable(ExperimentState.INITIAL);
	}

	/**
	 * This method is called to perform any work that needs to be performed before
	 * the experiment can be initialized. It is called before the {@link #init()}
	 * method and can be used to perform additional parameter checks, for instance.
	 */
	protected void starting() {
	}

	/**
	 * This method is called to perform any initializations required before the
	 * experiment is run.
	 */
	protected void init() {
	}

	/**
	 * This method is called immediately before {@link #performRun()}, but after
	 * {@link #init()}.
	 */
	protected void beforeRun() {
	}

	/**
	 * Contains the code to actually do something useful. This is the only abstract
	 * method that sub-classes are required to implement.
	 */
	protected abstract void performRun();

	/**
	 * This method is called immediately after {@link #performRun()}, but before
	 * {@link #done()}.
	 */
	protected void afterRun() {
	}

	/**
	 * This method can be overridden to perform any required clean-up. It is
	 * executed immediately after {@link #afterRun()}, but before
	 * {@link #produceResults()} and {@link #finish()}.
	 */
	protected void done() {
	}

	/**
	 * Populates the result map {@link #resultMap} with values produced during
	 * experiment execution. The Experiment always adds the two results
	 * {@value #RUNTIME} and {@value EXP_ABORTED}.
	 */
	protected void produceResults() {
	}

	/**
	 * This method gives experiments and listeners a chance to view/modify results.
	 * It is called after {@link #produceResults()}.
	 */
	protected void finish() {
	}

	/**
	 * Runs the experiment in a synchronous way. This is the main method to call in
	 * order to execute an experiment. Sub-classes normally don't have to overwrite
	 * this method but create customized behavior by overriding one of the
	 * life-cycle methods like {@link #init()}, {@link #beforeRun()},
	 * {@link #performRun()} (this one is required), {@link #done()},
	 * {@link #produceResults()} or {@link #finish()}.
	 * 
	 * @return The results of experiment execution.
	 * @see #runExperimentAsync()
	 */
	public Map<String, Object> runExperiment() {
		aboutToStart();
		return runExperimentInternal();
	}

	/**
	 * Call the {@link #runExperiment()} method in an asynchronous way.
	 * 
	 * @param pool The {@link ExecutorService} to use.
	 * @return A {@link CompletableFuture} to obtain experiment results.
	 * @see #runExperiment()
	 * @see #runExperimentInternal()
	 * @see #runExperimentAsync()
	 */
	public ExperimentCompletableFuture runExperimentAsync(ExecutorService pool) {
		return ExperimentExecutor.runExperimentAsync(this, null, pool);
	}

	/**
	 * Trigger asynchronous execution of the experiment in the default thread pool.
	 * 
	 * @return A {@link Future} to obtain experiment results.
	 * @see #runExperiment()
	 * @see #runExperimentAsync(ExecutorService)
	 */
	public ExperimentCompletableFuture runExperimentAsync() {
		return runExperimentAsync(Util.DEF_POOL);
	}

	/**
	 * Runs the experiment. This is the main method to call in order to execute an
	 * experiment. Sub-classes normally don't have to overwrite this method but
	 * create customized behavior by overriding one of the life-cycle methods like
	 * {@link #init()}, {@link #beforeRun()}, {@link #performRun()} (this one is
	 * required), {@link #done()}, {@link #produceResults()} or {@link #finish()}.
	 * 
	 * @return The results of experiment execution.
	 */
	protected Map<String, Object> runExperimentInternal() {
		synchronized (state) {
			// checking and setting as an atomic operation
			requireState(ExperimentState.ABOUT_TO_START);
			state.set(ExperimentState.RUNNING);
		}

		try {
			try {
				runTimeReal = System.currentTimeMillis();
				aborted = 0;
				resultMap = new LinkedHashMap<>();
				isCancelled = false;
				error = null;

				starting();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_STARTING);

				init();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_INITIALIZED);

				beforeRun();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_BEFORE_RUN);

				performRun();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_RUN_PERFORMED);

				afterRun();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_AFTER_RUN);

				done();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_DONE);
			} finally {
				runTimeReal = System.currentTimeMillis() - runTimeReal;
				addStandardResults();
			}

			checkCancelledOrInterrupted();

			if (numListener() > 0)
				fire(ExperimentMessage.EXPERIMENT_COLLECTING_RESULTS);

			produceResults();

			if (numListener() > 0)
				fire(ExperimentMessage.EXPERIMENT_FINISHING);

			finish();

			if (numListener() > 0)
				fire(ExperimentMessage.EXPERIMENT_FINISHED);

			// return results
			return getResults();
		} catch (Throwable t) {
			try {
				handleExecutionError(t);
			} catch (Throwable ignore) {
				// TODO: use proper log message
				ignore.printStackTrace();
			}
			throw t;
		} finally {
			try {
				finalActions();
				if (numListener() > 0)
					fire(ExperimentMessage.EXPERIMENT_FINALLY);
			} catch (Throwable ignore) {
				// TODO: use proper log message
				ignore.printStackTrace();
			} finally {
				state.set(error == null ? ExperimentState.FINISHED : ExperimentState.ERROR);
			}
		}
	}

	/**
	 * Don't call this method directly. Used internally by
	 * {@link ExperimentCompletableFuture} and in {@link #runExperiment()}.
	 */
	final void aboutToStart() {
		synchronized (state) {
			// checking and setting as an atomic operation
			requireState(ExperimentState.INITIAL);
			state.set(ExperimentState.ABOUT_TO_START);
		}
	}

	/**
	 * Checks, if an experiment is in a certain state.
	 * 
	 * @param expected The expected state of an experiment.
	 * @throws IllegalStateException If not in the expected state.
	 */
	protected void requireState(ExperimentState expected) {
		ExperimentState current = state.get();
		if (current != expected) {
			throw new IllegalStateException(
					"State expected " + expected + ", but was " + current + ". An experiment can only run once.");
		}
	}

	/**
	 * Lifecycle method that is executed if there was an Exception during experiment
	 * execution. In addition an EXPERIMENT_ERROR event is fired.
	 * 
	 * @param t
	 */
	protected void handleExecutionError(Throwable t) {
		error = t;
		aborted = 1;
		addErrorResults();
		if (numListener() > 0)
			fire(ExperimentMessage.EXPERIMENT_ERROR);
	}

	/**
	 * This method can be used for actions like clean-up that should be done
	 * irrespectively of whether an exception occurred during experiment execution
	 * or not. Any code executed in this method should take care that it is not
	 * producing any uncatched exceptions itself.
	 */
	protected void finalActions() {
	}

	/**
	 * Checks whether the experiment was requested to cancel or the executing Thread
	 * was interrupted. If so, a {@link CancellationException} (unchecked exception)
	 * is thrown.
	 * <p>
	 * Experiments that wan't to be responsive to cancellation requests should call
	 * this method frequently from within the main execution Thread.
	 * 
	 * @throws CancellationException If the experiment was cancelled or interrupted.
	 */
	protected void checkCancelledOrInterrupted() throws CancellationException {
		if (isCancelled()) {
			throw new CancellationException("Execution cancelled.");
		}
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt(); // restore interrupt flag
			throw new CancellationException("Execution interrupted.");
		}
	}

	protected void addStandardResults() {
		resultMap.put(RUNTIME, runTimeReal());
		resultMap.put(EXP_ABORTED, aborted);
	}

	protected void addErrorResults() {
		resultMap.put(EXP_ABORTED, aborted);
		resultMap.put(Experiment.EXCEPTION_MESSAGE, error.getMessage());
		resultMap.put(Experiment.EXCEPTION, Util.exceptionToString(error));
	}

	/**
	 * Requests the experiment to cancel its execution prematurely. This also
	 * implies aborting it.
	 * 
	 * @see #abort()
	 * @see #checkCancelledOrInterrupted()
	 */
	public void cancel() {
		abort();
		this.isCancelled = true;
	}

	/**
	 * Checks whether the experiment was requested to {@link #cancel()} its
	 * execution.
	 * 
	 * @return {@code true}, if {@link #cancel()} was called before; {@code false}
	 *         otherwise.
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * Marks experiment execution to be aborted by some error condition. This does
	 * not necessarily mean it's execution was {@link #cancel()}led prematurely.
	 */
	public void abort() {
		this.aborted = 1;
	}

	/**
	 * Returns the result map produced when executing this experiment.
	 * 
	 * @return This experiment's results as an unmodifiable map.
	 */
	public final Map<String, Object> getResults() {
		return Collections.unmodifiableMap(resultMap);
	}

	/**
	 * @return The exception that terminated this experiment. Might be a
	 *         {@link CancellationException} if this experiment's execution was
	 *         cancelled.
	 */
	public final Throwable getError() {
		return error;
	}

	/**
	 * @return The current execution state of this experiment as an
	 *         {@link ObservableValue}.
	 */
	public final ObservableValue<ExperimentState> state() {
		return state;
	}

	/**
	 * @return The current execution state of this experiment.
	 */
	public final ExperimentState getState() {
		return state.get();
	}

	/**
	 * Returns the run time (in seconds) of an Experiment. The returned value is
	 * only valid after calling {@link #runExperiment()} and measures the time
	 * between calling {@link #init()} and the completion of {@link #done()}.
	 * 
	 * @return The real time (wall time in seconds) it took to run the experiment.
	 */
	protected double runTimeReal() {
		return (runTimeReal / 1000.0d);
	}

	/**
	 * This is a convenience method to run a sub experiment without having to worry
	 * about {@code ExperimentExecutor} and {@code nestingLevel}.
	 * 
	 * @param sub The sub-experiment to run.
	 * @return An {@link ExperimentCompletableFuture} to access results.
	 */
	protected ExperimentCompletableFuture executeSubExperiment(Experiment sub) {
		sub.nestingLevel(nestingLevel() + 1);
		return ExperimentExecutor.runExperimentAsync(sub, this);
	}

	/**
	 * Retrieves a map containing the name and current value for each of this
	 * experiment's properties.
	 * 
	 * @return A map of all Java Bean properties and their values.
	 */
	public Map<String, Object> getPropsWithValues() {
		Map<String, Object> props = new LinkedHashMap<String, Object>();

		PropertyDescriptor[] pds = TypeUtil.findWritableProperties(this);
		for (PropertyDescriptor pd : pds) {
			try {
				props.put(pd.getName(), pd.getReadMethod().invoke(this));
			} catch (Exception e) {
				throw new RuntimeException(pd.getName(), e);
			}
		}

		return props;
	}

	/**
	 * Triggers a print event of category "info".
	 * 
	 * @param message The message to print.
	 * @see #print(MsgCategory, String)
	 */
	public void print(String message) {
		print(MsgCategory.INFO, message);
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener is
	 * installed, this should produce an output of {@code message}.
	 * 
	 * @param category Category of the message.
	 * @param message  The message to print.
	 * @see ConsolePrinter
	 */
	public void print(MsgCategory category, String message) {
		if (numListener() > 0 && category.ordinal() <= getLogLevel().ordinal())
			fire(new ExpPrintMessage(this, category, message));
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener is
	 * installed, this should produce a message created by the given format string
	 * and parameters.
	 * 
	 * @param category      Category of the message.
	 * @param messageFormat Format string for the message to produce.
	 * @param params        Parameters to use in the format string.
	 */
	public void print(MsgCategory category, String messageFormat, Object... params) {
		if (numListener() > 0 && category.ordinal() <= getLogLevel().ordinal())
			fire(new ExpPrintMessage(this, category, messageFormat, params));
	}

	/**
	 * Same as {@link #print(MsgCategory, String, Object...)}, just defaulting to
	 * the category {@code INFO}.
	 * 
	 * @param messageFormat The format String to use.
	 * @param params        Parameters to use when formatting the message.
	 */
	public void print(String messageFormat, Object... params) {
		print(MsgCategory.INFO, messageFormat, params);
	}

	/**
	 * Prints the results of this experiment to {@link System#out}.
	 */
	public final void printResults() {
		ConsolePrinter.printResults(this, getResults());
	}

	/**
	 * Sets the nesting level. This method is only for internal purposes.
	 * 
	 * @param nestingLevel The nesting level for this experiment.
	 */
	public void nestingLevel(int nestingLevel) {
		this.nestingLevel = nestingLevel;
	}

	/**
	 * The level in the call hierarchy this experiment is executed in. Experiments
	 * that spawn new sub-experiments (like {@link MultipleReplicationExperiment})
	 * are required to increase their children's nestingLevel by 1. If
	 * {@link #executeSubExperiment(Experiment)} is used, then this is set
	 * automatically to the correct value.
	 * 
	 * @return This experiment's nesting level.
	 */
	public int nestingLevel() {
		return nestingLevel;
	}

	/**
	 * Set some descriptive name for this experiment.
	 * 
	 * @param name The name of the experiment.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public long getInitialSeed() {
		return initialSeed;
	}

	/**
	 * Sets the initial seed for this experiment. If an experiment makes use of
	 * random influences, they should all and solely depend on this value.
	 * 
	 * @see RandomFactory
	 * 
	 * @param initialSeed The initial seed to use.
	 */
	public void setInitialSeed(long initialSeed) {
		this.initialSeed = initialSeed;
	}

	public MsgCategory getLogLevel() {
		return logLevel;
	}

	/**
	 * Set the maximum level of logging messages that are supposed to be printed
	 * (e.g. TRACE to produce a detailed log file). Default is INFO.
	 * 
	 * @param logLevel The maximum log level to display/forward to listeners.
	 */
	public void setLogLevel(MsgCategory logLevel) {
		this.logLevel = logLevel;
	}

	//
	//
	// ValueStore implementation
	//
	//

	@Override
	public ValueStore valueStoreImpl() {
		if (valueStore == null) {
			valueStore = new ValueStoreImpl();
		}
		return valueStore;
	}

	//
	//
	// event notification
	//
	//

	@Override
	public NotifierImpl<Experiment, ExperimentMessage> notifierImpl() {
		if (notifierAdapter == null) {
			notifierAdapter = new NotifierImpl<>(this);
		}
		return notifierAdapter;
	}

	//
	//
	// cloning
	//
	//

	@Override
	public Experiment clone() {
		try {
			Experiment c = (Experiment) super.clone();

			c.state = observable(ExperimentState.INITIAL);

			if (notifierAdapter != null) {
				c.notifierAdapter = new NotifierImpl<>(c);
				for (int i = 0; i < numListener(); i++) {
					c.addListener(TypeUtil.cloneIfPossible(getListener(i)));
				}
			}

			// clone value store copying (but not cloning!) all of its entries
			if (valueStore != null) {
				c.valueStore = valueStore.clone();
			}

			return c;
		} catch (CloneNotSupportedException shouldntHappen) {
			throw new RuntimeException(shouldntHappen);
		}
	}

	public String toString() {
		return getName() == null ? "exp@" + Integer.toHexString(hashCode()) : getName();
	}

	// ******************* static methods ************************

	public static void main(String... args) throws Exception {
		// create instance of the Experiment sub-class that was specified as
		// Java's main class
		Class<?> klazz = TypeUtil.getMainClass();

		Class<? extends Experiment> ec = klazz.asSubclass(Experiment.class);
		Experiment e = ec.newInstance();

		// parse command line arguments and run
		new ConsoleRunner(e).runWith(args);
	}

}