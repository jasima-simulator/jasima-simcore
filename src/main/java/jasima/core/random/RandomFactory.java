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
package jasima.core.random;

import static jasima.core.util.TypeUtil.getClassFromSystemProperty;
import static jasima.core.util.i18n.I18n.defFormat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import jasima.core.experiment.Experiment;
import jasima.core.random.continuous.DblSequence;
import jasima.core.simulation.Simulation;
import jasima.core.util.MersenneTwister;
import jasima.core.util.MsgCategory;
import jasima.core.util.TypeUtil;

/**
 * This class provides functionality to create (independent) random number
 * streams. These streams are dependent on a base seed ({@link #setSeed(long)})
 * and a stream name (provided as a parameter to {@link #createInstance(String)}
 * ).
 * <p>
 * Behaviour of this class can be modified in two ways using system properties.
 * <ol>
 * <li>A property {@link #RANDOM_FACTORY_PROP_KEY} can be used to change the
 * class returned by the static method {@link #newInstance()}.
 * <li>If just a different implementation of {@link java.util.Random} is desired
 * (default is {@link MersenneTwister}), use the system property
 * {@link #RANDOM_CLASS_PROP_KEY}.
 * </ol>
 * 
 * @author Torsten Hildebrandt
 */
public class RandomFactory implements Serializable {

	private static final long serialVersionUID = 4828925858942593527L;

	public static final String RANDOM_FACTORY_PROP_KEY = RandomFactory.class.getName();
	public static Class<? extends RandomFactory> randomFactoryClass = getClassFromSystemProperty(
			RANDOM_FACTORY_PROP_KEY, RandomFactory.class, RandomFactory.class);

	public static final String RANDOM_CLASS_PROP_KEY = RandomFactory.class.getName() + ".randomClass";
	public static Class<? extends Random> randomClass = getClassFromSystemProperty(RANDOM_CLASS_PROP_KEY, Random.class,
			MersenneTwister.class);

	/**
	 * Factory method to create a new instance of {@code RandomFactory}. The default
	 * implementation is to return a new instance of {@code RandomFactory}, but the
	 * class created can be customized with the system property
	 * "jasima.core.random.RandomFactory".
	 * 
	 * @return A new {@code RandomFactory} instance.
	 */
	public static RandomFactory newInstance() {
		RandomFactory f = TypeUtil.createInstance(randomFactoryClass);
		f.setSeed(Experiment.DEFAULT_SEED);
		return f;
	}

	private HashMap<Long, String> seeds = new HashMap<Long, String>();
	private Random seedStream = new Random();
	private long hashMask = 5787905968364136369L;
	private Simulation sim;

	/**
	 * This constructor is usually not called directly, use static factory method
	 * {@link #newInstance()} instead.
	 */
	public RandomFactory() {
		super();
	}

	/**
	 * Convenience constructor when creating instances directly.
	 */
	public RandomFactory(Simulation sim, long initialSeed) {
		this();
		setSim(sim);
		setSeed(initialSeed);
	}

	/**
	 * Create a new random instance. The seed of this new instance (and hence the
	 * stream of pseudo-random numbers) is determined by the given {@code name} and
	 * the seed of the {@code RandomFactory}.
	 * 
	 * @param name A unique name of the Random instance (indirectly setting its
	 *             seed).
	 * @return The new {@link Random} instance.
	 */
	public Random createInstance(final String name) {
		Consumer<String> warningReceiver = getSim() == null ? null : msg -> getSim().print(MsgCategory.WARN, msg);
		return createInstance(name, warningReceiver);
	}

	/**
	 * Create a new random instance. The seed of this new instance (and hence the
	 * stream of pseudo-random numbers) is determined by the given {@code name} and
	 * the seed of the {@code RandomFactory}.
	 * 
	 * @param name            A unique name of the Random instance (indirectly
	 *                        setting its seed).
	 * @param warningReceiver used to issue a warning message in case of a hash
	 *                        collision
	 * @return The new {@link Random} instance.
	 */
	public Random createInstance(final String name, @Nullable Consumer<String> warningReceiver) {
		long seed = getSeed(name, warningReceiver);

		if (getSim() != null) {
			getSim().print(MsgCategory.DEBUG,
					defFormat("created random stream '%s' with initial seed %d.", name, seed));
			if (getSim().isTraceEnabled()) {
				getSim().trace("create_random_stream", name, seed);
			}
		}
		return createRandom(seed);
	}

