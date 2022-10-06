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

import java.util.Random;

import org.apache.commons.math3.distribution.RealDistribution;

import jasima.core.util.Pair;

/**
 * Returns an arbitrarily distributed random number stream. Its distribution is
 * determined by an arbitrary {@link RealDistribution} object from the Apache
 * Commons Math library. This class is usually not used directly but through its
 * various sub-classes implementing particular distributions and exposing their
 * parameters as Java Bean properties.
 * 
 * @author Torsten Hildebrandt
 */
public class DblDistribution extends DblSequence {

	private static final long serialVersionUID = -157283852135250753L;

	private RealDistribution distribution;

	public DblDistribution() {
		this(null, null);
	}

	public DblDistribution(RealDistribution distribution) {
		this(null, distribution);
	}

	public DblDistribution(Random random, RealDistribution distribution) {
		super();
		setRndGen(random);
		setDistribution(distribution);
//		setName(name);
	}

	public RealDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Sets the continuous distribution to use.
	 */
	protected void setDistribution(RealDistribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public double nextDbl() {
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
		return new Pair<Double, Double>(distribution.getSupportLowerBound(), distribution.getSupportUpperBound());
	}

	@Override
	public String toString() {
		return "DblDistribution(" + String.valueOf(distribution) + ')';
	}

}