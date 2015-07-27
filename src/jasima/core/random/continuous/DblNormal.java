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
package jasima.core.random.continuous;

import jasima.core.util.Pair;
import jasima.core.util.Util;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Creates a number stream with values following a <a
 * href="http://en.wikipedia.org/wiki/Normal_distribution">Normal
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Normal_distribution">Normal
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/NormalDistribution.html">Normal
 *      distribution (MathWorld)</a>
 */
public class DblNormal extends DblStream {

	private static final long serialVersionUID = 8266321644360710699L;

	private double mean = 0.0;
	private double stdev = 1.0;

	public DblNormal() {
		super();
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
		return String.format(Util.DEF_LOCALE, "DblNormal(mean=%f;stdev=%f)",
				getMean(), getStdev());
	}

	public double getMean() {
		return mean;
	}

	/**
	 * Sets the mean value of this normally distributed number stream. Default
	 * value is {@code 0.0}.
	 * 
	 * @param mean
	 *            The mean value.
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStdev() {
		return stdev;
	}

	/**
	 * Sets the standard deviation of this normally distributed number stream.
	 * This defaults to {@code 1.0}.
	 * 
	 * @param stdev
	 *            The standard deviation.
	 * @throws NotPositiveException
	 *             Raised if stdev was negative.
	 */
	public void setStdev(double stdev) throws NotPositiveException {
		if (stdev >= 0.0) {
			this.stdev = stdev;
		} else {
			throw new NotStrictlyPositiveException(stdev);
		}
	}

}