	/**
	 * Create a new {@link Random} instance with the given seed. The concrete class
	 * instantiated is determined by a system property
	 * "jasima.core.random.RandomFactory.randomClass" (default:
	 * {@link MersenneTwister}).
	 * 
	 * @param seed The seed for the new {@link Random} instance.
	 * @return A new {@link Random} instance initialized with the given seed.
	 */
	protected Random createRandom(long seed) {
		Random o = TypeUtil.createInstance(randomClass);
		o.setSeed(seed);
		return o;
	}

	/**
	 * Compute a (hopefully unique) seed which only depends on 'name' and this
	 * RandomFactory's seed.
	 * 
	 * @param warningReceiver used to issue a warning message in case of a hash
	 *                        collision
	 */
	protected long getSeed(final String name, @Nullable Consumer<String> warningReceiver) {
		int hashCode = name.hashCode();
		// extend hash to 64 bit
		long nameHash = ((long) hashCode) << 32 | (~hashCode);

		seedStream.setSeed(nameHash ^ hashMask);
		long seed = seedStream.nextLong();

		// seed already used?
		String s;
		while ((s = seeds.get(seed)) != null) {
			if (s.equals(name))
				throw new IllegalArgumentException("Already created stream '" + name + "', please use unique names.");

			if (warningReceiver != null) {
				String warningMsg = defFormat(
						"Collision for random streams named '%s' and '%s'. If possible use different stream names to avoid problems with comparability/reproducability of results.",
						name, s);
				warningReceiver.accept(warningMsg);
			}

			seed = seedStream.nextLong();
		}

		seeds.put(seed, name);

		return seed;
	}

	/**
	 * Sets the seed that is used to initialize all random number streams.
	 * 
	 * @param seed The seed to use.
	 */
	public void setSeed(long seed) {
		hashMask = new Random(seed).nextLong();
		seeds.clear();
	}

	/**
	 * Initializes the random number generator of a DblStream if it is not already
	 * set. As name of the stream {@link DblSequence#getName()} is used. If it is
	 * {@code null}, {@code defaultName} is used instead.
	 * 
	 * @param stream      The stream to initialize.
	 * @param defaultName Default name if {@code stream} provides no useful
	 *                    information.
	 * @return The same as {@code stream}.
	 */
	public <T extends DblSequence> T initRndGen(T stream, String defaultName) {
		if (stream == null || stream.getRndGen() != null)
			return stream;

		String name = stream.getName();
		if (name == null)
			name = defaultName;

		if (name == null) {
			throw new IllegalArgumentException("No stream name provided.");
		}

		stream.setRndGen(createInstance(name));

		stream.init();

		return stream;
	}

	/**
	 * Initializes the random number generator of a DblStream if it is not already
	 * set using the stream's name. This method is the same as
	 * {@link #initRndGen(DblSequence, String)}, just without a default name.
	 * 
	 * @param stream The {@link DblSequence} to configure.
	 * @return The stream with random number generator initialized.
	 */
	public DblSequence initRndGen(DblSequence stream) {
		return initRndGen(stream, null);
	}

	public Simulation getSim() {
		return sim;
	}

	/**
	 * Sets the simulation this random factory is currently used with.
	 */
	public void setSim(Simulation sim) {
		this.sim = sim;
	}

	/** Used during tests to change implementation classes while JVM is running. */
	public static void reloadSysProps() {
		randomFactoryClass = getClassFromSystemProperty(RANDOM_FACTORY_PROP_KEY, RandomFactory.class,
				RandomFactory.class);
		randomClass = getClassFromSystemProperty(RANDOM_CLASS_PROP_KEY, Random.class, MersenneTwister.class);
	}

}
