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

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Hypergeometric_distribution"
 * >Hypergeometric distribution</a>. It has a three parameters: the population
 * size, the number of (possible) successes in the population, and the number of
 * trials/samples.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Hypergeometric
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/HypergeometricDistribution.html">Hypergeometric
 *      distribution (MathWorld)</a>
 */
public class IntHypergeometric extends IntDistribution {

	private static final long serialVersionUID = 7866375360611450226L;

	private HypergeometricDistribution dist;

	public IntHypergeometric() {
		this(20, 10, 5);
	}

	public IntHypergeometric(int populationSize, int numberOfSuccesses,
			int sampleSize) {
		super();
		setDistribution(new HypergeometricDistribution(populationSize,
				numberOfSuccesses, sampleSize));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (HypergeometricDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return String
				.format(Util.DEF_LOCALE,
						"IntHypergeometric(populationSize=%d;numberOfSuccesses=%d;sampleSize=%d)",
						getPopulationSize(), getNumberOfSuccesses(),
						getSampleSize());
	}

	// ************* getter / setter below ****************

	public int getPopulationSize() {
		return dist.getPopulationSize();
	}

	/**
	 * The total population size.
	 * 
	 * @param populationSize
	 *            The value for the population size.
	 * @throws NotStrictlyPositiveException
	 *             If the supplied value was {@code <=0}.
	 */
	public void setPopulationSize(int populationSize)
			throws NotStrictlyPositiveException {
		setDistribution(new HypergeometricDistribution(populationSize,
				dist.getNumberOfSuccesses(), dist.getSampleSize()));
	}

	public int getNumberOfSuccesses() {
		return dist.getNumberOfSuccesses();
	}

	/**
	 * The number of possible successes in the population.
	 * 
	 * @param numberOfSuccesses
	 *            The number of successes.
	 * @throws NumberIsTooLargeException
	 *             If the supplied value is larger than {@code populationSize}.
	 */
	public void setNumberOfSuccesses(int numberOfSuccesses)
			throws NumberIsTooLargeException {
		setDistribution(new HypergeometricDistribution(
				dist.getPopulationSize(), numberOfSuccesses,
				dist.getSampleSize()));
	}

	public int getSampleSize() {
		return dist.getSampleSize();
	}

	/**
	 * The number of samples taken.
	 * 
	 * @param sampleSize
	 *            The number of trials conducted.
	 * @throws NumberIsTooLargeException
	 *             If the supplied value is larger than {@code populationSize}.
	 */
	public void setSampleSize(int sampleSize) throws NumberIsTooLargeException {
		setDistribution(new HypergeometricDistribution(
				dist.getPopulationSize(), dist.getNumberOfSuccesses(),
				sampleSize));
	}

}
