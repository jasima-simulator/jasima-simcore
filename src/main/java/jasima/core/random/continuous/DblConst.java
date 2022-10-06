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

import java.util.Arrays;
import java.util.Random;

import jasima.core.util.Pair;
import jasima.core.util.Util;

/**
 * A DblConst stream infinitely returns the numbers of {@link #getValues()} in
 * exactly this order. After the last value being returned the sequence starts
 * again with the first number. Optionally the order of items is permuted if
 * {@code randomizeOrder} was set to true.
 * 
 * @author Torsten Hildebrandt
 */
public class DblConst extends DblSequence {

	private static final long serialVersionUID = -2122011743105354569L;

	private double[] values;
	private boolean randomizeOrder;

	private Double mean;
	private int next;
	private double[] valuesRnd;

	public DblConst() {
		this(null);
	}

	public DblConst(double... vs) {
		super();
		next = Integer.MAX_VALUE;
		setValues(vs);
	}

	private void nextIteration() {
		if (isRandomizeOrder()) {
			if (valuesRnd == null)
				valuesRnd = values.clone();
			Util.shuffle(valuesRnd, rndGen);
		} else {
			valuesRnd = values;
		}
		next = 0;
	}

	@Override
	public double nextDbl() {
		if (valuesRnd == null || next >= valuesRnd.length) {
			nextIteration();
		}

		double v = valuesRnd[next];
		next++;

		return v;
	}

	@Override
	public void setRndGen(Random rndGen) {
		valuesRnd = null;
		super.setRndGen(rndGen);
	}

	@Override
	public String toString() {
		return "DblConst" + Arrays.toString(values);
	}

	@Override
	public DblConst clone() {
		DblConst c = (DblConst) super.clone();

		c.values = values; // safe to only copy the reference, values is not changed/ directly accessible
		c.valuesRnd = null;

		return c;
	}

	@Override
	public double getNumericalMean() {
		// lazy initialization of "mean" upon first call
		if (mean == null) {
			if (values == null || values.length == 0)
				mean = Double.NaN;
			else
				mean = Util.sum(values) / values.length;
		}

		return mean;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		if (values == null || values.length == 0)
			return new Pair<>(Double.NaN, Double.NaN);

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (double d : values) {
			if (!(min <= d))
				min = d;
			if (!(max >= d))
				max = d;
		}

		return new Pair<>(min, max);
	}

	public double[] getValues() {
		return (values == null) ? null : values.clone();
	}

	/**
	 * Sets the values to return as members of this number stream.
	 * 
	 * @param vs The values to use.
	 */
	public void setValues(double... vs) {
		mean = null;
		values = vs == null ? null : vs.clone();
		valuesRnd = null;
	}

	public boolean isRandomizeOrder() {
		return randomizeOrder;
	}

	/**
	 * If set to {@code true}, the elements of {@code values} will be returned in a
	 * randomly permuted order. Otherwise the values will be returned in exactly the
	 * same order as given in {@code values}.
	 * 
	 * @param randomizeOrder Whether or not to randomize the order.
	 */
	public void setRandomizeOrder(boolean randomizeOrder) {
		this.randomizeOrder = randomizeOrder;
	}

}