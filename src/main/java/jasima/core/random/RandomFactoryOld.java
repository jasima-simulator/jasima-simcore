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

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Use {@link RandomFactory} instead.
 * 
 * @author Torsten Hildebrandt
 * @deprecated Use {@link RandomFactory} instead.
 */
@Deprecated
public class RandomFactoryOld extends RandomFactory {

	private static final long serialVersionUID = 8632388217395455449L;

	private static final int SEED_TAB_SIZE = 509;

	private Random seedStream = new Random(5787905968364136369L);
	private long[] seeds = null;

	public RandomFactoryOld() {
		super();
	}

	@Override
	protected long getSeed(final String name, @Nullable Consumer<String> warningReceiver) {
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
			if (warningReceiver != null) {
				String warningMsg = defFormat(
						"Collision for random stream name '%s', if possible use unique stream names to avoid problems with comparability/reproducability of results.",
						name);
				warningReceiver.accept(warningMsg);
			}
			res = seedStream.nextLong();
		}

		return res;
	}

	public void setSeed(long seed) {
		seedStream.setSeed(seed);
		seeds = null;
	}

}
