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

import org.apache.commons.math3.distribution.IntegerDistribution;

/**
 * Returns an arbitrarily distributed stream of integer numbers. Its
 * distribution is determined by an arbitrary {@link IntegerDistribution} object
 * from the Apache Commons Math library. This class is usually not used directly
 * but through its various sub-classes implementing particular distributions and
 * exposing their parameters as Java Bean properties.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class IntDistribution extends IntStream {

	private static final long serialVersionUID = -2911819386618158493L;

	private IntegerDistribution distribution;

	public IntDistribution() {
		this(null);
	}

	public IntDistribution(IntegerDistribution distribution) {
		super();
		setDistribution(distribution);
	}

	public IntegerDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Sets the discrete distribution to use.
	 */
	protected void setDistribution(IntegerDistribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public int nextInt() {
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
		double min = distribution.getSupportLowerBound();
		double max = distribution.getSupportUpperBound();
		return new Pair<Double, Double>(min,
				max);
	}

	@Override
	public String toString() {
		return "IntDistribution(" + String.valueOf(distribution) + ')';
	}

}