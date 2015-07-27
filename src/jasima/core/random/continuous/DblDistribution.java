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

import jasima.core.util.Pair;

import java.util.Random;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * Returns an arbitrarily distributed random number stream. Its distribution is
 * determined by an arbitrary {@link RealDistribution} object from the Apache
 * Commons Math library. This class is usually not used directly but through its
 * various sub-classes implementing particular distributions and exposing their
 * parameters as Java Bean properties.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class DblDistribution extends DblStream {

	private static final long serialVersionUID = -157283852135250753L;

	private RealDistribution distribution;

	public DblDistribution() {
		this(null, null, null);
	}

	public DblDistribution(RealDistribution distribution) {
		this(null, null, distribution);
	}

	public DblDistribution(Random random, RealDistribution distribution) {
		this(random, null, distribution);
	}

	public DblDistribution(String name, RealDistribution distribution) {
		this(null, name, distribution);
	}

	public DblDistribution(Random random, String name,
			RealDistribution distribution) {
		super();
		setRndGen(random);
		setDistribution(distribution);
		setName(name);
	}

	public RealDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Sets the continuous distribution to use.
	 */
	protected void setDistribution(RealDistribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public double nextDbl() {
		return distribution.inverseCumulativeProbability(rndGen.nextDouble());
	}

	@Override
	public double getNumericalMean() {
		if (distribution == null) {
			return Double.NaN;
		} else {
			return distribution.getNumericalMean();
		}
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		if (distribution == null)
			return null;
		return new Pair<Double, Double>(distribution.getSupportLowerBound(),
				distribution.getSupportUpperBound());
	}

	@Override
	public String toString() {
		return "DblDistribution(" + String.valueOf(distribution) + ')';
	}

}