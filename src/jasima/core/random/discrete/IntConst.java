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

import jasima.core.util.Util;

import java.util.Arrays;

/**
 * Returns a constant set of integer numbers, as passed to the constructor or
 * via {@link #setValues(int...)}. This value sequence is repeated if all values
 * are returned once. The sequence returned is not random at all, i.e. this
 * class does not use the inherited rndGen.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class IntConst extends IntStream {

	private static final long serialVersionUID = -3297743869223820992L;

	private int[] values;

	private int next;

	private int min, max;

	public IntConst(int... vs) {
		super();
		setValues(vs);
	}

	@Override
	public int min() {
		return min;
	}

	@Override
	public int max() {
		return max;
	}

	@Override
	public int nextInt() {
		int v = values[next];
		if (++next == values.length)
			next = 0;
		return v;
	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int... vs) {
		this.values = vs;
		min = Util.min(vs);
		max = Util.max(vs);
	}

	@Override
	public String toString() {
		return "IntConst" + Arrays.toString(values);
	}

	@Override
	public IntConst clone() throws CloneNotSupportedException {
		IntConst c = (IntConst) super.clone();

		if (values != null)
			c.values = Arrays.copyOf(values, values.length);

		return c;
	}

}