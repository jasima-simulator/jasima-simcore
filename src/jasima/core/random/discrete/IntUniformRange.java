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
package jasima.core.random.discrete;

import jasima.core.util.Pair;

import java.util.Random;

/**
 * Generates uniformly distributed integers in the interval [min,max] (including
 * both min and max).
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class IntUniformRange extends IntStream {

	private static final long serialVersionUID = -7138352768070870971L;

	private int min, max, range;

	public IntUniformRange(int min, int max) {
		this(null, null, min, max);
	}

	public IntUniformRange(Random random, int min, int max) {
		this(random, null, min, max);
	}

	public IntUniformRange(String name, int min, int max) {
		this(null, name, min, max);
	}

	public IntUniformRange(Random random, String name, int min, int max) {
		super();
		setRange(min, max);
		setRndGen(random);
		setName(name);
	}

	@Override
	public void init() {
		super.init();
		setRange(min, max); // force value checks
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>((double) getMin(), (double) getMax());
	}

	public void setRange(int min, int max) {
		if (min > max)
			throw new IllegalArgumentException("min<max " + min + " " + max);
		long r = max - min;
		if (r > Integer.MAX_VALUE - 1 || r < 0)
			throw new IllegalArgumentException(
					"range has to fit in an integer. " + min + " " + max);

		this.setMin(min);
		this.setMax(max);
		range = (int) r;
	}

	@Override
	public int nextInt() {
		return getMin() + rndGen.nextInt(range + 1);
	}

	@Override
	public double getNumericalMean() {
		return (((long) getMin()) + getMax()) / 2.0;
	}

	@Override
	public String toString() {
		return "IntUniformRange(min=" + getMin() + ";max=" + getMax() + ")";
	}

	public int getMin() {
		return min;
	}

	/**
	 * Sets the minimum value returned by this number stream.
	 * 
	 * @param min
	 *            The minimum to use.
	 */
	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	/**
	 * Sets the maximum value returned by this number stream.
	 * 
	 * @param max
	 *            The maximum to use.
	 */
	public void setMax(int max) {
		this.max = max;
	}
}