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
package jasima.core.experiment;

import jasima.core.util.MersenneTwister;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

/**
 * <p>
 * Allows to run a certain maximum number of experiments chosen randomly. This
 * number of experiment runs is usually smaller than the number of possible
 * factor combinations, i.e., only a fraction of possible designs is tested.
 * </p>
 * <p>
 * The implementation of this class ensures, that each value of a factor occurs
 * equally often (there are no guarantees about value combinations, however, or
 * if a {@link ConfigurationValidator} is used).
 * </p>
 * 
 * @author Torsten Hildebrandt, 2012-06-08
 * @version 
 *          "$Id$"
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

		int max = getMaxConfigurations() > 0 ? getMaxConfigurations()
				: Integer.MAX_VALUE;
		long numCfgsToCreate = Math.min(max, total);

		print("creating %d configurations out of %d possible...",
				numCfgsToCreate, total);
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
