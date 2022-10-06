/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.experiment;

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.analysis.solvers.RiddersSolver;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import jasima.core.statistics.SummaryStat;
import jasima.core.util.MsgCategory;
import jasima.core.util.Pair;

/**
 * <p>
 * Runs an arbitrary {@code baseExperiment} multiple times (determined by
 * {@code maxReplications}). All numeric results of the base experiment are
 * averaged over the runs, other result types are returned as an array
 * containing all values over the runs.
 * </p>
 * <p>
 * Optionally the maximum number of experiments run can be determined by a
 * confidence interval (t-test). To use this feature you have to tell
 * {@link #addConfIntervalMeasure(String)} which result(s) of the base
 * experiment to use.
 * </p>
 * <p>
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
 * @author Torsten Hildebrandt
 */
public class MultipleReplicationExperiment extends AbstractMultiExperiment {

	private static final long serialVersionUID = -5122164015247766742L;

	private Experiment baseExperiment;

	private int minReplications = 0;
	private int maxReplications = 10;

	private String[] confIntervalMeasures = {};
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

		int batchSize = getNumExperiments();

		for (int i = 0; i < batchSize; i++) {
			Experiment e = getBaseExperiment().clone();
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
		for (String name : confIntervalMeasures) {
			@SuppressWarnings("unchecked")
			Pair<Boolean, SummaryStat> data = (Pair<Boolean, SummaryStat>) detailedResultsNumeric.get(name);
			SummaryStat vs = data == null ? null : data.b;
			if (vs == null)
				throw new RuntimeException(defFormat("No results for name '%s'.", name));

			double allowance = Math.abs(vs.mean() * allowancePercentage);
			double interv = vs.confIntRangeSingle(getErrorProb());

			print(MsgCategory.INFO,
					"dynamic number of replications\tobjective: '%s'\tcurrent mean: %f\tconfInt: ±%f\ttarget: ±%f\testimated total replications: %d",
					name, vs.mean(), interv, allowance, estimateNumReps(vs, allowance));

			if (!(interv <= allowance))
				return true;
		}

		return false;
	}

	/**
	 * Numerically find the number of replications required to achieve the desired
	 * precision.
	 */
	private int estimateNumReps(SummaryStat vs, double allowance) {
		if (vs.weightSum() < 2)
			return -1;

		try {
			double c1 = errorProb * 0.5d;

			double numReps = new RiddersSolver(0.5).solve( //
					100, // max iterations
					(v) -> {
						TDistribution dist = new TDistribution(v - 1.0);
						return Math.abs(dist.inverseCumulativeProbability(c1)) * Math.sqrt(vs.variance() / v)
								- allowance;
					}, // the function to be solved for ==zero; see
						// SummaryStat.convIntRangeSingle()
					2, // min value
					1000000, // max value
					vs.weightSum() // initial value
			);

			return (int) Math.round(numReps);
		} catch (TooManyEvaluationsException | NoBracketingException ignore) {
			// ignore if numerical root finding should fail
			return -1;
		}
	}

	@Override
	protected final String prefix() {
		return "rep";
	}

	@Override
	public int getNumExperiments() {
		if (isNumRunsDynamic()) {
			int reps = getMinReplications();
			if (reps <= 0) {
				reps = Runtime.getRuntime().availableProcessors();
			}
			if (reps > getMaxReplications())
				reps = getMaxReplications();
			return reps;
		} else
			return getMaxReplications();
	}

	private boolean isNumRunsDynamic() {
		return confIntervalMeasures.length > 0;
	}

	public int getMinReplications() {
		return minReplications;
	}

	/**
	 * Sets the minimum number of replications to perform if the total number of
	 * replications is dynamic (i.e., if at least 1 result name is given in
	 * {@code confIntervalMeasure}).
	 * <p>
	 * If the number of runs is not dynamic, this setting has no effect. A value of
	 * 0 uses the number of available cpu cores.
	 * 
	 * @param minReplications Minimum number of replications.
	 */
	public void setMinReplications(int minReplications) {
		if (minReplications < 0)
			throw new IllegalArgumentException("" + minReplications);
		this.minReplications = minReplications;
	}

	public int getMaxReplications() {
		return maxReplications;
	}

