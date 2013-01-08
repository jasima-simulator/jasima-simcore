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

import jasima.core.simulation.Simulation.SimMsgCategory;

import java.util.Random;

/**
 * Use {@link RandomFactory} instead.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version "$Id$"
 */
@Deprecated
public class RandomFactoryOld extends RandomFactory {

	private static final long serialVersionUID = 8632388217395455449L;

	private static final int SEED_TAB_SIZE = 509;

	private Random seedStream = new Random(5787905968364136369L);
	private long[] seeds = null;

	protected RandomFactoryOld() {
		super();
	}

	@Override
	protected long getSeed(final String name) {
		if (seeds == null) {
			seeds = new long[SEED_TAB_SIZE];
			for (int i = 0; i < seeds.length; i++) {
				do {
					seeds[i] = seedStream.nextLong();
				} while (seeds[i] == 0);
			}
		}

		int idx = Math.abs(name.hashCode() % seeds.length);

		long res = seeds[idx];
		seeds[idx] = 0;

		if (res == 0) {
			if (getSim() != null)
				getSim().print(
						SimMsgCategory.WARN,
						"Collision for random stream name '"
								+ name
								+ "', if possible use unique stream names to avoid problems with comparability/reproducability of results.");
			res = seedStream.nextLong();
		}

		return res;
	}

	public void setSeed(long seed) {
		seedStream.setSeed(seed);
		seeds = null;
	}

}
