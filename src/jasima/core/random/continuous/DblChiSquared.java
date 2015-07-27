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
package jasima.core.random.continuous;

import jasima.core.util.Util;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of numbers following a <a
 * href="http://en.wikipedia.org/wiki/Chi-squared_distribution">Chi-squared
 * distribution</a>. The distribution has a single shape (integer) parameter:
 * {@code degreesOfFreedom}. This distribution is a special case of a
 * {@link DblGamma Gamma distribution}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Chi-squared_distribution">Chi-squared
 *      distribution (Wikipedia)</a>
 * @see <a
 *      href="http://mathworld.wolfram.com/Chi-SquaredDistribution.html">Chi-squared
 *      distribution (MathWorld)</a>
 */
public class DblChiSquared extends DblDistribution {

	private static final long serialVersionUID = -5702591884949743185L;

	private ChiSquaredDistribution dist;

	public DblChiSquared() {
		this(2);
	}

	public DblChiSquared(int degreesOfFreedom) {
		super();
		setDistribution(new ChiSquaredDistribution(degreesOfFreedom));
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE,
				"DblChiSquared(degreesOfFreedom=%d)", getDegreesOfFreedom());
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (ChiSquaredDistribution) distribution;
		super.setDistribution(distribution);
	}

	public int getDegreesOfFreedom() {
		return (int) dist.getDegreesOfFreedom();
	}

	/**
	 * Sets the degrees of freedom for this distribution.
	 * 
	 * @param degreesOfFreedom
	 *            The degrees of freedom to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setDegreesOfFreedom(int degreesOfFreedom)
			throws NotStrictlyPositiveException {
		setDistribution(new ChiSquaredDistribution(degreesOfFreedom));
	}

}