	/**
	 * Sets the maximum number of replications to perform. If the number of runs is
	 * not dynamic, this sets the total number of replications to perform. If the
	 * total number of replications is dynamic (i.e., if at least 1 result name is
	 * given in {@code confIntervalMeasures}), this sets the maximum number of
	 * replications to perform.
	 * 
	 * @param maxReplications The number of replications to perform.
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
	 * <p>
	 * Sets the error probability used when computing the width of the confidence
	 * interval of {@code confIntervalMeasures}. The closer this setting is to 0,
	 * the more replications will be performed (with less uncertain results).
	 * </p>
	 * <p>
	 * This setting only has an effect if the total number of runs is dynamic. Its
	 * default value is 0.05.
	 * </p>
	 * 
	 * @param errorProb Desired maximum error probability for computing the
	 *                  confidence intervals.
	 */
	public void setErrorProb(double errorProb) {
		if (errorProb <= 0.0 || errorProb >= 1.0)
			throw new IllegalArgumentException(
					defFormat("errorProb should be in the interval (0,1). invalid: %f", errorProb));
		this.errorProb = errorProb;
	}

	public double getAllowancePercentage() {
		return allowancePercentage;
	}

	/**
	 * Sets the desired target quality of results as a percentage of the mean across
	 * all replications performed so far. Its default value is 0.01, i.e., 1%. This
	 * setting has no effect if the total number of runs is static.
	 * <p>
	 * Let's say after 5 replications the observed mean of a result in
	 * {@code confIntervalMeasure} is 987.6 with some standard deviation (SD). Say
	 * this SD together with a certain {@code errorProb} leads to a confidence
	 * interval of &plusmn;10.2. No further replications are performed, if the total
	 * width of this interval (2*10.2=20.4) is smaller than the observed mean
	 * multiplied by {@code allowancePercentage} (987.6*0.01=9.876). In the example,
	 * the result is not yet precise enough, as the observed uncertainty is not
	 * smaller than the target value (20.4 &gt; 9.876). Therefore further
	 * replications would be performed to further reduce the uncertainty of results.
	 * 
	 * @param allowancePercentage The desired maximum result uncertainty as a
	 *                            percentage of the mean value.
	 */
	public void setAllowancePercentage(double allowancePercentage) {
		if (allowancePercentage <= 0.0 || allowancePercentage >= 1.0)
			throw new IllegalArgumentException(
					defFormat("allowancePercentage should be in the interval (0,1). invalid: %f", allowancePercentage));
		this.allowancePercentage = allowancePercentage;
	}

	public void addConfIntervalMeasure(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(confIntervalMeasures));
		list.add(name);
		// convert back to array
		confIntervalMeasures = list.toArray(new String[list.size()]);
	}

	public boolean removeConfIntervalMeasure(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(confIntervalMeasures));
		boolean res = list.remove(name);
		// convert back to array
		confIntervalMeasures = list.toArray(new String[list.size()]);

		return res;
	}

	public String[] getConfIntervalMeasures() {
		return confIntervalMeasures;
	}

	/**
	 * Sets the list of results that will be used when the total number of
	 * replications to perform is dynamic. If any result name is given here as a
	 * {@code confIntervalMeasure}, then at least {@code minReplication}
	 * replications are performed. After this, additional replications are performed
	 * until all {@code confIntervalMeasure}s are precise enough or a total of
	 * {@code maxReplications} was performed. The meaning of "precise enough" is
	 * determined by the settings {@code allowancePercentage} and {@code errorProb}.
	 * 
	 * @param confIntervalMeasures A list of all result names that should be checked
	 *                             when the number of runs is dynamic.
	 */
	public void setConfIntervalMeasures(String... confIntervalMeasures) {
		this.confIntervalMeasures = confIntervalMeasures;
	}

	public Experiment getBaseExperiment() {
		return baseExperiment;
	}

	/**
	 * Sets the base experiment that is executed multiple times in various
	 * configurations. Before experiment execution, a copy (clone) of
	 * {@code baseExperiment} is created and run. Therefore the specific experiment
	 * instance passed as the {@code baseExperiment} is never actually executed.
	 * 
	 * @param baseExperiment The base experiment to use.
	 */
	public void setBaseExperiment(Experiment baseExperiment) {
		this.baseExperiment = baseExperiment;
	}

	@Override
	public MultipleReplicationExperiment clone() {
		MultipleReplicationExperiment mre = (MultipleReplicationExperiment) super.clone();

		if (confIntervalMeasures != null)
			mre.confIntervalMeasures = confIntervalMeasures.clone();

		if (baseExperiment != null)
			mre.baseExperiment = baseExperiment.clone();

		return mre;
	}

}
