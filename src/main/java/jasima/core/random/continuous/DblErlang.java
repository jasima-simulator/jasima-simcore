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
 * Implementation of the
 * <a href="https://en.wikipedia.org/wiki/Erlang_distribution">Erlang
 * distribution</a>. It is the distribution of a sum of {@code shape}
 * independent exponential variables each with mean {@code scale}.
 * <p>
 * The Erlang distribution is a special case of the Gamma distribution (with the
 * shape parameter being restricted to an integer. For shape=1 it is the same as
 * the Exponential distribution.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Erlang_distribution">Erlang
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/ErlangDistribution.html">Erlang
 *      Distribution (MathWorld)</a>
 */
public class DblErlang extends DblSequence {

	private static final long serialVersionUID = -1862722756069025018L;

	private int shape;
	private double scale;

	public DblErlang() {
		this(1, 1.0);
	}

	public DblErlang(int shape, double scale) {
		super();
		this.shape = shape;
		this.scale = scale;
	}

	@Override
	public double nextDbl() {
		Random rnd = getRndGen();

		double unifProduct = 1.0;
		for (int i = 0; i < shape; i++) {
			unifProduct *= rnd.nextDouble();
		}

		return -scale * Math.log(unifProduct);
	}

	@Override
	public double getNumericalMean() {
		return shape * scale;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<Double, Double>(0.0, Double.POSITIVE_INFINITY);
	}

	// getter/setter below

	public int getShape() {
		return shape;
	}

	/** Sets the shape parameter of this distribution. */
	public void setShape(int shape) {
		if (shape < 1)
			throw new IllegalArgumentException();

		this.shape = shape;
	}

	public double getScale() {
		return scale;
	}

	/** Sets the scale parameter of this distribution. */
	public void setScale(double scale) {
		if (!(scale > 0.0))
			throw new IllegalArgumentException();

		this.scale = scale;
	}

	@Override
	public String toString() {
		return defFormat("DblErlang(sape=%d;scale=%f)", getShape(), getScale());
	}

}
