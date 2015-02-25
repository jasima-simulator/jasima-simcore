/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.random.discrete;

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

}