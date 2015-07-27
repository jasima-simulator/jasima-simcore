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

import jasima.core.util.Util;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Binomial_distribution">Binomial
 * distribution</a>. It has two parameters, the probability of success (in a
 * single trial) and the total number of trials conducted.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Binomial_distribution">Binomial
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/BinomialDistribution.html">Binomial
 *      distribution (MathWorld)</a>
 */
public class IntBinomial extends IntDistribution {

	private static final long serialVersionUID = 485349306562793350L;

	private BinomialDistribution dist;

	public IntBinomial() {
		this(20, 0.5);
	}

	public IntBinomial(int n, double p) {
		super();
		setDistribution(new BinomialDistribution(n, p));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (BinomialDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE,
				"IntBinomial(probabilityOfSuccess=%f;numTrials=%d)",
				getProbabilityOfSuccess(), getNumTrials());
	}

	public double getProbabilityOfSuccess() {
		return dist.getProbabilityOfSuccess();
	}

	/**
	 * Sets the probability of success in a single trial.
	 * 
	 * @param probOfSuccess
	 *            The success probability.
	 * @throws OutOfRangeException
	 *             If the supplied probability is not in the interval
	 *             {@code [0,1]}.
	 */
	public void setProbabilityOfSuccess(double probOfSuccess)
			throws OutOfRangeException {
		setDistribution(new BinomialDistribution(dist.getNumberOfTrials(),
				probOfSuccess));
	}

	public int getNumTrials() {
		return dist.getNumberOfTrials();
	}

	/**
	 * Sets the number of trails of the Bernoulli experiment.
	 * 
	 * @param numTrials
	 *            The number of trials.
	 * @throws NotPositiveException
	 *             If the supplied value was negative.
	 */
	public void setNumTrials(int numTrials) throws NotPositiveException {
		setDistribution(new BinomialDistribution(numTrials,
				dist.getProbabilityOfSuccess()));
	}

}
