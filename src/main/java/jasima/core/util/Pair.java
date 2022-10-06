/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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

	public A first() {
		return a;
	}

	public B second() {
		return b;
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
	 * Returns a String containing a and b separated by comma and enclosed in
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

	/**
	 * Convenient factory method calling the constructor
	 * {@link #Pair(Object, Object)}.
	 */
	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<>(a, b);
	}
}
