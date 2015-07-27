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

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * A class implementing a number stream with values following the <a
 * href="http://en.wikipedia.org/wiki/Exponential_distribution">Exponential
 * distribution</a>.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public final class DblExp extends DblDistribution {

	private static final long serialVersionUID = 7949202789555424738L;

	public static final double DEFAULT_MEAN = 1.0;

	private ExponentialDistribution dist;

	public DblExp() {
		this(DEFAULT_MEAN);
	}

	public DblExp(double mean) {
		super();
		setMean(mean);
	}

	public double getMean() {
		return dist.getMean();
	}

	/**
	 * Sets the mean of the exponential distribution.
	 * 
	 * @param mean
	 *            The exponential distribution's mean. This value has to be
	 *            {@code >0}.
	 * @throws NotStrictlyPositiveException
	 *             If the supplied mean value was not positive.
	 */
	public void setMean(double mean) throws NotStrictlyPositiveException {
		dist = new ExponentialDistribution(mean);
		setDistribution(dist);
	}

	@Override
	public String toString() {
		return String.format(Util.DEF_LOCALE, "DblExp(mean=%f)", getMean());
	}

	@Override
	public DblStream clone() throws CloneNotSupportedException {
		// default cloning behaviour is ok as 'dist' is immutable
		return super.clone();
	}

}
