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
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;

/**
 * Implements the extended P2-Algorithm. To calculate histograms, median values
 * or arbitrary quantiles. This class also collects all statistical values
 * collected by {@link SummaryStat}.
 * <p />
 * The method used is based on the following papers:
 * <ul>
 * <li>Raj Jain, Imrich Chlamtac: The P2 Algorithm for Dynamic Calculation of
 * Quantiles and Histograms Without Storing Observations, ACM 28, 10 (1985)
 * <li>Kimmo Raatikainen: Simultaneous estimation of several percentiles,
 * Simulations Councils (1987)
 * </ul>
 * 
 * @author Robin Kreis <r.kreis@uni-bremen.de>, 2012-09-07
 * @version $Id$
 */
public class QuantileEstimator extends SummaryStat implements
		Iterable<QuantileEstimator.Bar> {

	private static final long serialVersionUID = 1342062250846464757L;

	/**
	 * Stores the
	 */
	protected double[] p2_q;

	protected int[] p2_n;
	protected double[] p2_n_increment;

	/**
	 * Creates a QuantileEstimator and optimizes the marker positions to
	 * estimates the quantiles 0.1, 0.5 (the median) and 0.9 well.
	 * 
	 * @param name
	 *            the name of this {@link SummaryStat}
	 */
	public QuantileEstimator(String name) {
		super(name);
		p2_n_increment = new double[] { 0, 0.05, 0.1, 0.3, 0.5, 0.7, 0.9, 0.95,
				1 };
		initMarkers();
	}

	/**
	 * Creates a QuantileEstimator and optimizes the marker positions to
	 * estimates the given quantiles well.
	 * 
	 * @see #setQuantileList(double...)
	 * @param name
	 *            the name of this {@link SummaryStat}
	 * @param quantiles
	 *            a list of quantiles to be estimated
	 */
	public QuantileEstimator(String name, double... quantiles) {
		super(name);
		setQuantileList(quantiles);
	}

	/**
	 * Sets a list of quantiles to be estimated. For n quantiles, 2n+3 markers
	 * will be created.
	 */
	public void setQuantileList(double... quantiles) {
		Arrays.sort(quantiles);
		p2_n_increment = new double[quantiles.length * 2 + 3];

		// ~ is the mean of the neighbors, q is quantiles, we need:
		// p2_n_increment = [0, ~, q[0], ~, q[1], ~, q[2], ~, 1]

		// first, fill in all except for the mean
		p2_n_increment[0] = 0.0;
		for (int i = 0; i < quantiles.length; ++i) {
			assert quantiles[i] > 0.0 && quantiles[i] < 1.0;
			// first quantile at 2, one value inbetween:
			p2_n_increment[i * 2 + 2] = quantiles[i];
		}
		p2_n_increment[p2_n_increment.length - 1] = 1.0;

		// then, fill in the values inbetween
		for (int i = 1; i < p2_n_increment.length - 1; i += 2) {
			p2_n_increment[i] = (p2_n_increment[i - 1] + p2_n_increment[i + 1]) / 2;
		}

		initMarkers();
	}

	/**
	 * Returns a list of quantiles that are estimated well.
	 * 
	 * The returned array will be equal to the one passed to
	 * {@link #QuantileEstimator(String, double...)} or
	 * {@link #setQuantileList(double...)}, if the markers haven't been modified
	 * afterwards.
	 */
	public double[] getQuantileList() {
		double[] retVal = new double[(p2_n_increment.length - 3) / 2];
		for (int i = 0; i < retVal.length; ++i) {
			retVal[i] = p2_n_increment[i * 2 + 2];
		}
		return retVal;
	}

	/**
	 * Sets the number of cells of the histogram. Each cell will usually be
	 * plotted as a bar.
	 */
	public void setCellCount(int cells) {
		assert cells >= 2;
		p2_n_increment = new double[cells + 1];
		for (int i = 0; i < p2_n_increment.length; ++i) {
			p2_n_increment[i] = i / (p2_n_increment.length - 1.0);
		}
		initMarkers();
	}

	/**
	 * Returns the number of cells of the histogram. The returned value will be
	 * equal to the one passed to {@link #setCellCount(int)}, if the markers
	 * have't been modified afterwards.
	 */
	public int getCellCount() {
		return p2_q.length - 1;
	}

	@Override
	public void combine(SummaryStat other) {
		throw new RuntimeException(
				"QuantileEstimator.combine not implemented yet");
	}

	@Override
	public void clear() {
		if (numObs() > 0)
			super.clear();
		initMarkers();
	}

	/**
	 * Initializes all markers. This requires {@link #p2_n_increment} to be set.
	 * After this method completes, {@link #p2_n} and {@link #p2_q} will have
	 * the right dimensions and {@link #p2_n} will be initialized. This method
	 * should only be called when {@link #numObs()} would return 0. Otherwise,
	 * {@link #clear()} should be called.
	 */
	protected void initMarkers() {
		assert numObs() == 0;
		if (p2_n_increment != null) {
			if (p2_n == null || p2_n.length != p2_n_increment.length) {
				p2_n = new int[p2_n_increment.length];
			}
			if (p2_q == null || p2_q.length != p2_n_increment.length) {
				p2_q = new double[p2_n_increment.length];
			}
			for (int i = 0; i < p2_n.length; ++i) {
				// should this look at the desired marker pos?
				p2_n[i] = i;
			}
		}
	}

	@Override
	public void value(double v, double weight) {
		super.value(v, weight);
		int obsIdx = numObs() - 1; // first observation: 0

		if (obsIdx < p2_q.length) {
			// initialization
			p2_q[obsIdx] = v;
			if (obsIdx == p2_q.length - 1) {
				// finish initialization
				Arrays.sort(p2_q);
			}
		} else {
			// usual case
			int k = Arrays.binarySearch(p2_q, v);
			if (k < 0) {
				k = -(k + 1);
			}

			if (k == 0) {
				p2_q[0] = v;
				k = 1;
			} else if (k == p2_q.length) {
				k = p2_q.length - 1;
				p2_q[k] = v;
			}

			for (int i = k; i < p2_n.length; ++i) {
				++p2_n[i];
			}

			for (int i = 1; i < p2_q.length - 1; ++i) {
				double n_ = p2_n_increment[i] * obsIdx;
				double di = n_ - p2_n[i];
				if ((di >= 1.0 && p2_n[i + 1] - p2_n[i] > 1)
						|| ((di <= -1.0 && p2_n[i - 1] - p2_n[i] < -1))) {
					int d = di < 0 ? -1 : 1;

					double qi_ = quadPred(d, i);
					if (qi_ < p2_q[i - 1] || qi_ > p2_q[i + 1]) {
						qi_ = linPred(d, i);
					}
					p2_q[i] = qi_;
					p2_n[i] += d;
				}
			}
		}
	}

	protected double quadPred(int d, int i) {
		double qi = p2_q[i];
		double qip1 = p2_q[i + 1];
		double qim1 = p2_q[i - 1];
		int ni = p2_n[i];
		int nip1 = p2_n[i + 1];
		int nim1 = p2_n[i - 1];

		double a = (ni - nim1 + d) * (qip1 - qi) / (nip1 - ni);
		double b = (nip1 - ni - d) * (qi - qim1) / (ni - nim1);
		return qi + (d * (a + b)) / (nip1 - nim1);
	}

	protected double linPred(int d, int i) {
		double qi = p2_q[i];
		double qipd = p2_q[i + d];
		int ni = p2_n[i];
		int nipd = p2_n[i + d];

		return qi + d * (qipd - qi) / (nipd - ni);
	}

	/**
	 * Returns all markers and their positions. Used for testing.
	 * 
	 * @return the current markers formatted as a string
	 */
	public String getMarkers() {
		Formatter fmt = new Formatter(Locale.US);
		fmt.format("%5d", numObs());
		fmt.format(" |");
		for (int i = 0; i < p2_n.length; ++i) {
			fmt.format("%8.2f", 1 + (i * (numObs() - 1)) / (p2_n.length - 1.0));
		}
		fmt.format(" |");
		for (int n : p2_n) {
			fmt.format("%5d", n + 1);
		}
		fmt.format(" |");
		for (double q : p2_q) {
			fmt.format("%8.2f", q);
		}
		String retVal = fmt.toString();
		fmt.close();
		return retVal;
	}

	/**
	 * Can be used to receive a list of {@link Bar} instances to create a
	 * histogram.
	 */
	@Override
	public Iterator<Bar> iterator() {
		return new Iterator<Bar>() {
			private int bar = 0;
			private Bar retVal = new Bar();
			private double div = 1.0 / numObs();

			@Override
			public boolean hasNext() {
				return bar < p2_n.length - 1;
			}

			@Override
			public Bar next() {
				retVal.minX = p2_q[bar];
				retVal.maxX = p2_q[bar + 1];
				retVal.area = (p2_n[bar + 1] - p2_n[bar]) * div;
				++bar;
				return retVal;
			}

			@Override
			public void remove() {
				assert false;
			}
		};
	}

	/**
	 * Formats a histogram so that a bar graph can be plotted. Each line of the
	 * output will represent one bar and have three columns for the middle X
	 * position, height and width of the bar. The area of each bar is the ratio
	 * of all values within the X range of the bar.
	 * 
	 * Gnuplot can directly plot a bar graph using the command
	 * <code>plot <i>filename</i> with boxes</code>.
	 * 
	 * @param fmt
	 *            the formatter to store the output
	 */
	public void formatForGnuplot(Formatter fmt) {
		for (Bar b : this) {
			fmt.format(Locale.US, "%6.4f %6.4f %6.4f%n", (b.minX + b.maxX) / 2,
					b.height(), b.width());
		}
	}

	/**
	 * Estimates a quantile. If there is no marker for the quantile p, linear
	 * interpolation between the two closest markers is performed. If p is NaN,
	 * NaN will be returned. If there haven't been enough observations or the
	 * markers are not initialized, NaN is returned. If <code>p &lt;= 0.0</code>
	 * or <code>p &gt;= 1.0</code>, the minimum or maximum will be returned.
	 * 
	 * @param p
	 *            any number
	 * @return a number that is estimated to be bigger than 100p percent of all
	 *         numbers or Double.NaN, if no data is available
	 */
	public double quantile(double p) {
		if (Double.isNaN(p) || p2_n == null || numObs() < p2_n.length)
			return Double.NaN;
		if (p <= 0.0)
			return p2_q[0];
		if (p >= 1.0)
			return p2_q[p2_q.length - 1];
		int idx = Arrays.binarySearch(p2_n_increment, p);
		if (idx < 0) {
			int left = -idx - 2;
			int right = -idx - 1;
			double pl = p2_n_increment[left];
			double pr = p2_n_increment[right];
			return (p2_q[left] * (pr - p) + p2_q[right] * (p - pl)) / (pr - pl);
		}
		return p2_q[idx];
	}

	@Override
	public String toString() {
		if (p2_q.length > 10) {
			return String.format(
					"[10%%<%f, median: %f, 90%%<%f; %d more markers]",
					quantile(0.1), quantile(0.5), quantile(0.9),
					p2_q.length - 3);
		}
		Formatter fmt = new Formatter();
		fmt.format("[");
		// extract quantiles
		for (int i = 2; i < p2_n_increment.length - 2; i += 2) {
			if (i != 2)
				fmt.format("; ");
			fmt.format("%.0f%%<%f", p2_n_increment[i] * 100, p2_q[i]);
		}
		String retVal = fmt.format("]").toString();
		fmt.close();
		return retVal;
	}

	/**
	 * Represents one bar of a histogram.
	 */
	public class Bar {
		public double minX;
		public double maxX;

		/**
		 * The estimated ratio of observations within {@link #minX} and
		 * {@link #maxX}.
		 */
		public double area;

		public final double height() {
			return area / width();
		}

		public final double width() {
			return maxX - minX;
		}
	}
}
