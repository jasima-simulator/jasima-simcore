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

import jasima.core.random.continuous.DblStream;

/**
 * A stream of integer numbers, usually the sequence is produced by a pseudo
 * random number generator. This classes' nextInt()-method returns an int-value
 * in the interval [min(),max()], i.e. including both min() and max(). This is
 * an abstract base class.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id$"
 */
public abstract class IntStream extends DblStream {

	private static final long serialVersionUID = -4799011636085252707L;

	public IntStream() {
		super();
	}

	public abstract int nextInt();

	@Override
	public final double nextDbl() {
		return nextInt();
	}

}
