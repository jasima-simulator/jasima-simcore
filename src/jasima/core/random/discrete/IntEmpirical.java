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

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.Arrays;
import java.util.Random;

import jasima.core.util.Pair;
import jasima.core.util.Util;

/**
 * Returns an integer in the range [0,n]. The probability of each value is
 * determined by the probabilities passed to {@link #setProbabilities(double[])}
 * , and can be arbitrary positive numbers as long as they sum up to 1.0.
 * 
 * @author Torsten Hildebrandt
 */
public class IntEmpirical extends IntSequence {

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

	public void setProbabilities(double[] probs) {
		setProbabilities(probs, null);
	}

	public int[] getValues() {
		return vals;
	}

	public void setValues(int[] vs) {
		vals = vs;
	}

	@Override
	public int nextInt() {
		double prob = rndGen.nextDouble();

		int n = -1;

		double d = 0.0d;
		for (int i = 0; i < probs.length; i++) {
			d += probs[i];
			if (d > prob) {
				n = i;
				break; // for
			}
		}

		assert n >= 0; // we should always find something

		return vals != null ? vals[n] : n;
	}

	public void setProbabilities(double[] probs, int[] values) {
		if (probs != null && Math.abs(Util.sum(probs) - 1.0d) > 1e-6)
			throw new IllegalArgumentException(
					"probabilities must add to 1: " + Arrays.toString(probs) + " " + Util.sum(probs));
		if (values != null && values.length != probs.length)
			throw new IllegalArgumentException("There has to be a value for each probability.");

		this.probs = probs;
		this.vals = values;
		this.mean = null;
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
	public IntEmpirical clone() {
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
			String v = vals != null && i < vals.length ? Integer.toString(vals[i]) : "?";
			String p = probs != null && i < probs.length ? Double.toString(probs[i]) : "?";

			sb.append('<').append(v).append(',').append(p).append(">;");
		}
		if (sb.length() > 0)
			params = sb.substring(0, sb.length() - 1);

		return defFormat("%s(%s)", this.getClass().getSimpleName(), params);
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