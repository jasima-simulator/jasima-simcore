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
public class DblTriangular extends DblSequence {

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
	 * @param min  The distribution's minimum value.
	 * @param mode The distribution's mode (most frequent) value.
	 * @param max  The distribution's maximum value.
	 * @return A new DblTriangular object.
	 */
	public static DblTriangular fromMinModeMax(double min, double mode, double max) {
		return new DblTriangular(min, mode, max);
	}

	/**
	 * Factory method to create a DblStream given the distribution's mean value.
	 * 
	 * @param min  The distribution's minimum value.
	 * @param mean The distribution's mean value.
	 * @param max  The distribution's maximum value.
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
		return defFormat("DblTriangular(min=%f;mode=%f;max=%f)", min, mode, max);
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
