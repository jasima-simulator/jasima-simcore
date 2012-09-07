/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
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
 * Generates uniformly distributed integers in the interval [min,max] (including
 * both min and max).
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class IntUniformRange extends IntStream {

	private static final long serialVersionUID = -7338352768070870971L;

	private int min, max, range;

	public IntUniformRange(int min, int max) {
		this(null, null, min, max);
	}

	public IntUniformRange(Random random, int min, int max) {
		this(random, null, min, max);
	}

	public IntUniformRange(String name, int min, int max) {
		this(null, name, min, max);
	}

	public IntUniformRange(Random random, String name, int min, int max) {
		super();
		setRange(min, max);
		setRndGen(random);
		setName(name);
	}

	@Override
	public int min() {
		return min;
	}

	@Override
	public int max() {
		return max;
	}

	public void setRange(int min, int max) {
		if (min > max)
			throw new IllegalArgumentException("min<max " + min + " " + max);
		long r = max - min;
		if (r > Integer.MAX_VALUE - 1)
			throw new IllegalArgumentException(
					"range has to fit in an integer. " + min + " " + max);

		this.min = min;
		this.max = max;
		range = (int) r;
	}

	@Override
	public int nextInt() {
		return min + rndGen.nextInt(range + 1);
	}

	@Override
	public String toString() {
		return "IntUniformRange(min=" + min + ";max=" + max + ")";
	}

}