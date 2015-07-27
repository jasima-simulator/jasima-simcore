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

import jasima.core.util.Pair;
import jasima.core.util.Util;

import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Bernoulli_distribution">Bernoulli
 * distribution</a>. This means, it contains the value {@code 0} with a
 * probability of {@code 1-probabilityOfSuccess} and the value {@code 1} with a
 * probability of {@code probabilityOfSuccess}.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Bernoulli_distribution">Bernoulli
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/BernoulliDistribution.html">Bernoulli
 *      distribution (MathWorld)</a>
 */
public class IntBernoulli extends IntStream {
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
		return String.format(Util.DEF_LOCALE,
				"IntBernoulli(probabilityOfSuccess=%f)",
				getProbabilityOfSuccess());
	}

	public double getProbabilityOfSuccess() {
		return probabilityOfSuccess;
	}

	/**
	 * Sets the probability of success.
	 * 
	 * @param probOfSuccess
	 *            The success probability.
	 * @throws OutOfRangeException
	 *             If the supplied probability is not in the interval
	 *             {@code [0,1]}.
	 */
	public void setProbabilityOfSuccess(double probOfSuccess)
			throws OutOfRangeException {
		if (probOfSuccess < 0 || probOfSuccess > 1) {
			throw new OutOfRangeException(probOfSuccess, 0, 1);
		}
		probabilityOfSuccess = probOfSuccess;
	}

}
