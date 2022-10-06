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
package jasima.core.statistics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * <p>
 * This class provides efficient sampling of up to a certain maximum number of
 * values. A SummaryStat-object provides an efficient means to collect basic
 * summary statistics like mean, min, max, standard deviation. To do so it does
 * not have to store any observations. Obtaining information like median, or
 * percentile requires storing values, however. To limit the amount of data
 * which has to be stored, sampling can be used and the aforementioned
 * statistics estimated based on this sample.
 * </p>
 * <p>
 * This class can sample and store up to a certain maximum number of values.
 * Each value passed to a call of {@link #value(double)} has the same
 * probability of ending up in the final sample accessible by {@link #getData()}
 * .
 * </p>
 * <p>
 * Which values are selected is determined by a random number generator (see
 * {@link #setRnd(Random)}). If no such random number generator is set,
 * {@link Math#random()} is used.
 * 
 * @author Torsten Hildebrandt, 2012-07-06
 */
public class SamplingSummaryStat extends SummaryStat {

	private static final int DEF_NUM_SAMPLES = 100;

	private static final long serialVersionUID = 868112468971365852L;

	private int numSamples;
	private Random rnd;

	private PriorityQueue<DatEntry> data;
	private double threshold;

	public SamplingSummaryStat() {
		this(DEF_NUM_SAMPLES, null);
	}

	public SamplingSummaryStat(int numSamples) {
		this(numSamples, null);
	}

	public SamplingSummaryStat(Random rnd) {
		this(DEF_NUM_SAMPLES, rnd);
	}

	public SamplingSummaryStat(int numSamples, Random rnd) {
		super();
		setNumSamples(numSamples);
		setRnd(rnd);
	}

	@Override
	public SummaryStat value(double v, double weight) {
		throw new UnsupportedOperationException("Can't handle weights.");
	}

	@Override
	public SamplingSummaryStat value(double v) {
		super.value(v, 1.0d);

		if (data == null)
			init();

		double r = getRnd() == null ? Math.random() : getRnd().nextDouble();
		if (r < threshold || data.size() < getNumSamples()) {
			if (data.size() == getNumSamples())
				data.poll();

			data.offer(new DatEntry(r, numObs(), v));

			threshold = data.peek().rand;
		}

		return this;
	}

	public double[] getData() {
		// bring data in insertion order
		DatEntry[] tmp = data.toArray(new DatEntry[data.size()]);
		Arrays.sort(tmp, new Comparator<DatEntry>() {
			@Override
			public int compare(DatEntry o1, DatEntry o2) {
				if (o1.num < o2.num)
					return -1;
				else if (o1.num == o2.num)
					return 0;
				else
					return +1;
			}
		});

		// copy values to result array
		double[] res = new double[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			res[i] = tmp[i].data;
		}
		return res;
	}

	public void init() {
		data = new PriorityQueue<DatEntry>(getNumSamples());
		threshold = 1.0d;
	}

	public void setRnd(Random rnd) {
		this.rnd = rnd;
	}

	public Random getRnd() {
		return rnd;
	}

	public void setNumSamples(int numSamples) {
		if (numSamples <= 0)
			throw new IllegalArgumentException("numSamples must be positive. " + numSamples);
		this.numSamples = numSamples;
	}

	public int getNumSamples() {
		return numSamples;
	}

	@Override
	public SummaryStat clone() {
		throw new UnsupportedOperationException("clone()");
	}

	/**
	 * Each DatEntry stores a single decision situation plus a random value.
	 */
	protected static class DatEntry implements Comparable<DatEntry> {

		public final double rand;
		public final int num;
		public final double data;

		public DatEntry(double r, int num, double rs) {
			super();
			this.rand = r;
			this.num = num;
			this.data = rs;
		}

		@Override
		public int compareTo(DatEntry o) {
			return Double.compare(o.rand, rand);
		}
	}

}
