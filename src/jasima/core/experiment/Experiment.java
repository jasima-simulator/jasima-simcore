/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 *******************************************************************************/
package jasima.core.experiment;

import jasima.core.experiment.Experiment.ExperimentEvent;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.Util;
import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierAdapter;
import jasima.core.util.observer.NotifierListener;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An Experiment is something that produces results depending on various
 * parameters. The usual lifecycle is to create an experiment, set parameters to
 * their proper values, execute the experiment by calling it's runExperiment()
 * method. After execution a set of results are available using the getResults()
 * methods.
 * <p>
 * Experiments are not supposed to be run more than once, e.g., to run multiple
 * replications of an experiment (see {@link MultipleReplicationExperiment}) you
 * have to create multiple instances. Therefore experiments should be cloneable.
 * <p>
 * This class is intended as the base class for Experiments doing something
 * useful. This class only has a single parameter "initialSeed" (see
 * getInitialSeed()/setInitialSeed()). This parameter is supposed to be used as
 * the starting value for all (pseudo) random number generation activities at
 * experiment runtime. This means two experiments having the same initialSeed
 * and all other experiment parameters being the same should behave
 * deterministically and produce exactly the same results.
 * <p>
 * The only result produced by this class is "runTime" (type Double), which
 * measures the real time required to execute an experiment.
 * <p>
 * Experiments can have listeners registered, which are informed of an
 * experiment's start and completion and can be used by subclasses to provide
 * additional events.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id: Experiment.java 73 2013-01-08 17:16:19Z THildebrandt@gmail.com$"
 */
