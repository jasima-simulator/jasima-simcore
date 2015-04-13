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
import jasima.core.util.Util;

import java.util.Arrays;
import java.util.Random;

/**
 * Returns an integer in the range [0,n]. The probability of each value is
 * determined by the probabilities passed to {@link #setProbabilities(double[])}
 * , and can be arbitrary positive numbers as long as they sum up to 1.0.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class IntEmpirical extends IntStream {

	private static final long serialVersionUID = -8591371451392742035L;

	public IntEmpirical() {
		this(null, null, null, null);
	}

	public IntEmpirical(double[] probs) {
		this(null, null, probs, null);
	}

	public IntEmpirical(Random rnd, double[] probs) {
		this(rnd, null, probs, null);
	}

	public IntEmpirical(String name, double[] probs) {
		this(null, name, probs, null);
	}

	public IntEmpirical(double[] probs, int[] values) {
		this(null, null, probs, values);
	}

	public IntEmpirical(Random rnd, double[] probs, int[] values) {
		this(rnd, null, probs, values);
	}

	public IntEmpirical(String name, double[] probs, int[] values) {
		this(null, name, probs, values);
	}

	public IntEmpirical(Random rnd, String name, double[] probs, int[] values) {
		super();
		setProbabilities(probs, values);
		setRndGen(rnd);
		setName(name);
	}

	private double[] probs = null;
	private int[] vals = null;
	private Double mean = null;

	public double[] getProbabilities() {
		return probs;
	}

	public int[] getValues() {
		return vals;
	}

	public void setProbabilities(double[] probs, int[] values) {
		if (probs != null && Math.abs(Util.sum(probs) - 1.0d) > 1e-6)
			throw new IllegalArgumentException("probabilities must add to 1: "
					+ Arrays.toString(probs) + " " + Util.sum(probs));
		if (values != null && values.length != probs.length)
			throw new IllegalArgumentException(
					"There has to be a value for each probability.");

		this.probs = probs;
		this.vals = values;
		this.mean = null;
	}

	/**
	 * Sets only probabilities. In this case {@link #nextInt()} will produce
	 * integers in the range {@code [0, probs.length-1]}.
	 */
	public void setProbabilities(double[] probs) {
		setProbabilities(probs, null);
	}

	@Override
	public int nextInt() {
		double prob = rndGen.nextDouble();

		double d = 0.0d;
		for (int i = 0; i < probs.length; i++) {
			d += probs[i];
			if (d > prob)
				return vals != null ? vals[i] : i;
		}

		throw new AssertionError(); // should never be reached
	}

	@Override
	public double getNumericalMean() {
		if (mean == null) {
			if (probs == null || probs.length == 0) {
				mean = Double.NaN;
			} else {
				mean = 0.0;
				for (int i = 0; i < probs.length; i++) {
					int value = vals == null ? i : vals[i];
					mean += probs[i] * value;
				}
			}
		}

		return mean;
	}

	@Override
	public IntEmpirical clone() throws CloneNotSupportedException {
		IntEmpirical c = (IntEmpirical) super.clone();

		if (probs != null)
			c.probs = probs.clone();
		if (vals != null)
			c.vals = vals.clone();

		return c;
	}

	@Override
	public String toString() {
		String params = "";

		StringBuilder sb = new StringBuilder();

		int n = probs != null ? probs.length : 0;
		int m = vals != null ? vals.length : 0;
		for (int i = 0; i < Math.max(n, m); i++) {
			String v = vals != null && i < vals.length ? Integer
					.toString(vals[i]) : "?";
			String p = probs != null && i < probs.length ? Double
					.toString(probs[i]) : "?";

			sb.append('<').append(v).append(',').append(p).append(">;");
		}
		if (sb.length() > 0)
			params = sb.substring(0, sb.length() - 1);

		return String.format(Util.DEF_LOCALE, "%s(%s)", this.getClass()
				.getSimpleName(), params);
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		int min;
		int max;
		if (vals != null) {
			min = Util.min(vals);
			max = Util.max(vals);
		} else {
			min = 0;
			max = probs.length;
		}
		return new Pair<>((double) min, (double) max);
	}

}