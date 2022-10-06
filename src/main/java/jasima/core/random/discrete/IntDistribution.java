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

import org.apache.commons.math3.distribution.IntegerDistribution;

import jasima.core.util.Pair;

/**
 * Returns an arbitrarily distributed stream of integer numbers. Its
 * distribution is determined by an arbitrary {@link IntegerDistribution} object
 * from the Apache Commons Math library. This class is usually not used directly
 * but through its various sub-classes implementing particular distributions and
 * exposing their parameters as Java Bean properties.
 * 
 * @author Torsten Hildebrandt
 */
public class IntDistribution extends IntSequence {

	private static final long serialVersionUID = -2911819386618158493L;

	private IntegerDistribution distribution;

	public IntDistribution() {
		this(null);
	}

	public IntDistribution(IntegerDistribution distribution) {
		super();
		setDistribution(distribution);
	}

	public IntegerDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Sets the discrete distribution to use.
	 */
	protected void setDistribution(IntegerDistribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public int nextInt() {
		return distribution.inverseCumulativeProbability(rndGen.nextDouble());
	}

	@Override
	public double getNumericalMean() {
		if (distribution == null) {
			return Double.NaN;
		} else {
			return distribution.getNumericalMean();
		}
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		if (distribution == null)
			return null;
		double min = distribution.getSupportLowerBound();
		double max = distribution.getSupportUpperBound();
		return new Pair<Double, Double>(min, max);
	}

	@Override
	public String toString() {
		return "IntDistribution(" + String.valueOf(distribution) + ')';
	}

}