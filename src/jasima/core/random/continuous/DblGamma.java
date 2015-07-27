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

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of real numbers where each value follows a <a
 * href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma
 * distribution</a>. The distribution is characterized by a shape and a scale
 * parameter. It is a generalization of the Exponential and Erlang
 * distributions.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma
 *      distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/GammaDistribution.html">Gamma
 *      distribution (MathWorld)</a>
 */
public class DblGamma extends DblDistribution {

	private static final long serialVersionUID = 8196438918640991723L;

	private GammaDistribution dist;

	public DblGamma() {
		this(2.0, 2.0);
	}

	public DblGamma(double shape, double scale) {
		super();
		setDistribution(new GammaDistribution(shape, scale));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (GammaDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "DblGamma(shape=%f;scale=%f)",
				getShape(), getScale());
	}

	public double getShape() {
		return dist.getShape();
	}

	/**
	 * Sets the shape parameter for this distribution.
	 * 
	 * @param shape
	 *            The shape parameter to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setShape(double shape) throws NotStrictlyPositiveException {
		setDistribution(new GammaDistribution(shape, dist.getScale()));
	}

	public double getScale() {
		return dist.getScale();
	}

	/**
	 * Sets the scale parameter for this distribution.
	 * 
	 * @param scale
	 *            The scale parameter to use.
	 * @throws NotStrictlyPositiveException
	 *             If the parameter value was {@code <=0.0}.
	 */
	public void setScale(double scale) throws NotStrictlyPositiveException {
		setDistribution(new GammaDistribution(dist.getShape(), scale));
	}

}
