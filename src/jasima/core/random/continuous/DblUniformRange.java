/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
 *******************************************************************************/
package jasima.core.random.continuous;

import java.util.Random;

/**
 * Returns a uniformly distributed real number in the range [min, max).
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
public class DblUniformRange extends DblStream {

	private static final long serialVersionUID = 7183118564770349548L;

	private double min = 0.0d, max = 1.0d, range = 1.0d;

	public DblUniformRange(double min, double max) {
		this(null, null, min, max);
	}

	public DblUniformRange(Random random, double min, double max) {
		this(random, null, min, max);
	}

	public DblUniformRange(String name, double min, double max) {
		this(null, name, min, max);
	}

	public DblUniformRange(Random random, String name, double min, double max) {
		super();
		setRange(min, max);
		setRndGen(random);
		setName(name);
	}

	public double min() {
		return min;
	}

	public double max() {
		return max;
	}

	public void setRange(double min, double max) {
		if (min >= max)
			throw new IllegalArgumentException("min>=max " + min + " " + max);

		this.min = min;
		this.max = max;
		range = max - min;
	}

	@Override
	public double nextDbl() {
		return min + range * rndGen.nextDouble();
	}

	@Override
	public String toString() {
		return "DblUniformRange(min=" + min + ";max=" + max + ")";
	}

}