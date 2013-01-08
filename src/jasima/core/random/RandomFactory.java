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
package jasima.core.random;

import jasima.core.random.continuous.DblStream;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimMsgCategory;
import jasima.core.util.MersenneTwister;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

/**
 * This class provides functionality to create (independent) random number
 * streams. These streams are dependent on a base seed ({@link #setSeed(long)})
 * and a stream name (provided as a parameter to {@link #createInstance(String)}
 * ).
 * <p />
 * Behaviour of this class can be modified in two ways using system properties.
 * <ol>
 * <li>A property {@link #RANDOM_FACTORY_PROP_KEY} can be used to change the
 * class returned by the static method {@link #newInstance(Simulation)}.
 * <li>If just a different implementation of {@link java.util.Random} is desired
 * (default is {@link MersenneTwister}), use the system property
 * {@link #RANDOM_CLASS_PROP_KEY}.
 * </ol>
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
public class RandomFactory implements Serializable {
	private static final long serialVersionUID = 4828925858942593527L;

	public static final String RANDOM_FACTORY_PROP_KEY = RandomFactory.class
			.getName();
	public static final String DEFAULT_FACTORY = RandomFactory.class.getName();

	public static RandomFactory newInstance(Simulation s) {
		String factName = System.getProperty(RANDOM_FACTORY_PROP_KEY,
				DEFAULT_FACTORY);

		try {
			Class<?> factClass = Class.forName(factName);
			RandomFactory o = (RandomFactory) factClass.newInstance();
			o.setSim(s);
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static final String RANDOM_CLASS_PROP_KEY = RandomFactory.class
			.getName() + ".randomClass";
	public static final String DEFAULT_RANDOM_CLASS = MersenneTwister.class
			.getName();

	private HashMap<Long, String> seeds = new HashMap<Long, String>();
	private Random seedStream = new Random();
	private long hashMask = 5787905968364136369L;
	private Class<?> randomClass;
	private Simulation sim;

	/**
	 * Don't use this constructor, use static method
	 * {@link #newInstance(Simulation)} instead.
	 */
	protected RandomFactory() {
		super();

		// which Random implementation to use?
		String rndClassName = System.getProperty(RANDOM_CLASS_PROP_KEY,
				DEFAULT_RANDOM_CLASS);
		try {
			randomClass = Class.forName(rndClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public Random createInstance(final String name) {
		long seed = getSeed(name);
		if (getSim() != null)
			getSim().print(
					SimMsgCategory.DEBUG,
					"created random stream '" + name + "' with initial seed "
							+ seed + ".");
		return createRandom(seed);
	}

	protected Random createRandom(long seed) {
		try {
			Random o = (Random) randomClass.newInstance();
			o.setSeed(seed);
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Compute a (hopefully unique) seed which only depends on 'name' and this
	 * RandomFactory's seed.
	 */
	protected long getSeed(final String name) {
		int hashCode = name.hashCode();
		// extend hash to 64 bit
		long l = ((long) hashCode) << 32 | (~hashCode);

		seedStream.setSeed(l ^ hashMask);
		long seed = seedStream.nextLong();

		// seed already used?
		String s;
		while ((s = seeds.get(seed)) != null) {
			if (s.equals(name))
				throw new IllegalArgumentException("Already created stream '"
						+ name + "', please use unique names.");

			if (getSim() != null)
				getSim().print(
						SimMsgCategory.WARN,
						"Collision for random streams named '"
								+ name
								+ "' and '"
								+ s
								+ "'. If possible use different stream names to avoid problems with comparability/reproducability of results.");
			seed = seedStream.nextLong();
		}

		seeds.put(seed, name);

		return seed;
	}

	public void setSeed(long seed) {
		hashMask = new Random(seed).nextLong();
		seeds.clear();
	}

	/**
	 * Initializes the random number generator of a DblStream if it is not
	 * already set. As name of the stream {@link DblStream#getName()} is used.
	 * If it is {@code null}, {@code defaultName} is used instead.
	 * 
	 * @param stream
	 *            The stream to initialize.
	 * @param defaultName
	 *            Default name if {@code stream} provides no useful information.
	 * @return The same as {@code stream}.
	 */
	public DblStream initNumberStream(DblStream stream, String defaultName) {
		if (stream == null || stream.getRndGen() != null)
			return stream;

		String name = stream.getName();
		if (name == null)
			name = defaultName;

		if (name == null) {
			throw new IllegalArgumentException("No stream name provided.");
		}

		stream.setRndGen(createInstance(name));

		return stream;
	}

	/**
	 * Initializes the random number generator of a DblStream if it is not
	 * already set using the streams name. This method is the same as
	 * {@link #initNumberStream(DblStream, String)}, just without a default
	 * name.
	 * 
	 * @param stream
	 *            The {@link DblStream} to configure.
	 * @return The stream with random number generator initialized.
	 */
	public DblStream initNumberStream(DblStream stream) {
		return initNumberStream(stream, null);
	}

	public Simulation getSim() {
		return sim;
	}

	protected void setSim(Simulation sim) {
		this.sim = sim;
	}
}
