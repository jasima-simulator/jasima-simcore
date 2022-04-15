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

import java.io.Serializable;
import java.util.Random;

import jasima.core.util.Pair;
import jasima.shopSim.util.modelDef.streams.DblStreamDef;

/**
 * A stream of double numbers, usually the sequence is produced by a pseudo
 * random number generator.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class DblSequence implements Serializable, Cloneable {

	private static final long serialVersionUID = 7236623667061348954L;

	protected Random rndGen;

	public DblSequence() {
		super();
	}

	/**
	 * Initializes this stream. This method is supposed to be called once before
	 * repeated calls to {@link #nextDbl()} can be made.
	 */
	public void init() {
	}

	/**
	 * Returns the next number in this number stream.
	 */
	public abstract double nextDbl();

	/**
	 * Returns the arithmetic mean of the values returned by {@link #nextDbl()}.
	 */
	public double getNumericalMean() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method computes the minimum and maximum support values (range of
	 * possible values) of this stream.
	 * 
	 * @return A {@link Pair} containing the minimum and maximum support values.
	 */
	public Pair<Double, Double> getValueRange() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the minimum value to be produced by this object.
	 * 
	 * @return Returns the minimum value to be produced by this object.
	 * @see #getValueRange()
	 */
	public double min() {
		return getValueRange().a;
	}

	/**
	 * Returns the maximum value to be produced by this object.
	 * 
	 * @return Returns the maximum value to be produced by this object.
	 * @see #getValueRange()
	 */
	public double max() {
		return getValueRange().b;
	}

	/**
	 * Creates a {@link DblStreamDef} object from this stream. This method only
	 * delegates to {@link DblStreamDef#createStreamDefFromStream(DblSequence)} and
	 * therefore is final.
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
	public DblSequence clone() {
		if (rndGen != null) {
			throw new IllegalStateException("Only a DblStream without a rndGen set can be cloned.");
		}

		try {
			return (DblSequence) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new AssertionError(cantHappen);
		}
	}

	/**
	 * Returns the random number generator currently associated with this stream.
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

}
