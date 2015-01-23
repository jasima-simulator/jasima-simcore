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
package jasima.core.random.continuous;

import jasima.core.random.RandomFactory;
import jasima.shopSim.util.modelDef.streams.DblStreamDef;

import java.io.Serializable;
import java.util.Random;

/**
 * A stream of double numbers, usually the sequence is produced by a pseudo
 * random number generator.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version 
 *          "$Id: DblStream.java 512 2015-01-20 16:52:46Z THildebrandt@gmail.com$"
 */
public abstract class DblStream implements Serializable, Cloneable {

	private static final long serialVersionUID = 7236623667061348954L;

	protected Random rndGen;
	private String name;

	public DblStream() {
		super();
	}

	public void init() {
	}

	/**
	 * Returns the next number in this number stream.
	 */
	public abstract double nextDbl();

	/**
	 * Returns the arithmetic mean of the values returned by {@link #nextDbl()}.
	 */
	public abstract double getNumericalMean();

	/**
	 * Creates a {@link DblStreamDef} object from this stream. This method only
	 * delegates to {@link DblStreamDef#createStreamDefFromStream(DblStream)}
	 * and therefore is final.
	 */
	public final DblStreamDef createStreamDefFromStream() {
		return DblStreamDef.createStreamDefFromStream(this);
	}

	/**
	 * Clones the current number stream. This method fails with a
	 * {@link CloneNotSupportedException} if there is a random number generator
	 * associated with this stream.
	 */
	@Override
	public DblStream clone() throws CloneNotSupportedException {
		if (rndGen != null)
			throw new CloneNotSupportedException(
					"Only a DblStream without a rndGen set can be cloned.");
		return (DblStream) super.clone();
	}

	/**
	 * Returns the random number generator currently associated with this
	 * stream.
	 */
	public Random getRndGen() {
		return rndGen;
	}

	/**
	 * Sets the random number generator to be used if this stream has a random
	 * component.
	 */
	public void setRndGen(Random rndGen) {
		this.rndGen = rndGen;
	}

	/**
	 * Returns the stream's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets an optional name for this stream. The name can be used to, e.g.,
	 * initialize the random number generator as in
	 * {@link RandomFactory#initNumberStream(DblStream, String)}.
	 * 
	 * @param name
	 *            The stream's name.
	 */
	public void setName(String name) {
		this.name = name;
	}

}
