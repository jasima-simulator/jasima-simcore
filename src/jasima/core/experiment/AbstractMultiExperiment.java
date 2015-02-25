/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
 *******************************************************************************/
package jasima.core.experiment;

import jasima.core.expExecution.ExperimentExecutor;
import jasima.core.expExecution.ExperimentFuture;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Parent class of an experiment which runs a number of child experiments.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public abstract class AbstractMultiExperiment extends Experiment {

	private static final long serialVersionUID = 2285355204835181022L;

	public static final String NUM_TASKS_EXECUTED = "numTasks";

	/**
	 * Complex event object triggered upon sub-experiment completion.
	 */
	public static class BaseExperimentCompleted extends ExperimentEvent {

		public BaseExperimentCompleted(Experiment experimentRun,
				Map<String, Object> results) {
			super();
			this.experimentRun = experimentRun;
			this.results = results;
		}

		public final Experiment experimentRun;
		public final Map<String, Object> results;
	}

	// parameters

	private boolean allowParallelExecution = true;
	private boolean commonRandomNumbers = true;
	private int skipSeedCount = 0;
	private boolean abortUponBaseExperimentAbort = false;
	private String[] keepResults = new String[0];
	private boolean produceAveragedResults = true;

	// fields used during run

	protected UniqueNamesCheckingHashMap detailedResultsNumeric;
	protected UniqueNamesCheckingHashMap detailedResultsOther;
	protected Random seedStream;
	protected List<Experiment> experiments;
	protected int numTasksExecuted;

	@Override
	public void init() {
		super.init();

		experiments = new ArrayList<Experiment>();
		seedStream = null;

		detailedResultsNumeric = new UniqueNamesCheckingHashMap();
		detailedResultsOther = new UniqueNamesCheckingHashMap();
		numTasksExecuted = 0;

		for (int i = 0; i < getSkipSeedCount(); i++) {
			// throw away seed
			getExperimentSeed();
		}
	}

	@Override
	protected void performRun() {
		do {
			createExperiments();
			executeExperiments();
		} while (hasMoreTasks());
		experiments.clear();
	}

	protected boolean hasMoreTasks() {
		return false;
	}

	protected abstract void createExperiments();

	protected void executeExperiments() {
		try {
			if (isAllowParallelExecution()) {
				// start execution and store process results in the same order
				// as they are stored in tasks
				int n = 0;
				Collection<ExperimentFuture> allFutures = ExperimentExecutor
						.getExecutor().runAllExperiments(experiments, this);
				Iterator<ExperimentFuture> it = allFutures.iterator();
				while (it.hasNext()) {
					ExperimentFuture f = it.next();
					it.remove();

					getAndStoreResults(experiments.get(n), f);
					experiments.set(n, null);
					n++;

					// check if to abort this experiment, if so cancel all
					// future tasks
					if (aborted != 0) {
						for (ExperimentFuture f2 : allFutures) {
							f2.cancel(true);
						}
						break; // while
					}
				}
			} else {
				// sequential execution
				ExperimentExecutor ex = ExperimentExecutor.getExecutor();
				for (int i = 0; i < experiments.size(); i++) {
					Experiment e = experiments.get(i);
					experiments.set(i, null);

					if (aborted == 0) {
						ExperimentFuture future = ex.runExperiment(e, this);
						getAndStoreResults(e, future);
					} else {
						break; // for i
					}
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void getAndStoreResults(Experiment e, ExperimentFuture f)
			throws InterruptedException {
		Map<String, Object> res = f.get();

		numTasksExecuted++;
		storeRunResults(e, res);
		if (numListener() > 0) {
			fire(new BaseExperimentCompleted(e, res));
		}
	}

	protected void configureRunExperiment(Experiment e) {
		long s = getExperimentSeed();
		e.setInitialSeed(s);
		e.nestingLevel(nestingLevel() + 1);

		String name = prefix() + padNumTasks(experiments.size() + 1);
		if (e.getName() != null)
			name = name + "." + e.getName();
		e.setName(name);
	}

	protected long getExperimentSeed() {
		if (isCommonRandomNumbers())
			return getInitialSeed();
		else {
			if (seedStream == null)
				seedStream = new Random(getInitialSeed());
			return seedStream.nextLong();
		}
	}

	protected void storeRunResults(Experiment e, Map<String, Object> r) {
		Integer aborted = (Integer) r.get(Experiment.EXP_ABORTED);
		if (aborted != null) {
			if (aborted.intValue() > 0 && isAbortUponBaseExperimentAbort()) {
				abort();
			}
		}

		for (String key : r.keySet()) {
			Object val = r.get(key);

			if (shouldKeepDetails(key))
				detailedResultsOther.put(key + "." + prefix()
						+ padNumTasks(getNumTasksExecuted()), r.get(key));

			if (isProduceAveragedResults()) {
				if ((val != null)
						&& ((val instanceof SummaryStat) || ((val instanceof Number))))
					handleNumericValue(key, val);
				else
					handleOtherValue(key, val);
			}
		}
	}

	private boolean shouldKeepDetails(String key) {
		for (String s : keepResults) {
			if (s.equals(key) || key.startsWith(s + '.'))
				return true;
		}
		return false;
	}

	private String padNumTasks(int v) {
		int l = String.valueOf(getNumExperiments()).length();

		StringBuilder sb = new StringBuilder(l);
		sb.append(v);
		while (sb.length() < l)
			sb.insert(0, '0');
		return sb.toString();
	}

	public abstract int getNumExperiments();

	public int getNumTasks() {
		return experiments.size();
	}

	public int getNumTasksExecuted() {
		return numTasksExecuted;
	}

	protected abstract String prefix();

	public void abort() {
		this.aborted++;
	}

	/**
	 * Handles arbitrary values "val" by storing them in an object array.
	 */
	protected void handleOtherValue(String key, Object val) {
		if (key.endsWith(EXCEPTION) || key.endsWith(EXCEPTION_MESSAGE)) {
			key = "baseExperiment." + key;
		}

		@SuppressWarnings("unchecked")
		ArrayList<Object> l = (ArrayList<Object>) detailedResultsOther.get(key);
		if (l == null) {
			l = new ArrayList<Object>();
			detailedResultsOther.put(key, l);
		}
		l.add(val);
	}

	/**
	 * Handles a numeric value "val" by averaging it over all runs performed. If
	 * "val" is of type SummaryStat, averaging is performed with its
	 * mean()-value.
	 */
	protected void handleNumericValue(String key, Object val) {
		Double v;
		boolean wasSummaryStat;

		// store run result, which can be a complex statistic or scalar value
		if (val instanceof SummaryStat) {
			SummaryStat vs = (SummaryStat) val;
			wasSummaryStat = true;
			if (vs.numObs() > 0)
				v = vs.mean();
			else
				v = null;
		} else if (val instanceof Number) {
			Number n = (Number) val;
			v = n.doubleValue();
			wasSummaryStat = false;
		} else
			// should never occur
			throw new AssertionError("Illegal experiment result type: "
					+ val.getClass().getName());

		// get/create entry in "detailedResultsNumeric"
		@SuppressWarnings("unchecked")
		Pair<Boolean, SummaryStat> data = (Pair<Boolean, SummaryStat>) detailedResultsNumeric
				.get(key);
		if (data == null) {
			data = new Pair<Boolean, SummaryStat>(wasSummaryStat,
					new SummaryStat());
			detailedResultsNumeric.put(key, data);
		}
		SummaryStat repValues = data.b;

		// store value
		if (v != null) {
			repValues.value(v.doubleValue());
		}
	}

	protected boolean isSpecialKey(String key) {
		return key.endsWith(RUNTIME) || key.endsWith(NUM_TASKS_EXECUTED)
				|| key.endsWith(EXP_ABORTED);
	}

	@Override
	protected void produceResults() {
		super.produceResults();

		for (String key : detailedResultsNumeric.keySet()) {
			@SuppressWarnings("unchecked")
			Pair<Boolean, SummaryStat> data = (Pair<Boolean, SummaryStat>) detailedResultsNumeric
					.get(key);
			SummaryStat val = data.b;

			if (isSpecialKey(key)) {
				key = "baseExperiment." + key;
			} else {
				// was base result already a SummaryStat?
				if (data.a == true) {
					key = key + ".mean";
				}
			}

			// careful: SummaryStat is not immutable, so it might be better to
			// create a clone?
			resultMap.put(key, val);
		}

		for (String key : detailedResultsOther.keySet()) {
			Object val = detailedResultsOther.get(key);

			while (resultMap.containsKey(key))
				key += "@Other";

			if (val instanceof ArrayList) {
				ArrayList<?> l = (ArrayList<?>) val;
				val = l.toArray(new Object[l.size()]);
			}
			resultMap.put(key, val);
		}

		resultMap.put(NUM_TASKS_EXECUTED, getNumTasksExecuted());
	}

	@Override
	public AbstractMultiExperiment clone() throws CloneNotSupportedException {
		AbstractMultiExperiment mre = (AbstractMultiExperiment) super.clone();

		if (keepResults != null)
			mre.keepResults = keepResults.clone();

		return mre;
	}

	public void addKeepResultName(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(keepResults));
		list.add(name);
		// convert back to array
		keepResults = list.toArray(new String[list.size()]);
	}

	public boolean removeKeepResultName(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(keepResults));
		boolean res = list.remove(name);
		// convert back to array
		keepResults = list.toArray(new String[list.size()]);

		return res;
	}

	public boolean isKeepTaskResults() {
		return keepResults.length > 0;
	}

	public String[] getKeepResults() {
		return keepResults;
	}

	/**
	 * Sets the names of all results where detailed results of all
	 * sub-experiment executions should be preserved.
	 */
	public void setKeepResults(String[] keepResults) {
		this.keepResults = keepResults;
	}

	/**
	 * If this attribute is set to {@code true}, sub-experiments will be
	 * executed concurrently in parallel. Setting this property to {@code false}
	 * (therefore using only a single CPU core) is sometimes useful for
	 * debugging purposes or when fine-grained control over parallelization of
	 * nested (multi-)experiments is required.
	 */
	public void setAllowParallelExecution(boolean allowParallelExecution) {
		this.allowParallelExecution = allowParallelExecution;
	}

	public boolean isAllowParallelExecution() {
		return allowParallelExecution;
	}

	/**
	 * Whether to use the variance reduction technique of common random numbers.
	 * If set to true, all sub-experiments are executed using the same random
	 * seed, so random influences will be the same for all sub-experiments. If
	 * set to {@code false}, all sub-experiments will be assigned a different
	 * {@code initialSeed}, depending only on this experiment's
	 * {@code initialSeed}.
	 */
	protected void setCommonRandomNumbers(boolean commonRandomNumbers) {
		this.commonRandomNumbers = commonRandomNumbers;
	}

	public boolean isCommonRandomNumbers() {
		return commonRandomNumbers;
	}

	/**
	 * Before starting, throw away this many seed values. This setting can be
	 * useful to resume interrupted sub-experiments.
	 */
	public void setSkipSeedCount(int skipSeedCount) {
		this.skipSeedCount = skipSeedCount;
	}

	public int getSkipSeedCount() {
		return skipSeedCount;
	}

	/**
	 * If set to {@code true}, this experiment aborts immediately (indicating an
	 * abort in its results) after the first sub-experiment aborting. If this is
	 * set to {@code false}, execution of sub-experiments continues, ignoring
	 * aborting experiments.
	 */
	public void setAbortUponBaseExperimentAbort(
			boolean abortUponBaseExperimentAbort) {
		this.abortUponBaseExperimentAbort = abortUponBaseExperimentAbort;
	}

	public boolean isAbortUponBaseExperimentAbort() {
		return abortUponBaseExperimentAbort;
	}

	public boolean isProduceAveragedResults() {
		return produceAveragedResults;
	}

	/**
	 * Whether or not to produce averaged results across all sub-experiments as
	 * a result of this experiment.
	 */
	public void setProduceAveragedResults(boolean produceAveragedResults) {
		this.produceAveragedResults = produceAveragedResults;
	}

}
