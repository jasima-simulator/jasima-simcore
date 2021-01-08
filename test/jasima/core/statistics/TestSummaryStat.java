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
package jasima.core.statistics;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notANumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Test;

public class TestSummaryStat {

	@Test
	public void testPathological1() {
		// this method will fail using the numerically unstable version to
		// compute the variance
		SummaryStat s = new SummaryStat();
		SummaryStat s2 = new SummaryStat();

		Random rnd = new Random(23);

		for (int i = 0; i < 1000000; i++) {
			double d = rnd.nextDouble() * 1e9;

			s.value(d, 1);
			s.value(d, 3);
			s.value(d, 2);

			s2.value(d, 6);
		}

		assertEquals(s.numObs() / 3, s2.numObs());
		assertEquals(s.weightSum(), s2.weightSum(), 1e-6);
		assertEquals(s.sum(), s2.sum(), 1000.0);
		assertEquals(s.mean(), s2.mean(), 1e-4);
		assertEquals(s.variance(), s2.variance(), 10000.0);
	}

	@Test
	public void testPathological2() {
		// this method will fail using the numerically unstable version to
		// compute the variance
		SummaryStat s = new SummaryStat();
		s.values(1e9 + 4, 1e9 + 7, 1e9 + 13, 1e9 + 16);

		assertEquals(4, s.numObs());
		assertEquals(4.0, s.weightSum(), 1e-6);
		assertEquals(4e9 + 40, s.sum(), 1e-6);
		assertEquals(1e9 + 10, s.mean(), 1e-4);
		assertEquals(30.0, s.variance(), 1e-6);
	}

	@Test
	public void testPathological3() {
		// this method will fail using the numerically unstable version to
		// compute the variance
		SummaryStat s = new SummaryStat();
		s.values(1e9 + 4, 1e9 + 7, 1e9 + 13, 1e9 + 16, 1e9 + 4, 1e9 + 7, 1e9 + 13, 1e9 + 16);

		assertEquals(8, s.numObs());
		assertEquals(8, s.weightSum(), 1e-6);
		assertEquals(1e9 + 10, s.mean(), 1e-4);
		assertEquals(25.71428571, s.variance(), 1e-6);

		SummaryStat s2 = new SummaryStat();
		s2.value(1e9 + 4, 2.0);
		s2.value(1e9 + 7, 2.0);
		s2.value(1e9 + 13, 2.0);
		s2.value(1e9 + 16, 2.0);

		assertEquals(4, s2.numObs(), 4);
		assertEquals(8, s2.weightSum(), 1e-6);
		assertEquals(1e9 + 10, s2.mean(), 1e-4);
		assertEquals(25.71428571, s2.variance(), 1e-6);
	}

	@Test
	public void testWeights1() {
		SummaryStat s = new SummaryStat();
		s.values(2, 2, 4, 5, 5, 5);

		assertEquals(6, s.numObs());
		assertEquals(6, s.weightSum(), 1e-6);
		assertEquals(23.0 / 6.0, s.mean(), 1e-4);
		assertEquals(2.166667, s.variance(), 1e-4);

		SummaryStat s2 = new SummaryStat();
		s2.value(2, 2.0);
		s2.value(4, 1.0);
		s2.value(5, 3.0);

		assertEquals(3, s2.numObs(), 4);
		assertEquals(6, s2.weightSum(), 1e-6);
		assertEquals(23.0 / 6.0, s2.mean(), 1e-4);
		assertEquals(2.166667, s2.variance(), 1e-6);
	}

	@Test
	public void testSimple1() {
		SummaryStat s = new SummaryStat();
		s.values(1.0, 2.0, 3.0, 4.0, 5.0);
		assertEquals(5, s.numObs());
		assertEquals(5.0, s.weightSum(), 1e-10);
		assertEquals(15.0, s.sum(), 1e-10);
		assertEquals(3.0, s.mean(), 1e-10);
		assertEquals(2.5, s.variance(), 1e-10);
	}

	@Test
	public void testSimple2() {
		SummaryStat s = new SummaryStat();
		s.value(2, 5).value(4, 3).value(5, 4);
		assertEquals(3, s.numObs());
		assertEquals(12.0, s.weightSum(), 1e-10);
		assertEquals(42.0, s.sum(), 1e-10);
		assertEquals(42.0 / 12.0, s.mean(), 1e-10);
		assertEquals(1.90909090, s.variance(), 1e-6);
	}

	@Test
	public void testEmpty() {
		SummaryStat s = new SummaryStat();
		assertEquals("numObs", 0, s.numObs());
		assertThat("mean", s.mean(), is(notANumber()));
		assertThat("min", s.min(), is(notANumber()));
		assertThat("max", s.max(), is(notANumber()));
		assertThat("lastValue", s.lastValue(), is(notANumber()));
		assertThat("lastWeight", s.lastWeight(), is(notANumber()));
		assertThat("weightSum", s.weightSum(), is(notANumber()));
		assertThat("variance", s.variance(), is(notANumber()));
	}

