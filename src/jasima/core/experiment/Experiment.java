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

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.expExecution.ExperimentFuture;
import jasima.core.experiment.Experiment.ExperimentMessage;
import jasima.core.random.RandomFactory;
import jasima.core.run.ConsoleRunner;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.SilentCloneable;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierImpl;

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
		implements Notifier<Experiment, ExperimentMessage>, SilentCloneable<Experiment>, Serializable {

	/**
	 * Just an arbitrary default seed.
	 */
	public static final long DEFAULT_SEED = 0xd23284FEA3L;

	private static final long serialVersionUID = -5981694222402234985L;

	public static final String RUNTIME = "runTime";
	public static final String EXP_ABORTED = "expAborted";
	public static final String EXCEPTION = "exception";
	public static final String EXCEPTION_MESSAGE = "exceptionMessage";

	/**
	 * Simple base class for messages used by the notification mechanism.
	 */
	public static class ExperimentMessage {
		public final String s;

		public ExperimentMessage(String s) {
			super();
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}

		public static final ExperimentMessage EXPERIMENT_STARTING = new ExperimentMessage("EXPERIMENT_STARTING");
		public static final ExperimentMessage EXPERIMENT_INITIALIZED = new ExperimentMessage("EXPERIMENT_INITIALIZED");
		public static final ExperimentMessage EXPERIMENT_BEFORE_RUN = new ExperimentMessage("EXPERIMENT_BEFORE_RUN");
		public static final ExperimentMessage EXPERIMENT_AFTER_RUN = new ExperimentMessage("EXPERIMENT_AFTER_RUN");
		public static final ExperimentMessage EXPERIMENT_DONE = new ExperimentMessage("EXPERIMENT_DONE");
		public static final ExperimentMessage EXPERIMENT_COLLECT_RESULTS = new ExperimentMessage(
				"EXPERIMENT_COLLECT_RESULTS");
		public static final ExperimentMessage EXPERIMENT_FINISHING = new ExperimentMessage("EXPERIMENT_FINISHING");
		public static final ExperimentMessage EXPERIMENT_FINISHED = new ExperimentMessage("EXPERIMENT_FINISHED");
	}

	/**
	 * Class to store print messages of an experiment.
	 */
	public static class ExpPrintEvent extends ExperimentMessage {

		public final Experiment exp;
		public final MsgCategory category;
		private String message;
		private String messageFormatString;
		private Object[] params;

		public ExpPrintEvent(Experiment exp, MsgCategory category, String message) {
			super("ExpPrintEvent");
			if (message == null)
				throw new NullPointerException();
			this.exp = exp;
			this.category = category;
			this.message = message;
		}

		public ExpPrintEvent(Experiment exp, MsgCategory category, String messageFormatString, Object... params) {
			super("ExpPrintEvent");
			this.exp = exp;
			this.category = category;
			this.messageFormatString = messageFormatString;
			this.params = params;
			this.message = null;
		}

		/**
		 * Returns this message formatted as a {@code String} using the default
		 * {@link Locale}.
		 * 
		 * @return The formatted message using the default {@code Locale}.
		 * @see Util#DEF_LOCALE
		 */
		public String getMessage() {
			return getMessage(Util.DEF_LOCALE);
		}

		/**
		 * Returns this message formatted using the given {@link Locale}.
		 * 
		 * @param locale
		 *            The {@link Locale} to use when formatting the message.
		 * @return The formatted message.
		 */
		public String getMessage(Locale locale) {
			// lazy creation of message only when needed
			if (message == null) {
				message = String.format(locale, messageFormatString, params);
				messageFormatString = null;
				params = null;
			}

			return message;
		}

		@Override
		public String toString() {
			return getMessage();
		}
	}

	// fields to store parameters
	private int nestingLevel = 0;
	private String name = null;
	private long initialSeed = DEFAULT_SEED;
	private MsgCategory logLevel = MsgCategory.INFO;
	private NotifierImpl<Experiment, ExperimentMessage> notifierAdapter;

	// fields used during run
	private long runTimeReal;
	protected int aborted;
	protected Map<String, Object> resultMap;

	public static class UniqueNamesCheckingHashMap extends LinkedHashMap<String, Object> {
		private static final long serialVersionUID = -6783419937586790463L;

		@Override
		public Object put(String key, Object value) {
			if (containsKey(key))
				throw new RuntimeException("Map already contains value '" + key + "'.");
			return super.put(key.intern(), value);
		}
	}

	public Experiment() {
		super();

		notifierAdapter = new NotifierImpl<>(this);
	}

	/**
	 * This method is called to perform any initializations required before the
	 * experiment is run.
	 */
	protected void init() {
		aborted = 0;
	}

	/**
	 * This method is called immediately before {@link #performRun()}, but after
	 * {@link #init()}.
	 */
	protected void beforeRun() {
	}

	/**
	 * Contains the code to actually do something useful. This is the only
	 * abstract method that sub-classes are required to implement.
	 */
	protected abstract void performRun();

	/**
	 * This method can be overridden to perform any required clean-up. It is
	 * executed immediately after {@link #performRun()}, but before
	 * {@link #produceResults()} and {@link #finish()}.
	 */
	protected void done() {
	}

	/**
	 * Populates the result map {@link #resultMap} with values produced during
	 * {@link #results} experiment execution. The implementation in Experiment
	 * adds the two results {@value #RUNTIME} and {@value EXP_ABORTED}.
	 */
	protected void produceResults() {
		resultMap.put(RUNTIME, runTimeReal());
		resultMap.put(EXP_ABORTED, aborted);
	}

	/**
	 * This method gives experiments and listeners a chance to view/modify
	 * results. It is called after {@link #produceResults()}.
	 */
	protected void finish() {
	}

	/**
	 * Runs the experiment. This is the main method to call to execute an
	 * experiment. Sub-classes normally don't have to overwrite this method but
	 * create customized behavior by overriding on of the methods like
	 * {@link #init()}, {@link #beforeRun()}, {@link #performRun()} (this one is
	 * required), {@link #done()}, {@link #produceResults()} or
	 * {@link #finish()}.
	 * 
	 * @return The results of experiment execution.
	 */
	public Map<String, Object> runExperiment() {
		try {
			runTimeReal = System.currentTimeMillis();

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
				fire(ExperimentMessage.EXPERIMENT_AFTER_RUN);

			done();

			if (numListener() > 0)
				fire(ExperimentMessage.EXPERIMENT_DONE);
		} finally {
			runTimeReal = System.currentTimeMillis() - runTimeReal;
		}

		// build result map
		resultMap = new UniqueNamesCheckingHashMap();

		produceResults();

		if (numListener() > 0)
			fire(ExperimentMessage.EXPERIMENT_COLLECT_RESULTS);

		// give experiments and listener a chance to view/modify results
		finish();

		if (numListener() > 0)
			fire(ExperimentMessage.EXPERIMENT_FINISHING);

		// we are done, don't change results any more
		resultMap = Collections.unmodifiableMap(resultMap);
		if (numListener() > 0)
			fire(ExperimentMessage.EXPERIMENT_FINISHED);

		// return results
		return getResults();
	}

	/**
	 * Returns the result map produced when executing this experiment.
	 * 
	 * @return This experiment's results.
	 */
	public final Map<String, Object> getResults() {
		return resultMap;
	}

	/**
	 * Returns the run time (in seconds) of an Experiment. The returned value is
	 * only valid after calling {@link #runExperiment()} and measures the time
	 * between calling {@link #init()} and the completion of {@link #done()}.
	 * 
	 * @return The real time (wall time in seconds) it took to run the
	 *         experiment.
	 */
	protected double runTimeReal() {
		return (runTimeReal / 1000.0d);
	}

	/**
	 * This is a convenience method to run a sub experiment without having to
	 * worry about {@code ExperimentExecutor} and {@code nestingLevel}.
	 * 
	 * @param sub
	 *            The sub-experiment to run.
	 * @return An {@link ExperimentFuture} to access results.
	 */
	protected ExperimentFuture executeSubExperiment(Experiment sub) {
		sub.nestingLevel(nestingLevel() + 1);
		return ExperimentExecutor.getExecutor().runExperiment(sub, this);
	}

	/**
	 * Retrieves a map containing the name and current value for each of this
	 * class's properties.
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
	 * @param message
	 *            The message to print.
	 * @see #print(ExpMsgCategory, String)
	 */
	public void print(String message) {
		print(MsgCategory.INFO, message);
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this should produce an output of {@code message}.
	 * 
	 * @param category
	 *            Category of the message.
	 * @param message
	 *            The message to print.
	 * @see ConsolePrinter
	 */
	public void print(MsgCategory category, String message) {
		if (numListener() > 0 && category.ordinal() <= getLogLevel().ordinal())
			fire(new ExpPrintEvent(this, category, message));
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this should produce a message created by the given format
	 * string and parameters.
	 * 
	 * @param category
	 *            Category of the message.
	 * @param messageFormat
	 *            Format string for the message to produce.
	 * @param params
	 *            Parameters to use in the format string.
	 */
	public void print(MsgCategory category, String messageFormat, Object... params) {
		if (numListener() > 0 && category.ordinal() <= getLogLevel().ordinal())
			fire(new ExpPrintEvent(this, category, messageFormat, params));
	}

	/**
	 * Same as {@link #print(ExpMsgCategory, String, Object...)}, just
	 * defaulting to the category {@code INFO}.
	 * 
	 * @param messageFormat
	 *            The format String to use.
	 * @param params
	 *            Parameters to use when formatting the message.
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

	// event notification

	@Override
	public Notifier<Experiment, ExperimentMessage> notifierImpl() {
		return notifierAdapter;
	}

	public String toString() {
		return getName() == null ? super.toString() : getName();
	}

	@Override
	public Experiment clone() throws CloneNotSupportedException {
		Experiment c = (Experiment) super.clone();

		c.notifierAdapter = new NotifierImpl<>(c);
		for (int i = 0; i < numListener(); i++) {
			c.addListener(TypeUtil.cloneIfPossible(getListener(i)));
		}

		return c;
	}

	/**
	 * Sets the nesting level. This method is only for internal purposes.
	 * 
	 * @param nestingLevel
	 *            The nesting level for this experiment.
	 */
	public void nestingLevel(int nestingLevel) {
		this.nestingLevel = nestingLevel;
	}

	/**
	 * The level in the call hierarchy this experiment is executed in.
	 * Experiments that spawn new sub-experiments (like
	 * {@link MultipleReplicationExperiment}) are required to increase their
	 * children's nestingLevel by 1. If
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
	 * @param name
	 *            The name of the experiment.
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
	 * @param initialSeed
	 *            The initial seed to use.
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
	 * @param logLevel
	 *            The maximum log level to display/forward to listeners.
	 */
	public void setLogLevel(MsgCategory logLevel) {
		this.logLevel = logLevel;
	}

	// ******************* static methods ************************

	public static void main(String[] args) throws Exception {
		// create instance of the Experiment sub-class that was specified as
		// Java's main class
		Class<?> klazz = TypeUtil.getMainClass();
		Experiment e = (Experiment) klazz.newInstance();

		// parse command line arguments and run
		new ConsoleRunner(e).parseArgs(args).run();
	}

}