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

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a number stream with values following a <a
 * href="http://en.wikipedia.org/wiki/Beta_distribution">Beta distribution</a>.
 * A beta distribution is characterized by the two positive shape parameters
 * {@code alpha} and {@code beta}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Beta_distribution">Beta
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/BetaDistribution.html">Beta
 *      distribution (MathWorld)</a>
 */
public class DblBeta extends DblDistribution {

	private static final long serialVersionUID = -5717640931580338541L;

	private BetaDistribution dist;

	public DblBeta() {
		this(2.0, 5.0);
	}

	public DblBeta(double alpha, double beta) {
		super();
		setDistribution(new BetaDistribution(alpha, beta));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (BetaDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "DblBeta(alpha=%f;beta=%f)",
				getAlpha(), getBeta());
	}

	public double getAlpha() {
		return dist.getAlpha();
	}

	/**
	 * Sets the parameter value for the distribution's shape parameter
	 * {@code alpha}.
	 * 
	 * @param alpha
	 *            The alpha value to use.
	 * @throws NotStrictlyPositiveException
	 *             If {@code alpha} was {@code <=0}.
	 */
	public void setAlpha(double alpha) throws NotStrictlyPositiveException {
		setDistribution(new BetaDistribution(alpha, getBeta()));
	}

	public double getBeta() {
		return dist.getBeta();
	}

	/**
	 * Sets the shape parameter {@code beta} of the distribution.
	 * 
	 * @param beta
	 *            The {@code beta} parameter value to use.
	 * @throws NotStrictlyPositiveException
	 *             If {@code beta} was {@code <=0}.
	 */
	public void setBeta(double beta) throws NotStrictlyPositiveException {
		setDistribution(new BetaDistribution(getAlpha(), beta));
	}

}
