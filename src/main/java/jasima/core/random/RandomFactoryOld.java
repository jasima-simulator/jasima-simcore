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
