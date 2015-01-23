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
package jasima.core.util;

import java.io.Serializable;

/**
 * Simple wrapper to store two different objects. Instances of this class are
 * immutable.
 * <p>
 * This class can be used safely with {@code null} values for a and b.
 * 
 * 
 * @param <A>
 *            Type of the first element.
 * @param <B>
 *            Type of the second element.
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class Pair<A, B> implements Cloneable, Serializable {

	private static final long serialVersionUID = -1202307182078250138L;

	private static final int NULL_HASH = 2147483647;

	public final A a;
	public final B b;

	public Pair(final A a, final B b) {
		super();
		this.a = a;
		this.b = b;
	}

	public Pair(final Pair<A, B> p) {
		this(p.a, p.b);
	}

	@Override
	public boolean equals(Object obj) {
		Pair<?, ?> p2 = (Pair<?, ?>) obj;

		boolean aEquals = a == null ? p2.a == null : a.equals(p2.a);
		boolean bEquals = b == null ? p2.b == null : b.equals(p2.b);

		return aEquals && bEquals;
	}

	@Override
	public int hashCode() {
		final int hcA = a != null ? a.hashCode() : NULL_HASH;
		final int hcB = b != null ? b.hashCode() : NULL_HASH;
		return ((hcA << 16) ^ hcB) & (hcA >>> 16);
	}

	/**
	 * Returns a String containing a and b seperated by comma and enclosed in
	 * arrow brackets.
	 */
	@Override
	public String toString() {
		return "<" + a + "," + b + ">";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Pair<A, B> clone() throws CloneNotSupportedException {
		return (Pair<A, B>) super.clone();
	}

}
