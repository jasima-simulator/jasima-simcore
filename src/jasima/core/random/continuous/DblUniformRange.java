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

import jasima.core.util.Pair;

import java.util.Random;

/**
 * Returns a uniformly distributed real number in the range [min, max).
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class DblUniformRange extends DblStream {

	private static final long serialVersionUID = 7183118564770349548L;

	private double min = 0.0d, max = 1.0d, range = 1.0d;

	public DblUniformRange(double min, double max) {
		this(null, null, min, max);
	}

	public DblUniformRange(Random random, double min, double max) {
		this(random, null, min, max);
	}

	public DblUniformRange(String name, double min, double max) {
		this(null, name, min, max);
	}

	public DblUniformRange(Random random, String name, double min, double max) {
		super();
		setRange(min, max);
		setRndGen(random);
		setName(name);
	}

	public void setRange(double min, double max) {
		checkValues(min, max);

		this.min = min;
		this.max = max;
	}

	@Override
	public void init() {
		checkValues(min, max);

		super.init();
		range = max - min;
	}

	@Override
	public double nextDbl() {
		return min + range * rndGen.nextDouble();
	}

	@Override
	public double getNumericalMean() {
		return (getMin() + getMax()) / 2.0;
	}

	@Override
	public String toString() {
		return "DblUniformRange(min=" + min + ";max=" + max + ")";
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(getMin(), getMax());
	}

	private void checkValues(double min, double max) {
		if (min > max)
			throw new IllegalArgumentException("min>max " + min + " " + max);
	}

	public double getMin() {
		return min;
	}

	/**
	 * Sets the minimum value returned by this number stream.
	 * 
	 * @param min
	 *            The minimum to use.
	 */
	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	/**
	 * Sets the maximum value returned by this number stream.
	 * 
	 * @param max
	 *            The maximum to use.
	 */
	public void setMax(double max) {
		this.max = max;
	}

}