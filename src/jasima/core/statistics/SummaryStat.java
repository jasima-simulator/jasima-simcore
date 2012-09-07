/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.core.statistics;

import java.io.Serializable;

import org.apache.commons.math3.distribution.TDistribution;

/**
 * Class to collect the most important statistics without having to store all
 * values encountered. It can return mean, standard deviation, variance, min,
 * max etc. in O(1) time. Values are passed by calling the
 * {@link #value(double)} method. Values can be weighted, just call {@link
 * #value(double, double)} instead.
 * <p />
 * In other simulation packages this is sometimes called "tally".
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 */
public class SummaryStat implements Serializable {

	private static final long serialVersionUID = 2887454928117526659L;

	private static final double MIN_WEIGHT = 1e-12d;
	protected static final double DEF_ERROR_PROB = 0.05;

	private String name;
	private double valSum, sumSquare, weightSum;
	private int numObs;
	private double max;
	private double min;
	protected double lastValue, lastWeight;

	public SummaryStat() {
		this((String) null);
	}

	/**
	 * Create a new SummaryStat-object initialized with the values of "vs".
	 */
	public SummaryStat(SummaryStat vs) {
		this(vs.name);
		valSum = vs.valSum;
		sumSquare = vs.sumSquare;
		weightSum = vs.weightSum;
		lastValue = vs.lastValue;
		lastWeight = vs.lastWeight;
		numObs = vs.numObs;
		max = vs.max;
		min = vs.min;
	}

	public SummaryStat(String name) {
		super();
		clear();
		setName(name);
	}

	public void value(double v) {
		value(v, 1.0d);
	}

	public void value(double v, double weight) {
		if (weight < 0.0d)
			throw new IllegalArgumentException("Weight can't be negative. "
					+ weight);

		if (weight < MIN_WEIGHT)
			return;

		lastValue = v;
		lastWeight = weight;

		numObs++;
		if (v < min)
			min = v;
		if (v > max)
			max = v;

		weightSum += weight;

		final double vw = v * weight;
		valSum += vw;
		sumSquare += v * vw;
	}

	public double mean() {
		if (numObs < 1)
			return Double.NaN;
		return valSum / weightSum;
	}

	public double stdDev() {
		return Math.sqrt(variance());
	}

	public double variance() {
		if (numObs < 2)
			return Double.NaN;
		return (weightSum * sumSquare - valSum * valSum)
				/ (weightSum * (weightSum - 1));
	}

	/** Returns the coefficient of variation. */
	public double varCoeff() {
		return stdDev() / mean();
	}

	public double sum() {
		if (numObs < 1)
			return Double.NaN;
		return valSum;
	}

	public int numObs() {
		return numObs;
	}

	public double min() {
		if (numObs < 1)
			return Double.NaN;
		return min;
	}

	public double max() {
		if (numObs < 1)
			return Double.NaN;
		return max;
	}

	/**
	 * Combines the data in "other" with another ValueStat-Object. The combined
	 * object behaves as if it had also seen the data of "other".
	 */
	public void combine(SummaryStat other) {
		valSum += other.valSum;
		sumSquare += other.sumSquare;
		weightSum += other.weightSum;
		numObs += other.numObs;
		if (other.max > max)
			max = other.max;
		if (other.min < min)
			min = other.min;
	}

	/**
	 * @return lower value of a confidence interval with a 0.95-confidence level
	 */
	public double confidenceIntervalLower() {
		return confidenceIntervalLower(DEF_ERROR_PROB);
	}

	public double confidenceIntervalUpper() {
		return confidenceIntervalUpper(DEF_ERROR_PROB);
	}

	public double confidenceIntervalLower(double errorProb) {
		return mean() - confIntRangeSingle(errorProb);
	}

	public double confidenceIntervalUpper(double errorProb) {
		return mean() + confIntRangeSingle(errorProb);
	}

	public double confIntRangeSingle(double errorProb) {
		if (numObs <= 2)
			return Double.NaN;

		double deg = weightSum() - 1.0d;
		TDistribution dist = new TDistribution(deg);
		return Math.abs(dist.inverseCumulativeProbability(errorProb * 0.5d))
				* Math.sqrt(variance() / weightSum());
	}

	public double weightSum() {
		return weightSum;
	}

	public double lastValue() {
		if (numObs == 0)
			return Double.NaN;
		return lastValue;
	}

	public double lastWeight() {
		if (numObs == 0)
			return Double.NaN;
		return lastWeight;
	}

	public void clear() {
		valSum = sumSquare = 0.0d;
		numObs = 0;
		weightSum = 0.0d;
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
