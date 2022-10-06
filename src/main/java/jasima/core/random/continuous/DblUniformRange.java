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

import java.util.Random;

import jasima.core.util.Pair;

/**
 * Returns a uniformly distributed real number in the range [min, max).
 * 
 * @author Torsten Hildebrandt
 */
public class DblUniformRange extends DblSequence {

	private static final long serialVersionUID = 7183118564770349548L;

	private double min = 0.0d, max = 1.0d, range = 1.0d;

	public DblUniformRange(double min, double max) {
		this(null, min, max);
	}

	public DblUniformRange(Random random, double min, double max) {
		super();
		setRange(min, max);
		setRndGen(random);
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
		return defFormat("DblUniformRange(min=%f;max=%f)", min, max);
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(getMin(), getMax());
	}

	private void checkValues(double min, double max) {
		if (!(min < max))
			throw new IllegalArgumentException("min>max " + min + " " + max);
	}

	public double getMin() {
		return min;
	}

	/**
	 * Sets the minimum value returned by this number stream.
	 * 
	 * @param min The minimum to use.
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
	 * @param max The maximum to use.
	 */
	public void setMax(double max) {
		this.max = max;
	}

}