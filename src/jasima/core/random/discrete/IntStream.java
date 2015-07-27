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

import jasima.core.random.continuous.DblStream;

/**
 * A stream of integer numbers, usually the sequence is produced by a pseudo
 * random number generator. This classes' nextInt()-method returns an int-value
 * in the interval [min(),max()], i.e. including both min() and max(). This is
 * an abstract base class.
 * 
 * @author Torsten Hildebrandt
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
