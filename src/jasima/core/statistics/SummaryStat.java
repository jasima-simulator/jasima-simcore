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

	public SummaryStat values(double... vs) {
		for (double v : vs) {
			value(v);
		}

		return this;
	}

	public SummaryStat value(double v) {
		return value(v, 1.0d);
	}

	public SummaryStat value(double v, double weight) {
		if (weight < 0.0d)
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
		double r = q * weight / weightSum;

		meanEst = meanEst + r;
		varEst = varEst + r * oldSum * q;

		return this;
	}

	public double mean() {
		if (numObs < 1)
			return Double.NaN;
		return meanEst;
	}

	public double stdDev() {
		return Math.sqrt(variance());
	}

	public double variance() {
		if (numObs < 1)
			return Double.NaN;
		if (numObs == 1)
			return 0.0;
		if (weightSum <= 1.0)
			throw new IllegalStateException("weight sum is <=1.0: " + weightSum);

		return varEst / (weightSum - 1.0);
	}

	public double variancePopulation() {
		if (numObs < 1)
			return Double.NaN;
		if (numObs == 1)
			return 0.0;

		return varEst / weightSum;
	}

	/** Returns the coefficient of variation. */
	public double varCoeff() {
		return stdDev() / mean();
	}

	public double sum() {
		if (numObs < 1)
			return Double.NaN;
		return meanEst * weightSum;
	}

	public double weightSum() {
		if (numObs == 0)
			return Double.NaN;
		return weightSum;
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
	 * Combines the data in {@code other} with this SummaryStat-Object. The
	 * combined object behaves as if it had also seen the data of "other".
	 * 
	 * @return Returns {@code this} to allow easy chaining of calls.
	 */
	public SummaryStat combine(SummaryStat other) {
		double ws = weightSum + other.weightSum;
		double newMean = (meanEst * weightSum + other.meanEst * other.weightSum)
				/ (weightSum + other.weightSum);
		varEst = varEst + other.varEst + weightSum * meanEst * meanEst
				+ other.weightSum * other.meanEst * other.meanEst - ws
				* newMean * newMean;
		meanEst = newMean;

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

	public double confIntRangeSingle(double errorProb) {
		if (numObs <= 2)
			return Double.NaN;

		double deg = weightSum() - 1.0d;
		TDistribution dist = new TDistribution(deg);
		return Math.abs(dist.inverseCumulativeProbability(errorProb * 0.5d))
				* Math.sqrt(variance() / weightSum());
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
