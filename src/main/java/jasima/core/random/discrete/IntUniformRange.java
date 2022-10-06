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
package jasima.core.random.discrete;

import java.util.Random;

import jasima.core.util.Pair;

/**
 * Generates uniformly distributed integers in the interval [min,max] (including
 * both min and max).
 * 
 * @author Torsten Hildebrandt
 */
public class IntUniformRange extends IntSequence {

	private static final long serialVersionUID = -7138352768070870971L;

	private int min, max, range;

	public IntUniformRange(int min, int max) {
		this(null, min, max);
	}

	public IntUniformRange(Random random, int min, int max) {
		super();
		setRange(min, max);
		setRndGen(random);
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
			throw new IllegalArgumentException("range has to fit in an integer. " + min + " " + max);

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
	 * @param min The minimum to use.
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
	 * @param max The maximum to use.
	 */
	public void setMax(int max) {
		this.max = max;
	}
}