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
package jasima.core.random.discrete;

import java.util.Arrays;

import jasima.core.util.Pair;
import jasima.core.util.Util;

/**
 * Returns a constant set of integer numbers, as passed to the constructor or
 * via {@link #setValues(int...)}. This value sequence is repeated if all values
 * are returned once. Optionally the order of items is permuted if
 * {@code randomizeOrder} was set to true.
 * 
 * @author Torsten Hildebrandt
 */
public class IntConst extends IntStream {

	private static final long serialVersionUID = -3297743869123820992L;

	private int[] values;
	private boolean randomizeOrder;

	private Double mean;
	private int next;
	private int[] valuesRnd;

	public IntConst() {
		this(null);
	}

	public IntConst(int... vs) {
		super();
		setValues(vs);
	}

	@Override
	public void init() {
		super.init();

		valuesRnd = isRandomizeOrder() ? values.clone() : values;
		nextIteration();
	}

	private void nextIteration() {
		next = 0;
		if (isRandomizeOrder()) {
			Util.shuffle(valuesRnd, rndGen);
		}
	}

	@Override
	public int nextInt() {
		int v = valuesRnd[next];
		// wrap around
		if (++next == valuesRnd.length)
			nextIteration();
		return v;
	}

	@Override
	public double getNumericalMean() {
		// lazy initialization of "mean" upon first call
		if (mean == null) {
			if (values == null || values.length == 0)
				mean = Double.NaN;
			else
				mean = ((double) Util.sum(values)) / values.length;
		}

		return mean;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		if (values == null || values.length == 0)
			return new Pair<>(Double.NaN, Double.NaN);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (int d : values) {
			if (d < min)
				min = d;
			if (d > max)
				max = d;
		}

		return new Pair<>((double) min, (double) max);
	}

	@Override
	public String toString() {
		return "IntConst" + Arrays.toString(values);
	}

	@Override
	public IntConst clone() throws CloneNotSupportedException {
		IntConst c = (IntConst) super.clone();

		if (values != null)
			c.values = values.clone();

		return c;
	}

	public int[] getValues() {
		return values;
	}

	/**
	 * Sets the values to return as members of this number stream.
	 * 
	 * @param vs
	 *            The values to use.
	 */
	public void setValues(int... vs) {
		this.values = vs;
		this.mean = null;
	}

	public boolean isRandomizeOrder() {
		return randomizeOrder;
	}

	/**
	 * If set to {@code true}, the elements of {@code values} will be returned
	 * in a randomly permuted order. Otherwise the values will be returned in
	 * exactly the same order as given in {@code values}.
	 * 
	 * @param randomizeOrder
	 *            Whether or not to randomize the order.
	 */
	public void setRandomizeOrder(boolean randomizeOrder) {
		this.randomizeOrder = randomizeOrder;
	}

}