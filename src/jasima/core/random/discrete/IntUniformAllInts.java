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

import java.util.Random;

/**
 * Generates uniformly distributed integers for the complete set of integer
 * values. This class just delegates to the underlying method
 * {@link java.util.Random#nextInt()}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class IntUniformAllInts extends IntStream {

	private static final long serialVersionUID = -2235431750818257710L;

	public IntUniformAllInts() {
		this(null, null);
	}

	public IntUniformAllInts(Random random) {
		this(random, null);
	}

	public IntUniformAllInts(String name) {
		this(null, name);
	}

	public IntUniformAllInts(Random random, String name) {
		super();
		setRndGen(random);
		setName(name);
	}

	@Override
	public int nextInt() {
		return rndGen.nextInt();
	}

	@Override
	public double getNumericalMean() {
		return (((long) Integer.MAX_VALUE) + Integer.MIN_VALUE) / 2.0;
	}

	@Override
	public String toString() {
		return "IntUniformAllInts";
	}

	@Override
	public Pair<Double, Double> getValueRange() {
		return new Pair<>((double) Integer.MIN_VALUE,
				(double) Integer.MAX_VALUE);
	}

}