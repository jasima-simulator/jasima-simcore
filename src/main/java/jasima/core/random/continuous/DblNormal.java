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
package jasima.core.random.continuous;

import static jasima.core.util.i18n.I18n.defFormat;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

import jasima.core.util.Pair;

/**
 * Creates a number stream with values following a
 * <a href="http://en.wikipedia.org/wiki/Normal_distribution">Normal
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Normal_distribution">Normal
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/NormalDistribution.html">Normal
 *      distribution (MathWorld)</a>
 */
public class DblNormal extends DblSequence {

	private static final long serialVersionUID = 8266321644360710699L;

	private double mean;
	private double stdev;

	public DblNormal() {
		this(0.0, 1.0);
	}

	public DblNormal(double mean, double stdev) {
		super();

		setMean(mean);
		setStdev(stdev);
	}

	@Override
	public double nextDbl() {
		return getMean() + rndGen.nextGaussian() * getStdev();
	}

	@Override
	public double getNumericalMean() {
		return mean;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	@Override
	public String toString() {
		return defFormat("DblNormal(mean=%f;stdev=%f)", getMean(), getStdev());
	}

	public double getMean() {
		return mean;
	}

	/**
	 * Sets the mean value of this normally distributed number stream. Default value
	 * is {@code 0.0}.
	 * 
	 * @param mean The mean value.
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStdev() {
		return stdev;
	}

	/**
	 * Sets the standard deviation of this normally distributed number stream. This
	 * defaults to {@code 1.0}.
	 * 
	 * @param stdev The standard deviation.
	 * @throws NotPositiveException Raised if stdev was negative.
	 */
	public void setStdev(double stdev) throws NotPositiveException {
		if (stdev >= 0.0) {
			this.stdev = stdev;
		} else {
			throw new NotStrictlyPositiveException(stdev);
		}
	}

}
