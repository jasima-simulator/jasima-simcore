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
package jasima.core.random.continuous;

/**
 * Returns a random real number following a triangular distribution as defined
 * by the three parameters min, mode, and max.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public class DblTriangular extends DblStream {

	private static final long serialVersionUID = -8098960209631757070L;

	// parameters with some arbitrary default values
	private double min = 0.0;
	private double mode = 5.0;
	private double max = 10.0;

	// constants derived from parameters
	private double critValue, range, minDist, maxDist;

	public DblTriangular(double min, double mode, double max) {
		super();
		setMin(min);
		setMode(mode);
		setMax(max);
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
		return "DblTriangular(min=" + min + ";mode=" + mode + ";max=" + max
				+ ")";
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
		calcInternalValues();
	}

	public double getMode() {
		return mode;
	}

	public void setMode(double mode) {
		this.mode = mode;
		calcInternalValues();
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
		calcInternalValues();
	}

}
