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

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.exception.NotPositiveException;

/**
 * This class implements a discrete number stream following a <a
 * href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson
 * distribution</a>. It is parameterized by its mean.
 * 
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson
 *      distribution (MathWorld)</a>
 * */
public class IntPoisson extends IntDistribution {

	private static final long serialVersionUID = -3018032705267062035L;

	private PoissonDistribution dist;

	public IntPoisson() {
		this(1.0);
	}

	public IntPoisson(double mean) {
		super();
		setDistribution(new PoissonDistribution(mean));
	}

	@Override
	protected void setDistribution(IntegerDistribution distribution) {
		dist = (PoissonDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "IntPoisson(mean=%f)", getMean());
	}

	// ************* getter / setter below ****************

	public double getMean() {
		return dist.getMean();
	}

	/**
	 * The mean of the Poisson distribution.
	 * 
	 * @param mean
	 *            The value to use.
	 * @throws NotPositiveException
	 *             If {@code mean} was {@code <=0}.
	 */
	public void setMean(double mean) throws NotPositiveException {
		setDistribution(new PoissonDistribution(mean));
	}

}
