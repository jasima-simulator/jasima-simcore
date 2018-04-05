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

/**
 * Returns a random real number following a
 * <a href="http://en.wikipedia.org/wiki/Triangular_distribution">triangular
 * distribution</a> as defined by the three parameters min, mode, and max.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Triangular_distribution">
 *      Triangular distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/TriangularDistribution.html">
 *      Triangular distribution (MathWorld)</a>
 */
public class DblTriangular extends DblStream {

	private static final long serialVersionUID = -8098960209631757070L;

	// parameters with some arbitrary default values
	private double min = 0.0;
	private double mode = 5.0;
	private double max = 10.0;

	// constants derived from parameters
	private double critValue = Double.NaN, range = Double.NaN, minDist = Double.NaN, maxDist = Double.NaN;

	public DblTriangular(double min, double mode, double max) {
		super();

		if (min > mode || mode > max)
			throw new IllegalArgumentException();

		setMin(min);
		setMode(mode);
		setMax(max);
	}

	/**
	 * Factory method to create a DblStream given the distribution's mode value.
	 * 
	 * @param min
	 *            The distribution's minimum value.
	 * @param mode
	 *            The distribution's mode (most frequent) value.
	 * @param max
	 *            The distribution's maximum value.
	 * @return A new DblTriangular object.
	 */
	public static DblTriangular fromMinModeMax(double min, double mode, double max) {
		return new DblTriangular(min, mode, max);
	}

	/**
	 * Factory method to create a DblStream given the distribution's mean value.
	 * 
	 * @param min
	 *            The distribution's minimum value.
	 * @param mean
	 *            The distribution's mean value.
	 * @param max
	 *            The distribution's maximum value.
	 * @return A new DblTriangular object.
	 */
	public static DblTriangular fromMinMeanMax(double min, double mean, double max) {
		double mode = mean * 3.0 - min - max;
		return new DblTriangular(min, mode, max);
	}

	@Override
	public void init() {
		super.init();

		if (min > mode || mode > max)
			throw new IllegalArgumentException();

		calcInternalValues();
	}

	private void calcInternalValues() {
		range = (max - min);
		minDist = (mode - min);
		maxDist = (max - mode);
		critValue = minDist / range;
	}

	@Override
	public double nextDbl() {
		double rnd = rndGen.nextDouble();

		// inverse of CDF
		double v;
		if (rnd <= critValue) {
			v = getMin() + Math.sqrt(range * minDist * rnd);
		} else {
			v = getMax() - Math.sqrt(range * maxDist * (1.0 - rnd));
		}

		return v;
	}

	@Override
	public double getNumericalMean() {
		return (getMin() + getMode() + getMax()) / 3.0;
	}

	@Override
	public String toString() {
		return "DblTriangular(min=" + min + ";mode=" + mode + ";max=" + max + ")";
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(getMin(), getMax());
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMode() {
		return mode;
	}

	public void setMode(double mode) {
		this.mode = mode;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

}
