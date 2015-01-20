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

import jasima.core.random.RandomFactory;
import jasima.shopSim.util.modelDef.streams.StreamDef;

import java.io.Serializable;
import java.util.Random;

/**
 * A stream of double numbers, usually the sequence is produced by a pseudo
 * random number generator.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
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

	public abstract double nextDbl();

	public StreamDef createStreamDefFromStream() {
		return StreamDef.createStreamDefFromStream(this);
	}

	@Override
	public DblStream clone() throws CloneNotSupportedException {
		if (rndGen != null)
			throw new CloneNotSupportedException(
					"Only a DblStream without a rndGen set can be cloned.");
		return (DblStream) super.clone();
	}

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
