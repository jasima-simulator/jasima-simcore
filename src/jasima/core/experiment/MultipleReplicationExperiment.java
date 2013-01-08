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

import jasima.core.statistics.SummaryStat;

import java.util.ArrayList;

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

	private ArrayList<String> confIntervalMeasure = new ArrayList<String>();
	private double errorProb = 0.05d;
	private double allowancePercentage = 0.01d;

	public MultipleReplicationExperiment() {
		super();

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

		// check all measures in "confIntervalMeasure" to have the quality
		// measured by "errorProb" and "allowancePercentage".
		for (String name : confIntervalMeasure) {
			SummaryStat vs = (SummaryStat) detailedResults.get(name);
			if (vs == null)
				throw new RuntimeException("No results for name '" + name + "'");

			double allowance = Math.abs(vs.mean() * allowancePercentage);
			// allowance = Math.max(allowance, 1.0d);

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
		return confIntervalMeasure.size() > 0;
	}

	public int getMinReplications() {
		return minReplications;
	}

	public void setMinReplications(int minReplications) {
		if (minReplications <= 0)
			throw new IllegalArgumentException("" + minReplications);
		this.minReplications = minReplications;
	}

	public int getMaxReplications() {
		return maxReplications;
	}

	public void setMaxReplications(int maxReplications) {
		if (maxReplications <= 0 || maxReplications < getMinReplications())
			throw new IllegalArgumentException("" + maxReplications);
		this.maxReplications = maxReplications;
	}

	public double getErrorProb() {
		return errorProb;
	}

	public void setErrorProb(double errorProb) {
		if (errorProb < 0.0 || errorProb >= 1.0)
			throw new IllegalArgumentException("" + errorProb);
		this.errorProb = errorProb;
	}

	public double getAllowancePercentage() {
		return allowancePercentage;
	}

	public void setAllowancePercentage(double allowancePercentage) {
		if (allowancePercentage < 0.0 || allowancePercentage >= 1.0)
			throw new IllegalArgumentException("" + allowancePercentage);
		this.allowancePercentage = allowancePercentage;
	}

	public void addConfIntervalMeasure(String name) {
		if (confIntervalMeasure.contains(name))
			throw new IllegalArgumentException(name);
		confIntervalMeasure.add(name);
	}

	public void removeConfIntervalMeasure(String name) {
		if (!confIntervalMeasure.contains(name))
			throw new IllegalArgumentException(name);
		confIntervalMeasure.remove(name);
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

	@SuppressWarnings("unchecked")
	@Override
	public MultipleReplicationExperiment clone()
			throws CloneNotSupportedException {
		MultipleReplicationExperiment mre = (MultipleReplicationExperiment) super
				.clone();

		if (confIntervalMeasure != null)
			mre.confIntervalMeasure = (ArrayList<String>) confIntervalMeasure
					.clone();

		if (baseExperiment != null)
			mre.baseExperiment = baseExperiment.clone();

		return mre;
	}

}
