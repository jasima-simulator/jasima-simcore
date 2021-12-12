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

import static jasima.core.util.i18n.I18n.defFormat;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Implements a stream of numbers following a
 * <a href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">Student's
 * t-distribution</a>. The distribution has a single shape parameter:
 * {@code degreesOfFreedom}.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Student%27s_t-distribution">
 *      Student's t-distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/Studentst-Distribution.html">
 *      Student 's t-Distribution (MathWorld)</a>
 */
public class DblTDist extends DblDistribution {

	private static final long serialVersionUID = -3355042798681194054L;

	private TDistribution dist;

	public DblTDist() {
		this(1.0);
	}

	public DblTDist(double degreesOfFreedom) {
		super();
		setDistribution(new TDistribution(degreesOfFreedom));
	}

	@Override
	protected void setDistribution(RealDistribution distribution) {
		dist = (TDistribution) distribution;
		super.setDistribution(distribution);
	}

	@Override
	public String toString() {
		return defFormat("DblTDist(degreesOfFreedom=%f)", getDegreesOfFreedom());
	}

	public double getDegreesOfFreedom() {
		return dist.getDegreesOfFreedom();
	}

	/**
	 * Sets the degrees of freedom for this distribution.
	 * 
	 * @param degreesOfFreedom The degrees of freedom to use.
	 * @throws NotStrictlyPositiveException If the parameter value was
	 *                                      {@code <=0.0}.
	 */
	public void setDegreesOfFreedom(double degreesOfFreedom) throws NotStrictlyPositiveException {
		setDistribution(new TDistribution(degreesOfFreedom));
	}

}
