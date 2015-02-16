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

import jasima.core.statistics.SummaryStat;
import jasima.core.util.Pair;
import jasima.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Runs an arbitrary baseExperiment multiple times (determined by
 * maxReplications()). All numeric results of the base experiment are averaged
 * over the runs, other result types are returned as an array containing all
 * values over the runs.
 * <p />
 * 
 * Optionally the maximum number of experiments run can be determined by a
 * confidence interval (t-test). To use this feature you have to tell
 * addConfIntervalMeasure(String) which result(s) of the base experiment to use.
 * <p />
 * 
 * In case of dynamic runs the following procedure is followed:
 * <ol>
 * <li>getMinReplications() replications are performed
 * <li>no further runs are performed if the confidence interval is less than a
 * certain allowance value
 * <ol>
 * <li>width of the confidence interval is determined by setErrorProb(double),
 * default is 0.05
 * <li>allowance value is computed by the runMean * allowancePercentage()
 * (default 1%)
 * </ol>
 * <li>if there is another run (i.e., confidence interval too large), another
 * batch of getMinReplications() is performed, i.e., go back to step 1
 * </ol>
 * 
 * @see OCBAExperiment
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class MultipleReplicationExperiment extends AbstractMultiExperiment {

	private static final long serialVersionUID = -5122164015247766742L;

	private Experiment baseExperiment;

	private int minReplications = 1;
	private int maxReplications = 10;

	private String[] confIntervalMeasure = new String[0];
	private double errorProb = 0.05d;
	private double allowancePercentage = 0.01d;

	public MultipleReplicationExperiment() {
		super();

		setAbortUponBaseExperimentAbort(true);
		setCommonRandomNumbers(false);
	}

	public MultipleReplicationExperiment(Experiment e, int numReps) {
		this();
		setBaseExperiment(e);
		setMaxReplications(numReps);
	}

	@Override
	protected void createExperiments() {
		experiments.clear();

		int batchSize = isNumRunsDynamic() ? getMinReplications()
				: getMaxReplications();

		for (int i = 0; i < batchSize; i++) {
			Experiment e = getBaseExperiment().silentClone();
			configureRunExperiment(e);
			experiments.add(e);
		}
	}

	@Override
	protected boolean hasMoreTasks() {
		if (!isNumRunsDynamic())
			return false;

		if (numTasksExecuted >= getMaxReplications())
			return false;

		// check all measures in "confIntervalMeasure" to have the quality
		// measured by "errorProb" and "allowancePercentage".
		for (String name : confIntervalMeasure) {
			@SuppressWarnings("unchecked")
			Pair<Boolean, SummaryStat> data = (Pair<Boolean, SummaryStat>) detailedResultsNumeric
					.get(name);
			SummaryStat vs = data.b;
			if (vs == null)
				throw new RuntimeException(String.format(Util.DEF_LOCALE,
						"No results for name '%s'.", name));

			double allowance = Math.abs(vs.mean() * allowancePercentage);

			double interv = 2 * vs.confIntRangeSingle(getErrorProb());

			if (interv > allowance)
				return true;
		}

		return false;
	}

	@Override
	protected final String prefix() {
		return "rep";
	}

	@Override
	public int getNumExperiments() {
		if (isNumRunsDynamic())
			return getMinReplications();
		else
			return getMaxReplications();
	}

	private boolean isNumRunsDynamic() {
		return confIntervalMeasure.length > 0;
	}

	public int getMinReplications() {
		return minReplications;
	}

	/**
	 * Sets the minimum number of replications to perform if the total number of
	 * replications is dynamic (i.e., if at least 1 result name is given in
	 * {@code confIntervalMeasure}).
	 * <p />
	 * If the number of runs is not dynamic, this setting has no effect.
	 */
	public void setMinReplications(int minReplications) {
		if (minReplications <= 0)
			throw new IllegalArgumentException("" + minReplications);
		this.minReplications = minReplications;
	}

	public int getMaxReplications() {
		return maxReplications;
	}

	/**
	 * Sets the maximum number of replications to perform. If the number of runs
	 * is not dynamic, this sets the total number of replications to perform. If
	 * the total number of replications is dynamic (i.e., if at least 1 result
	 * name is given in {@code confIntervalMeasure}), this sets the maximum
	 * number of replications to perform.
	 */
	public void setMaxReplications(int maxReplications) {
		if (maxReplications <= 0 || maxReplications < getMinReplications())
			throw new IllegalArgumentException("" + maxReplications);
		this.maxReplications = maxReplications;
	}

	public double getErrorProb() {
		return errorProb;
	}

	/**
	 * Sets the error probability used when computing the width of the
	 * confidence interval of {@code confIntervalMeasure}s. The closer this
	 * setting is to 0, the more replications will be performed (with less
	 * uncertain results).
	 * <p />
	 * This setting only has an effect if the total number of runs is dynamic.
	 * Its default value is 0.05.
	 */
	public void setErrorProb(double errorProb) {
		if (errorProb <= 0.0 || errorProb >= 1.0)
			throw new IllegalArgumentException(String.format(Util.DEF_LOCALE,
					"errorProb should be in the interval (0,1). invalid: %f",
					errorProb));
		this.errorProb = errorProb;
	}

	public double getAllowancePercentage() {
		return allowancePercentage;
	}

	/**
	 * Sets the desired target quality of results as a percentage of the mean
	 * across all replications performed so far. Its default value is 0.01,
	 * i.e., 1%. This setting has no effect if the total number of runs is
	 * static.
	 * <p />
	 * Let's say after 5 replications the observed mean of a result in
	 * {@code confIntervalMeasure} is 987.6 with some standard deviation (SD).
	 * Say this SD together with a certain {@code errorProb} leads to a
	 * confidence interval of &plusmn;10.2. No further replications are
	 * performed, if the total width of this interval (2*10.2=20.4) is smaller
	 * than the observed mean multiplied by {@code allowancePercentage}
	 * (987.6*0.01=9.876). In the example, the result is not yet precise enough,
	 * as the observed uncertainty is not smaller than the target value (20.4
	 * &gt; 9.876). Therefore further replications would be performed to further
	 * reduce the uncertainty of results.
	 */
	public void setAllowancePercentage(double allowancePercentage) {
		if (allowancePercentage <= 0.0 || allowancePercentage >= 1.0)
			throw new IllegalArgumentException(
					String.format(
							Util.DEF_LOCALE,
							"allowancePercentage should be in the interval (0,1). invalid: %f",
							allowancePercentage));
		this.allowancePercentage = allowancePercentage;
	}

	public void addConfIntervalMeasure(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(confIntervalMeasure));
		list.add(name);
		// convert back to array
		confIntervalMeasure = list.toArray(new String[list.size()]);
	}

	public boolean removeConfIntervalMeasure(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(confIntervalMeasure));
		boolean res = list.remove(name);
		// convert back to array
		confIntervalMeasure = list.toArray(new String[list.size()]);

		return res;
	}

	public String[] getConfIntervalMeasure() {
		return confIntervalMeasure;
	}

	/**
	 * Sets the list of results that will be used when the total number of
	 * replications to perform is dynamic. If any result name is given here as a
	 * {@code confIntervalMeasure}, then at least {@code minReplication}
	 * replications are performed. After this, additional replications are
	 * performed until all {@code confIntervalMeasure}s are precise enough or a
	 * total of {@code maxReplications} was performed. The meaning of
	 * "precise enough" is determined by the settings
	 * {@code allowancePercentage} and {@code errorProb}.
	 */
	public void setConfIntervalMeasure(String[] confIntervalMeasure) {
		this.confIntervalMeasure = confIntervalMeasure;
	}

	public Experiment getBaseExperiment() {
		return baseExperiment;
	}

	/**
	 * Sets the base experiment that is executed multiple times.
	 */
	public void setBaseExperiment(Experiment baseExperiment) {
		this.baseExperiment = baseExperiment;
	}

	@Override
	public MultipleReplicationExperiment clone()
			throws CloneNotSupportedException {
		MultipleReplicationExperiment mre = (MultipleReplicationExperiment) super
				.clone();

		if (confIntervalMeasure != null)
			mre.confIntervalMeasure = confIntervalMeasure.clone();

		if (baseExperiment != null)
			mre.baseExperiment = baseExperiment.clone();

		return mre;
	}

}
