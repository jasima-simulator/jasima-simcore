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
		this(null, null, null);
	}

	public IntEmpirical(double[] probs) {
		this(null, probs, null);
	}

	public IntEmpirical(Random rnd, double[] probs) {
		this(rnd, probs, null);
	}

	public IntEmpirical(double[] probs, int[] values) {
		this(null, probs, values);
	}

	public IntEmpirical(Random rnd, double[] probs, int[] values) {
		super();
		setProbabilities(probs, values);
		setRndGen(rnd);
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