	@Test
	public void testSingleValue() {
		SummaryStat s = new SummaryStat();
		s.value(3);
		assertEquals("numObs", 1, s.numObs());
		assertEquals("mean", 3.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 3.0, s.max(), 1e-6);
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 1.0, s.lastWeight(), 1e-6);
		assertEquals("weightSum", 1.0, s.weightSum(), 1e-6);
		assertEquals("variance", 0.0, s.variance(), 1e-6);
	}

	@Test
	public void testSingleValueSmallWeight() {
		SummaryStat s = new SummaryStat();
		s.value(3, 0.1);
		assertEquals("numObs", 1, s.numObs());
		assertEquals("mean", 3.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 3.0, s.max(), 1e-6);
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 0.1, s.lastWeight(), 1e-6);
		assertEquals("weightSum", 0.1, s.weightSum(), 1e-6);
	}

	@Test
	public void testSingleValueZeroWeight() {
		SummaryStat s = new SummaryStat();
		s.value(3, 0.0);
		assertEquals("numObs", 1, s.numObs());
		assertEquals("mean", 3.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 3.0, s.max(), 1e-6);
		assertEquals("lastValue", 3.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 0.0, s.lastWeight(), 1e-6);
		assertEquals("weightSum", 0.0, s.weightSum(), 1e-6);
	}

	@Test
	public void testTwoValuesZeroWeight() {
		SummaryStat s = new SummaryStat();
		s.value(3, 0.0);
		s.value(4, 0.0);
		assertEquals("numObs", 2, s.numObs());
		assertEquals("mean", 4.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 4.0, s.max(), 1e-6);
		assertEquals("lastValue", 4.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 0.0, s.lastWeight(), 1e-6);
		assertEquals("weightSum", 0.0, s.weightSum(), 1e-6);
	}

	@Test
	public void testZeroThenSmallWeight() {
		SummaryStat s = new SummaryStat();
		s.value(3, 0.0);
		s.value(4, 0.1);
		assertEquals("numObs", 2, s.numObs());
		assertEquals("mean", 4.0, s.mean(), 1e-6);
		assertEquals("min", 3.0, s.min(), 1e-6);
		assertEquals("max", 4.0, s.max(), 1e-6);
		assertEquals("lastValue", 4.0, s.lastValue(), 1e-6);
		assertEquals("lastWeight", 0.1, s.lastWeight(), 1e-6);
		assertEquals("weightSum", 0.1, s.weightSum(), 1e-6);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSingleValueNegativeWeight() {
		SummaryStat s = new SummaryStat();
		s.value(3, -1.0);
	}

	@Test
	public void testCombine1() throws Exception {
		SummaryStat s1 = new SummaryStat();
		s1.values(2, 2, 4, 5, 5, 5);

		SummaryStat s2 = new SummaryStat();
		s2.value(2, 3.0);
		s2.value(4, 2.0);
		s2.value(5, 1.0);

		SummaryStat manualCombine = new SummaryStat().values(2, 2, 4, 5, 5, 5, 2, 2, 2, 4, 4, 5);
		assertEquals(12, manualCombine.numObs());
		assertEquals(12.0, manualCombine.weightSum(), 1e-10);
		assertEquals(42.0, manualCombine.sum(), 1e-10);
		assertEquals(42.0 / 12.0, manualCombine.mean(), 1e-10);
		assertEquals(1.90909090, manualCombine.variance(), 1e-6);

		SummaryStat cmb1 = s1.clone().combine(s2);
		assertEquals(9, cmb1.numObs());
		assertEquals(manualCombine.sum(), cmb1.sum(), 1e-10);
		assertEquals(manualCombine.mean(), cmb1.mean(), 1e-10);
		assertEquals(manualCombine.variance(), cmb1.variance(), 1e-6);

		SummaryStat cmb2 = new SummaryStat(s2).combine(s1);
		assertEquals(9, cmb2.numObs());
		assertEquals(manualCombine.sum(), cmb2.sum(), 1e-10);
		assertEquals(manualCombine.mean(), cmb2.mean(), 1e-10);
		assertEquals(manualCombine.variance(), cmb2.variance(), 1e-6);

		SummaryStat cmb3 = new SummaryStat().combine(s1).combine(s2);
		assertEquals(9, cmb3.numObs());
		assertEquals(manualCombine.sum(), cmb3.sum(), 1e-10);
		assertEquals(manualCombine.mean(), cmb3.mean(), 1e-10);
		assertEquals(manualCombine.variance(), cmb3.variance(), 1e-6);
	}
}
