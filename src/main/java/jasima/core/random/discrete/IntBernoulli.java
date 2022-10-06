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

import org.apache.commons.math3.exception.OutOfRangeException;

import jasima.core.util.Pair;

/**
 * This class implements a discrete number stream following a
 * <a href="http://en.wikipedia.org/wiki/Bernoulli_distribution">Bernoulli
 * distribution</a>. This means, it contains the value {@code 0} with a
 * probability of {@code 1-probabilityOfSuccess} and the value {@code 1} with a
 * probability of {@code probabilityOfSuccess}.
 * 
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Bernoulli_distribution">Bernoulli
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/BernoulliDistribution.html">
 *      Bernoulli distribution (MathWorld)</a>
 */
public class IntBernoulli extends IntSequence {
	private static final long serialVersionUID = 1515307949822420310L;

	private double probabilityOfSuccess;

	public IntBernoulli() {
		this(0.5);
	}

	public IntBernoulli(double probabilityOfSuccess) {
		super();
		setProbabilityOfSuccess(probabilityOfSuccess);
	}

	@Override
	public int nextInt() {
		return rndGen.nextDouble() < probabilityOfSuccess ? 1 : 0;
	}

	@Override
	public double getNumericalMean() {
		return probabilityOfSuccess;
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>(0.0, 1.0);
	}

	@Override
	public String toString() {
		return defFormat("IntBernoulli(probabilityOfSuccess=%f)", getProbabilityOfSuccess());
	}

	public double getProbabilityOfSuccess() {
		return probabilityOfSuccess;
	}

	/**
	 * Sets the probability of success.
	 * 
	 * @param probOfSuccess The success probability.
	 * @throws OutOfRangeException If the supplied probability is not in the
	 *                             interval {@code [0,1]}.
	 */
	public void setProbabilityOfSuccess(double probOfSuccess) throws OutOfRangeException {
		if (probOfSuccess < 0 || probOfSuccess > 1) {
			throw new OutOfRangeException(probOfSuccess, 0, 1);
		}
		probabilityOfSuccess = probOfSuccess;
	}

}
