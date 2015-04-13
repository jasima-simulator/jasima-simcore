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
package jasima.core.statistics;

import java.io.Serializable;

import org.apache.commons.math3.distribution.TDistribution;

/**
 * Class to collect the most important statistics without having to store all
 * values encountered. It can return mean, standard deviation, variance, min,
 * max etc. in O(1) time. Values are passed by calling the
 * {@link #value(double)} method. Values can be weighted, just call
 * {@link #value(double, double)} instead.
 * <p>
 * In other simulation packages this is sometimes called "tally".
 * <p>
 * This implementation is based on: D. H. D. West (1979). Communications of the
 * ACM, 22, 9, 532-535: Updating Mean and Variance Estimates: An Improved Method
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class SummaryStat implements Serializable, Cloneable {

	private static final long serialVersionUID = 817115058373461360L;

	protected static final double DEF_ERROR_PROB = 0.05;

	private String name;
	private double meanEst, varEst;
	private double weightSum;
	private int numObs;
	private double max;
	private double min;
	protected double lastValue, lastWeight;

	public SummaryStat() {
		this((String) null);
	}

	public SummaryStat(String name) {
		super();
		clear();
		setName(name);
	}

	/**
	 * Create a new SummaryStat-object initialized with the values of "vs". Copy
	 * constructor.
	 */
	public SummaryStat(SummaryStat vs) {
		this(vs.name);
		meanEst = vs.meanEst;
		varEst = vs.varEst;
		weightSum = vs.weightSum;
		numObs = vs.numObs;
		max = vs.max;
		min = vs.min;
		lastValue = vs.lastValue;
		lastWeight = vs.lastWeight;
	}

	/**
	 * Resets this object.
	 */
	public void clear() {
		meanEst = 0.0;
		varEst = 0.0d;
		numObs = 0;
		weightSum = 0.0d;
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		lastValue = Double.NaN;
		lastWeight = Double.NaN;
	}

	/**
	 * Convenience method to add all values given as arguments with a weight of
	 * 1.
	 * 
	 * @param vs
	 *            The values to add.
	 * @return {@code this}, to allow easy chaining of calls.
	 */
	public SummaryStat values(double... vs) {
		for (double v : vs) {
			value(v);
		}

		return this;
	}

	/**
	 * Adds the given value with a weight of 1.
	 * 
	 * @param v
	 *            The value to add.
	 * @return {@code this}, to allow easy chaining of calls.
	 */
	public SummaryStat value(double v) {
		return value(v, 1.0d);
	}

	/**
	 * Adds a value with a given weight.
	 * 
	 * @param v
	 *            The value to add.
	 * @param weight
	 *            The weight to give to this value. Has to be positive.
	 * @return {@code this}, to allow easy chaining of calls.
	 * @throws IllegalArgumentException
	 *             If weight was negative.
	 */
	public SummaryStat value(double v, double weight)
			throws IllegalArgumentException {
		if (!(weight >= 0.0d))
			throw new IllegalArgumentException("Weight can't be negative. "
					+ weight);

		lastValue = v;
		lastWeight = weight;

		numObs++;

		if (v < min)
			min = v;
		if (v > max)
			max = v;

		double oldSum = weightSum;
		weightSum += weight;

		double q = v - meanEst;
		double r = weightSum == 0.0 ? 0.0 : q * weight / weightSum;

		meanEst += r;
		varEst += r * oldSum * q;

		return this;
	}

	/**
	 * Returns the mean of all values given to {@link #value(double)}.
	 * 
	 * @return The arithmetic mean of all values seen so far.
	 */
	public double mean() {
		if (numObs < 1)
			return Double.NaN;
		return meanEst;
	}

	/**
	 * The standard deviation of all values.
	 * 
	 * @return The standard deviation of all values given to
	 *         {@link #value(double)}.
	 */
	public double stdDev() {
		return Math.sqrt(variance());
	}

	/**
	 * Returns the sample variance of the values.
	 * 
	 * @return The (sample) variance of all values given to
	 *         {@link #value(double)}. Returns NaN, if no values were added yet.
	 */
	public double variance() {
		if (numObs < 1)
			return Double.NaN;
		if (numObs == 1)
			return 0.0;
		if (weightSum <= 1.0)
			throw new IllegalStateException("weight sum is <=1.0: " + weightSum);

		return varEst / (weightSum - 1.0);
	}

	/**
	 * Returns the population variance of the values.
	 * 
	 * @return The (sample) variance of all values given to
	 *         {@link #value(double)}. Returns NaN, if no values were added yet.
	 */
	public double variancePopulation() {
		if (numObs < 1)
			return Double.NaN;
		if (numObs == 1)
			return 0.0;

		return varEst / weightSum;
	}

	/**
	 * Returns the coefficient of variation ({@link #stdDev()} divided by
	 * {@link #mean()}).
	 * 
	 * @return The coefficient of variation.
	 * */
	public double varCoeff() {
		return stdDev() / mean();
	}

	/**
	 * Returns the sum of all {@link #value(double)}s (taking into account
	 * potential weights if {@link #value(double, double)} is used).
	 * 
	 * @return The sum of all values.
	 */
	public double sum() {
		if (numObs < 1)
			return Double.NaN;
		return meanEst * weightSum;
	}

	/**
	 * Returns the sum of all weights. If only {@link #value(double)} is used,
	 * then the value returned is identical to the value returned by
	 * {@link #numObs}.
	 * 
	 * @return The weight sum.
	 */
	public double weightSum() {
		if (numObs == 0)
			return Double.NaN;
		return weightSum;
	}

	/**
	 * Returns the number of times, {@link #value(double)} or
	 * {@link #value(double, double)} were called.
	 * 
	 * @return The number of calls to {@link #value(double)} or
	 *         {@link #value(double, double)}.
	 */
	public int numObs() {
		return numObs;
	}

	/**
	 * Returns the minimum value seen so far.
	 * 
	 * @return The minimum value seen so far, or NaN, if no values were given so
	 *         far.
	 */
	public double min() {
		if (numObs < 1)
			return Double.NaN;
		return min;
	}

	/**
	 * Returns the maximum value seen so far.
	 * 
	 * @return The maximum value seen so far, or NaN, if no values were given so
	 *         far.
	 */
	public double max() {
		if (numObs < 1)
			return Double.NaN;
		return max;
	}

	/**
	 * Combines the data in {@code other} with this SummaryStat-Object. The
	 * combined object behaves as if it had also seen the data of "other".
	 * 
	 * @param other
	 *            The {@link SummaryStat} to combine with this object.
	 * @return Returns {@code this} to allow easy chaining of calls.
	 */
	public SummaryStat combine(SummaryStat other) {
		double ws = weightSum + other.weightSum;
		double delta = other.meanEst - meanEst;

		meanEst = (meanEst * weightSum + other.meanEst * other.weightSum) / ws;
		varEst = varEst + other.varEst + delta * delta * weightSum
				* other.weightSum / ws;

		weightSum = ws;
		numObs += other.numObs;

		if (other.max > max)
			max = other.max;
		if (other.min < min)
			min = other.min;

		lastValue = other.lastValue;
		lastWeight = other.lastWeight;

		return this;
	}

	/**
	 * Clones this object. We can use the standard functionality here, as there
	 * are only primitive fields.
	 * 
	 * @return A clone of this {@link SummaryStat}.
	 */
	public SummaryStat clone() throws CloneNotSupportedException {
		return (SummaryStat) super.clone();
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

	// TODO: confidence interval calculation should be factored out
	public double confIntRangeSingle(double errorProb) {
		if (numObs <= 2)
			return Double.NaN;

		double deg = weightSum() - 1.0d;
		TDistribution dist = new TDistribution(deg);
		return Math.abs(dist.inverseCumulativeProbability(errorProb * 0.5d))
				* Math.sqrt(variance() / weightSum());
	}

	/**
	 * Returns the last value passed to {@link #value(double)} or
	 * {@link #value(double, double)}.
	 * 
	 * @return The last value, or NaN if no {@code numObs==0}.
	 */
	public double lastValue() {
		if (numObs == 0)
			return Double.NaN;
		return lastValue;
	}

	/**
	 * Returns the weight of the last value passed to {@link #value(double)} or
	 * {@link #value(double, double)}.
	 * 
	 * @return The last value's weight, or NaN if no {@code numObs==0}.
	 */
	public double lastWeight() {
		if (numObs == 0)
			return Double.NaN;
		return lastWeight;
	}

	/**
	 * Sets a descriptive name for this object.
	 * 
	 * @param name
	 *            A name for this {@code SummaryStat}.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this object.
	 * 
	 * @return The name for this {@code SummaryStat}.
	 */
	public String getName() {
		return name;
	}

	// ************* static utility methods *************

	/**
	 * This method creates a new {@code SummaryStat} object and passes all
	 * values to it.
	 * 
	 * @param values
	 *            The values to use.
	 * @return A {@code SummaryStat} summarizing the values.
	 */
	public static SummaryStat summarize(double... values) {
		return new SummaryStat().values(values);
	}

	/**
	 * This method creates a new {@code SummaryStat} object and passes all
	 * values to it.
	 * 
	 * @param values
	 *            The values to use.
	 * @return A {@code SummaryStat} summarizing the values.
	 */
	public static SummaryStat summarize(int... values) {
		SummaryStat res = new SummaryStat();
		for (int v : values) {
			res.value(v);
		}
		return res;
	}

	/**
	 * Creates a new {@code SummaryStat} object that behaves if all values seen
	 * by {@code stats1} and {@code stats2} would have been passed to it.
	 * 
	 * @param stats1
	 *            {@code SummaryStat} summarizing first set of values.
	 * @param stats2
	 *            {@code SummaryStat} summarizing second set of values.
	 * @return New {@code SummaryStat} object summarizing the union of first and
	 *         second value set.
	 */
	public static SummaryStat combine(SummaryStat stats1, SummaryStat stats2) {
		return new SummaryStat(stats1).combine(stats2);
	}

}
