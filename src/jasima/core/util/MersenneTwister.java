// based on http://www.cs.gmu.edu/~sean/research/mersenne/MersenneTwister.java by Sean Luke
package jasima.core.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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

public strictfp class MersenneTwister extends java.util.Random implements
		Serializable, Cloneable {
	// Serialization
	private static final long serialVersionUID = -4035832775130174188L; // locked
																		// as of
																		// Version
																		// 15

	// Period parameters
	private static final int N = 624;
	private static final int M = 397;
	private static final int MATRIX_A = 0x9908b0df; // private static final *
													// constant vector a
	private static final int UPPER_MASK = 0x80000000; // most significant w-r
														// bits
	private static final int LOWER_MASK = 0x7fffffff; // least significant r
														// bits

	// Tempering parameters
	private static final int TEMPERING_MASK_B = 0x9d2c5680;
	private static final int TEMPERING_MASK_C = 0xefc60000;

	private int mt[]; // the array for the state vector
	private int mti; // mti==N+1 means mt[N] is not initialized
	private int mag01[];

	// a good initial seed (of int size, though stored in a long)
	// private static final long GOOD_SEED = 4357;

	/*
	 * implemented here because there's a bug in Random's implementation of the
	 * Gaussian code (divide by zero, and log(0), ugh!), yet its gaussian
	 * variables are private so we can't access them here. :-(
	 */

	private double __nextNextGaussian;
	private boolean __haveNextNextGaussian;

	/*
	 * We're overriding all internal data, to my knowledge, so this should be
	 * okay
	 */
	public Object clone() {
		try {
			MersenneTwister f = (MersenneTwister) (super.clone());
			f.mt = (int[]) (mt.clone());
			f.mag01 = (int[]) (mag01.clone());
			return f;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		} // should never happen
	}

	public boolean stateEquals(Object o) {
		if (o == this)
			return true;
		if (o == null || !(o instanceof MersenneTwister))
			return false;
		MersenneTwister other = (MersenneTwister) o;
		if (mti != other.mti)
			return false;
		for (int x = 0; x < mag01.length; x++)
			if (mag01[x] != other.mag01[x])
				return false;
		for (int x = 0; x < mt.length; x++)
			if (mt[x] != other.mt[x])
				return false;
		return true;
	}

	/** Reads the entire state of the MersenneTwister RNG from the stream */
	public void readState(DataInputStream stream) throws IOException {
		int len = mt.length;
		for (int x = 0; x < len; x++)
			mt[x] = stream.readInt();

		len = mag01.length;
		for (int x = 0; x < len; x++)
			mag01[x] = stream.readInt();

		mti = stream.readInt();
		__nextNextGaussian = stream.readDouble();
		__haveNextNextGaussian = stream.readBoolean();
	}

	/** Writes the entire state of the MersenneTwister RNG to the stream */
	public void writeState(DataOutputStream stream) throws IOException {
		int len = mt.length;
		for (int x = 0; x < len; x++)
			stream.writeInt(mt[x]);

		len = mag01.length;
		for (int x = 0; x < len; x++)
			stream.writeInt(mag01[x]);

		stream.writeInt(mti);
		stream.writeDouble(__nextNextGaussian);
		stream.writeBoolean(__haveNextNextGaussian);
	}

	/**
	 * Constructor using the default seed.
	 */
	public MersenneTwister() {
		this(System.currentTimeMillis());
	}

	/**
	 * Constructor using a given seed. Though you pass this seed in as a long,
	 * it's best to make sure it's actually an integer.
	 */
	public MersenneTwister(final long seed) {
		super(seed); /* just in case */
		setSeed(seed);
	}

	/**
	 * Constructor using an array of integers as seed. Your array must have a
	 * non-zero length. Only the first 624 integers in the array are used; if
	 * the array is shorter than this then integers are repeatedly used in a
	 * wrap-around fashion.
	 */
	public MersenneTwister(final int[] array) {
		super(System.currentTimeMillis()); /*
											 * pick something at random just in
											 * case
											 */
		setSeed(array);
	}

	/**
	 * Initalize the pseudo random number generator. Don't pass in a long that's
	 * bigger than an int (Mersenne Twister only uses the first 32 bits for its
	 * seed).
	 */

	synchronized public void setSeed(final long seed) {
		// it's always good style to call super
		super.setSeed(seed);

		// Due to a bug in java.util.Random clear up to 1.2, we're
		// doing our own Gaussian variable.
		__haveNextNextGaussian = false;

		mt = new int[N];

		mag01 = new int[2];
		mag01[0] = 0x0;
		mag01[1] = MATRIX_A;

		mt[0] = (int) (seed & 0xffffffff);
		for (mti = 1; mti < N; mti++) {
			mt[mti] = (1812433253 * (mt[mti - 1] ^ (mt[mti - 1] >>> 30)) + mti);
			/* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
			/* In the previous versions, MSBs of the seed affect */
			/* only MSBs of the array mt[]. */
			/* 2002/01/09 modified by Makoto Matsumoto */
			mt[mti] &= 0xffffffff;
			/* for >32 bit machines */
		}
	}

	/**
	 * Sets the seed of the MersenneTwister using an array of integers. Your
	 * array must have a non-zero length. Only the first 624 integers in the
	 * array are used; if the array is shorter than this then integers are
	 * repeatedly used in a wrap-around fashion.
	 */

	synchronized public void setSeed(final int[] array) {
		if (array.length == 0)
			throw new IllegalArgumentException(
					"Array length must be greater than zero");
		int i, j, k;
		setSeed(19650218);
		i = 1;
		j = 0;
		k = (N > array.length ? N : array.length);
		for (; k != 0; k--) {
			mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1664525))
					+ array[j] + j; /* non linear */
			mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
			i++;
			j++;
			if (i >= N) {
				mt[0] = mt[N - 1];
				i = 1;
			}
			if (j >= array.length)
				j = 0;
		}
		for (k = N - 1; k != 0; k--) {
			mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * 1566083941))
					- i; /* non linear */
			mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
			i++;
			if (i >= N) {
				mt[0] = mt[N - 1];
				i = 1;
			}
		}
		mt[0] = 0x80000000; /* MSB is 1; assuring non-zero initial array */
	}

	/**
	 * Returns an integer with <i>bits</i> bits filled with a random number.
	 */
	synchronized protected int next(final int bits) {
		int y;

		if (mti >= N) // generate N words at one time
		{
			int kk;
			final int[] mt = this.mt; // locals are slightly faster
			final int[] mag01 = this.mag01; // locals are slightly faster

			for (kk = 0; kk < N - M; kk++) {
				y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
				mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
			}
			for (; kk < N - 1; kk++) {
				y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
				mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
			}
			y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
			mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

			mti = 0;
		}

		y = mt[mti++];
		y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
		y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
		y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
		y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

		return y >>> (32 - bits); // hope that's right!
	}

	/*
	 * If you've got a truly old version of Java, you can omit these two next
	 * methods.
	 */

	private synchronized void writeObject(final ObjectOutputStream out)
			throws IOException {
		// just so we're synchronized.
		out.defaultWriteObject();
	}

	private synchronized void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		// just so we're synchronized.
		in.defaultReadObject();
	}

	/**
	 * This method is missing from jdk 1.0.x and below. JDK 1.1 includes this
	 * for us, but what the heck.
	 */
	public boolean nextBoolean() {
		return next(1) != 0;
	}

	/**
	 * This generates a coin flip with a probability <tt>probability</tt> of
	 * returning true, else returning false. <tt>probability</tt> must be
	 * between 0.0 and 1.0, inclusive. Not as precise a random real event as
	 * nextBoolean(double), but twice as fast. To explicitly use this, remember
	 * you may need to cast to float first.
	 */

	public boolean nextBoolean(final float probability) {
		if (probability < 0.0f || probability > 1.0f)
			throw new IllegalArgumentException(
					"probability must be between 0.0 and 1.0 inclusive.");
		if (probability == 0.0f)
			return false; // fix half-open issues
		else if (probability == 1.0f)
			return true; // fix half-open issues
		return nextFloat() < probability;
	}

	/**
	 * This generates a coin flip with a probability <tt>probability</tt> of
	 * returning true, else returning false. <tt>probability</tt> must be
	 * between 0.0 and 1.0, inclusive.
	 */

	public boolean nextBoolean(final double probability) {
		if (probability < 0.0 || probability > 1.0)
			throw new IllegalArgumentException(
					"probability must be between 0.0 and 1.0 inclusive.");
		if (probability == 0.0)
			return false; // fix half-open issues
		else if (probability == 1.0)
			return true; // fix half-open issues
		return nextDouble() < probability;
	}

	/**
	 * This method is missing from JDK 1.1 and below. JDK 1.2 includes this for
	 * us, but what the heck.
	 */

	public int nextInt(final int n) {
		if (n <= 0)
			throw new IllegalArgumentException("n must be positive, got: " + n);

		if ((n & -n) == n)
			return (int) ((n * (long) next(31)) >> 31);

		int bits, val;
		do {
			bits = next(31);
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}

	/**
	 * This method is for completness' sake. Returns a long drawn uniformly from
	 * 0 to n-1. Suffice it to say, n must be > 0, or an
	 * IllegalArgumentException is raised.
	 */

	public long nextLong(final long n) {
		if (n <= 0)
			throw new IllegalArgumentException("n must be positive, got: " + n);

		long bits, val;
		do {
			bits = (nextLong() >>> 1);
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}

	/**
	 * A bug fix for versions of JDK 1.1 and below. JDK 1.2 fixes this for us,
	 * but what the heck.
	 */
	public double nextDouble() {
		return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
	}

	/**
	 * A bug fix for versions of JDK 1.1 and below. JDK 1.2 fixes this for us,
	 * but what the heck.
	 */

	public float nextFloat() {
		return next(24) / ((float) (1 << 24));
	}

	/**
	 * A bug fix for all versions of the JDK. The JDK appears to use all four
	 * bytes in an integer as independent byte values! Totally wrong. I've
	 * submitted a bug report.
	 */

	public void nextBytes(final byte[] bytes) {
		for (int x = 0; x < bytes.length; x++)
			bytes[x] = (byte) next(8);
	}

	/** For completeness' sake, though it's not in java.util.Random. */

	public char nextChar() {
		// chars are 16-bit UniCode values
		return (char) (next(16));
	}

	/** For completeness' sake, though it's not in java.util.Random. */

	public short nextShort() {
		return (short) (next(16));
	}

	/** For completeness' sake, though it's not in java.util.Random. */

	public byte nextByte() {
		return (byte) (next(8));
	}

	/**
	 * A bug fix for all JDK code including 1.2. nextGaussian can theoretically
	 * ask for the log of 0 and divide it by 0! See Java bug <a href=
	 * "http://developer.java.sun.com/developer/bugParade/bugs/4254501.html">
	 * http://developer.java.sun.com/developer/bugParade/bugs/4254501.html</a>
	 */

	synchronized public double nextGaussian() {
		if (__haveNextNextGaussian) {
			__haveNextNextGaussian = false;
			return __nextNextGaussian;
		} else {
			double v1, v2, s;
			do {
				v1 = 2 * nextDouble() - 1; // between -1.0 and 1.0
				v2 = 2 * nextDouble() - 1; // between -1.0 and 1.0
				s = v1 * v1 + v2 * v2;
			} while (s >= 1 || s == 0);
			double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
			__nextNextGaussian = v2 * multiplier;
			__haveNextNextGaussian = true;
			return v1 * multiplier;
		}
	}

}
