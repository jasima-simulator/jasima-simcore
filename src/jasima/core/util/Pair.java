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
package jasima.core.util;

import java.io.Serializable;

/**
 * Simple wrapper to store two different objects. Instances of this class are
 * immutable.
 * 
 * @param <A>
 *            Type of the first element.
 * @param <B>
 *            Type of the second element.
 * @author Torsten Hildebrandt
 * @version $Id$
 */
public class Pair<A, B> implements Cloneable, Serializable {

	private static final long serialVersionUID = -1202307182078250138L;

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

	public static <A, B> Pair<A, B> makePair(A a, B b) {
		return new Pair<A, B>(a, b);
	}

	@Override
	public boolean equals(Object obj) {
		Pair<?, ?> p2 = (Pair<?, ?>) obj;

		return a.equals(p2.a) && b.equals(p2.b);
	}

	@Override
	public String toString() {
		return "<" + a + "," + b + ">";
	}

	@Override
	public int hashCode() {
		final int hcA = a.hashCode();
		return ((hcA << 16) ^ b.hashCode()) & (hcA >>> 16);
	}

	@Override
	public Pair<A, B> clone() throws CloneNotSupportedException {
		return new Pair<A, B>(a, b);
	}

}
