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
package jasima.core.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import jasima.core.util.MersenneTwister;

/**
 * Allows to run a certain maximum number of experiments chosen randomly. This
 * number of experiment runs is usually smaller than the number of possible
 * factor combinations, i.e., only a fraction of possible designs is tested.
 * <p>
 * The implementation of this class ensures, that each value of a factor occurs
 * equally often (there are no guarantees about value combinations, however, or
 * if a {@link ConfigurationValidator} is used).
 * 
 * @author Torsten Hildebrandt
 */
public class RandomFractionalExperiment extends FullFactorialExperiment {

	private static final int DEF_MAX_CONFS = 100;

	private static final long serialVersionUID = 6227676813209467282L;

	// fields used during run
	private Random rnd;

	public RandomFractionalExperiment() {
		this(DEF_MAX_CONFS);
	}

	public RandomFractionalExperiment(int numDesigns) {
		super();
		setMaxConfigurations(numDesigns);
	}

	@Override
	public void init() {
		super.init();

		rnd = new MersenneTwister(getInitialSeed());
	}

	@Override
	protected void createExperiments() {
		int numFactors = getFactorNames().size();
		int[] numValuesPerFactor = new int[numFactors];
		long total = 1;

		// calculate totals
		int i = 0;
		for (String name : getFactorNames()) {
			int n = getFactorValues(name).size();
			numValuesPerFactor[i++] = n;
			total *= n;
		}

		int max = getMaxConfigurations() > 0 ? getMaxConfigurations() : Integer.MAX_VALUE;
		long numCfgsToCreate = Math.min(max, total);

		print("creating %d configurations out of %d possible...", numCfgsToCreate, total);
		numConfs = 0;

		sampleConfs((int) numCfgsToCreate, numValuesPerFactor);

		print("%d valid configurations found.", experiments.size());
	}

	private void sampleConfs(int numCfgsToCreate, int[] numValuesPerFactor) {
		int numFactors = getFactorNames().size();
		int[][] confs = new int[numFactors][numCfgsToCreate];
		for (int n = 0; n < numFactors; n++) {
			initConfDim(confs[n], numValuesPerFactor[n]);
		}

		HashSet<IntArrayWrapper> cfgs = new HashSet<IntArrayWrapper>();
		boolean cont = true;
		while (cont) {
			// shuffle all dimensions
			for (int n = 0; n < confs.length; n++) {
				shuffle(confs[n]);
			}

			// create new configurations if possible
			for (int n = confs[0].length - 1; n >= 0; n--) {
				int[] conf = new int[numFactors];
				for (int j = 0; j < conf.length; j++) {
					conf[j] = confs[j][n];
				}

				IntArrayWrapper w = new IntArrayWrapper(conf);
				if (!cfgs.contains(w)) {
					cfgs.add(w);

					if (cfgs.size() == numCfgsToCreate) {
						cont = false;
						break; // for n
					}
				}
			}
		}

		ArrayList<IntArrayWrapper> l = new ArrayList<IntArrayWrapper>(cfgs);
		Collections.sort(l);
		for (IntArrayWrapper iaw : l) {
			addExperimentForConf(iaw.is);
		}
	}

	private static class IntArrayWrapper implements Comparable<IntArrayWrapper> {
		public final int[] is;

		public IntArrayWrapper(int[] is) {
			super();
			this.is = is;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(is);
		}

		@Override
		public boolean equals(Object o) {
			return Arrays.equals(is, ((IntArrayWrapper) o).is);
		}

		@Override
		public int compareTo(IntArrayWrapper o) {
			assert is.length == o.is.length;
			for (int i = 0; i < is.length; i++) {
				int diff = is[i] - o.is[i];
				if (diff != 0)
					return diff;
			}
			return 0;
		}
	}

	private void shuffle(int[] is) {
		int n = is.length;
		for (int i = 0; i < n; i++) {
			int b = i + rnd.nextInt(n - i);

			int x = is[i];
			is[i] = is[b];
			is[b] = x;
		}
	}

	private void initConfDim(int[] is, int numValues) {
		double n = is.length * 1.0 / numValues;
		int m = (int) Math.floor(n);

		// fill integer part
		int p = 0;
		for (int i = 0; i < numValues; i++) {
			for (int j = 0; j < m; j++) {
				is[p++] = i;
			}
		}

		// fill rest, avoiding duplicates
		assert is.length - p < numValues;
		int[] v = new int[numValues];
		for (int i = 0; i < v.length; i++) {
			v[i] = i;
		}
		shuffle(v);

		int q = 0;
		while (p < is.length) {
			is[p++] = v[q++];
		}
	}

}
