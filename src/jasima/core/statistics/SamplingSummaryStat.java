/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
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
 *******************************************************************************/
package jasima.core.statistics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * This class provides efficient sampling of up to a certain maximum number of
 * values. A SummaryStat-object provides an efficient means to collect basic
 * summary statistics like mean, min, max, standard deviation. To do so it does
 * not have to store any observations. Obtaining information like median, or
 * percentile requires storing values, however. To limit the amount of data
 * which has to be stored, sampling can be used and the aforementioned
 * statistics estimated based on this sample.
 * <p />
 * This class can sample and store up to a certain maximum number of values.
 * Each value passed to a call of {@link #value(double)} has the same
 * probability of ending up in the final sample accessible by {@link #getData()}.
 * <p />
 * Which values are selected is determined by a random number generator (see
 * {@link #setRnd(Random)}). If no such random number generator is set,
 * {@link Math#random()} is used.
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>, 2012-07-06
 * @version $Id$
 */
public class SamplingSummaryStat extends SummaryStat {

	private static final int DEF_NUM_SAMPLES = 100;

	private static final long serialVersionUID = 868112468971365852L;

	private int numSamples;
	private Random rnd;

	private PriorityQueue<DatEntry> data;
	private double threshold;

	public SamplingSummaryStat() {
		this(null, DEF_NUM_SAMPLES, null);
	}

	public SamplingSummaryStat(String name) {
		this(name, DEF_NUM_SAMPLES, null);
	}

	public SamplingSummaryStat(int numSamples) {
		this(null, numSamples, null);
	}

	public SamplingSummaryStat(Random rnd) {
		this(null, DEF_NUM_SAMPLES, rnd);
	}

	public SamplingSummaryStat(int numSamples, Random rnd) {
		this(null, numSamples, rnd);
	}

	public SamplingSummaryStat(String name, int numSamples, Random rnd) {
		super(name);
		setNumSamples(numSamples);
		setRnd(rnd);
	}

	@Override
	public void value(double v, double weight) {
		throw new UnsupportedOperationException("Can't handle weights.");
	}

	@Override
	public void value(double v) {
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
			throw new IllegalArgumentException("numSamples must be positive. "
					+ numSamples);
		this.numSamples = numSamples;
	}

	public int getNumSamples() {
		return numSamples;
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