public abstract class Experiment implements Cloneable, Serializable,
		Notifier<Experiment, ExperimentEvent> {

	private static final long serialVersionUID = -5981694222402234985L;

	public static final String RUNTIME = "runTime";
	public static final String EXP_ABORTED = "expAborted";
	public static final String EXCEPTION = "exception";
	public static final String EXCEPTION_MESSAGE = "exceptionMessage";

	/**
	 * Simple base class for events used by the notification mechanism.
	 */
	public static class ExperimentEvent {
	}

	public static final ExperimentEvent EXPERIMENT_STARTING = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_INITIALIZED = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_BEFORE_RUN = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_AFTER_RUN = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_DONE = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_COLLECT_RESULTS = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_FINISHING = new ExperimentEvent();
	public static final ExperimentEvent EXPERIMENT_FINISHED = new ExperimentEvent();

	public enum ExpMsgCategory {
		OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
	}

	public static class ExpPrintEvent extends ExperimentEvent {
		public ExpPrintEvent(Experiment exp, ExpMsgCategory category,
				String message) {
			super();
			this.exp = exp;
			this.category = category;
			this.message = message;
		}

		public final Experiment exp;
		public final ExpMsgCategory category;
		public final String message;
	}

	// parameters

	private int nestingLevel = 0;
	private String name = null;
	private long initialSeed = 0xd23284FEA3L; // just an arbitrary default seed

	// used during run

	private long runTimeReal;
	protected int aborted;
	protected Map<String, Object> resultMap;

	// used during event notification
	public Map<String, Object> results;

	public static class UniqueNamesCheckingHashMap extends
			LinkedHashMap<String, Object> {
		private static final long serialVersionUID = -6783419937586790463L;

		@Override
		public Object put(String key, Object value) {
			if (containsKey(key))
				throw new RuntimeException("Map already contains value '" + key
						+ "'.");
			return super.put(key.intern(), value);
		}
	}

	public Experiment() {
		super();
	}

	protected void init() {
		aborted = 0;
	}

	protected void beforeRun() {
	}

	protected abstract void performRun();

	protected void done() {
	}

	protected void finish() {
	}

	public Map<String, Object> runExperiment() {
		try {
			runTimeReal = System.currentTimeMillis();
			if (numListener() > 0)
				fire(EXPERIMENT_STARTING);
			init();
			if (numListener() > 0)
				fire(EXPERIMENT_INITIALIZED);
			beforeRun();
			if (numListener() > 0)
				fire(EXPERIMENT_BEFORE_RUN);
			performRun();
			if (numListener() > 0)
				fire(EXPERIMENT_AFTER_RUN);
			done();
			if (numListener() > 0)
				fire(EXPERIMENT_DONE);
		} finally {
			runTimeReal = System.currentTimeMillis() - runTimeReal;
		}

		// build result map
		resultMap = new UniqueNamesCheckingHashMap();
		produceResults();
		if (numListener() > 0) {
			results = resultMap;
			fire(EXPERIMENT_COLLECT_RESULTS);
			results = null;
		}

		// give experiments and listener a chance to view/modify results
		finish();
		if (numListener() > 0) {
			results = resultMap;
			fire(EXPERIMENT_FINISHING);
			results = null;
		}

		// we are done, don't change results any more
		resultMap = Collections.unmodifiableMap(resultMap);
		if (numListener() > 0)
			fire(EXPERIMENT_FINISHED);

		return getResults();
	}

	public final Map<String, Object> getResults() {
		return resultMap;
	}

	protected void produceResults() {
		resultMap.put(RUNTIME, (runTimeReal / 1000.0d));
		resultMap.put(EXP_ABORTED, aborted);
	}

	/**
	 * Retrieves a list containing the name and current value of this class's
	 * properties.
	 */
	public Map<String, Object> getPropsWithValues() {
		Map<String, Object> props = new LinkedHashMap<String, Object>();

		PropertyDescriptor[] pds = Util.findWritableProperties(this);
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
		print(ExpMsgCategory.INFO, message);
	}

	/**
	 * Triggers a print event of the given category. If an appropriate listener
	 * is installed, this should produce an output of {@code message}.
	 * 
	 * @param message
	 *            The message to print.
	 * @see ConsolePrinter
	 */
	public void print(ExpMsgCategory category, String message) {
		if (numListener() > 0) {
			fire(new ExpPrintEvent(this, category, message));
		}
	}

	/**
	 * Prints the results of this experiments to {@link System#out}.
	 * 
	 * @param res
	 */
	public final void printResults(Map<String, Object> res) {
		ConsolePrinter.printResults(this, res);
	}

	/**
	 * Prints the results of this experiments to {@link System#out}.
	 * 
	 * @param res
	 */
	public final void printResults() {
		ConsolePrinter.printResults(this, getResults());
	}

	public String toString() {
		return getName() == null ? super.toString() : getName();
	}

	@Override
	public Experiment clone() throws CloneNotSupportedException {
		Experiment c = (Experiment) super.clone();

		if (adapter != null)
			c.adapter = adapter.clone();

		return c;
	}

	/**
	 * This is the same as clone(), just without throwing the checked exception
	 * CloneNotSupportedException. If such an exception occurs, it is wrapped in
	 * a RuntimeException.
	 */
	public Experiment silentClone() {
		try {
			return clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	// /**
	// * Runs the experiment and returns results. This method implements the
	// * {@link Callable} interface.
	// */
	// @Override
	// public Map<String, Object> call() throws Exception {
	// runExperiment();
	// return getResults();
	// }

	/**
	 * Sets the nesting level. This method is only for internal purposes.
	 */
	public void nestingLevel(int nestingLevel) {
		this.nestingLevel = nestingLevel;
	}

	/**
	 * The level in the call hierarchy this experiment is executed in.
	 * Experiments that spawn new sub-experiments (like
	 * {@link MultipleReplicationExperiment}) are required to increase their
	 * children's nestingLevel by 1.
	 */
	public int nestingLevel() {
		return nestingLevel;
	}

	/**
	 * This method is used internally to decide how to execute an experiment. If
	 * this method returns true (default), it does not spawn child experiments
	 * to produce it's results.
	 */
	public boolean isLeafExperiment() {
		return true;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public long getInitialSeed() {
		return initialSeed;
	}

	public void setInitialSeed(long s) {
		initialSeed = s;
	}

	//
	//
	// event notification
	//
	//

	private NotifierAdapter<Experiment, ExperimentEvent> adapter = null;

	@Override
	public void addNotifierListener(
			NotifierListener<Experiment, ExperimentEvent> listener) {
		if (adapter == null)
			adapter = new NotifierAdapter<Experiment, ExperimentEvent>(this);
		adapter.addNotifierListener(listener);
	}

	@Override
	public NotifierListener<Experiment, ExperimentEvent> getNotifierListener(
			int index) {
		return adapter.getNotifierListener(index);
	}

	@Override
	public void removeNotifierListener(
			NotifierListener<Experiment, ExperimentEvent> listener) {
		adapter.removeNotifierListener(listener);
	}

	protected void fire(ExperimentEvent event) {
		if (adapter != null)
			adapter.fire(event);
	}

	@Override
	public int numListener() {
		return adapter == null ? 0 : adapter.numListener();
	}

}