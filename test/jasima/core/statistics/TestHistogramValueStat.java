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
package jasima.core.statistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import jasima.core.util.MersenneTwister;

import java.io.PrintStream;

import org.junit.Test;

import util.FileChecker;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id$
 */
public class TestHistogramValueStat {

	@Test
	public void testBasicP2() throws Exception {
		QuantileEstimator basicQE = new QuantileEstimator("BasicTest", 0.5);

		PrintStream pStr = new PrintStream("basicPP.txt");

		double[] init = { 0.02, 0.15, 0.74, 3.39, 0.83 };
		for (double d : init) {
			basicQE.value(d);
			pStr.println(basicQE.getMarkers());
		}
		pStr.println();

		double[] vals = { 22.37, 10.15, 15.43, 38.62, 15.92, 34.60, 10.28,
				1.47, 0.40, 0.05, 11.39, 0.27, 0.42, 0.09, 11.37 };
		for (double d : vals) {
			basicQE.value(d);
			pStr.println(basicQE.getMarkers());
		}
		pStr.close();

		FileChecker.checkFiles("basicPP.txt", "testInstances/basicPP.txt");
	}

	@Test
	public void testQuantileList() throws Exception {
		QuantileEstimator defQE = new QuantileEstimator("ExponentialQuantiles");

		MersenneTwister mt = new MersenneTwister(123456L);

		for (int i = 0; i < 1000000; ++i) {
			// inversion method to create an exponential distribution
			defQE.value(-Math.log(mt.nextDouble() + Double.MIN_NORMAL));
		}

		assertThat(defQE.quantile(0.1), closeTo(Math.log(1 / 0.9), 0.005));
		assertThat(defQE.quantile(0.5), closeTo(Math.log(1 / 0.5), 0.005));
		assertThat(defQE.quantile(0.9), closeTo(Math.log(1 / 0.1), 0.005));
	}

	@Test
	public void testHistogram() throws Exception {
		QuantileEstimator histQE = new QuantileEstimator("HistogramTest");
		histQE.setCellCount(100);
		for (int i = 0; i < 10000; ++i) {
			histQE.value(i / 1000.0);
		}

		for (int i = 0; i < 5000; ++i) {
			histQE.value((i + 0.1) / 1000.0);
			histQE.value((i + 0.2) / 1000.0);
		}

		for (QuantileEstimator.Bar bar : histQE) {
			if (bar.minX > 0.5 && bar.maxX < 4.5) {
				assertThat(bar.height(), closeTo(0.15, 0.01));
			} else if (bar.minX > 5.5 && bar.maxX < 9.5) {
				assertThat(bar.height(), closeTo(0.05, 0.01));
			}
		}

		// Formatter fmt = new Formatter("histogram.txt");
		// valueStat.formatForGnuplot(fmt);
		// fmt.close();
	}
}
