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
import jasima.core.util.Util;

import java.util.Arrays;

/**
 * A DblConst stream infinitely returns the numbers of {@link #getValues()} in
 * exactly this order. After the last value being returned the sequence starts
 * again with the first number. Optionally the order of items is permuted is
 * {@code randomizeOrder} was set to true.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class DblConst extends DblStream {

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
	public double nextDbl() {
		double v = valuesRnd[next];
		// wrap around
		if (++next == valuesRnd.length)
			nextIteration();
		return v;
	}

	@Override
	public String toString() {
		return "DblConst" + Arrays.toString(values);
	}

	@Override
	public DblConst clone() throws CloneNotSupportedException {
		DblConst c = (DblConst) super.clone();

		if (values != null)
			c.values = values.clone();

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
	 * @param vs
	 *            The values to use.
	 */
	public void setValues(double... vs) {
		this.mean = null;
		if (vs == null)
			values = null;
		else
			values = vs.clone();
	}

	public boolean isRandomizeOrder() {
		return randomizeOrder;
	}

	/**
	 * If set to {@code true}, this elements of {@value values} will be returned
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