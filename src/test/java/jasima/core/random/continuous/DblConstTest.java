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
package jasima.core.random.continuous;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsNaN.notANumber;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import jasima.core.util.Pair;

public class DblConstTest {

	private static final double[] TEST_VALUES = new double[] { 1.0, 3.0, 2.0, 4.0 };

	@Test
	public void shouldReturnNumbersInSequenceOnce() {
		DblConst dblConst = new DblConst(TEST_VALUES);
		assertArrayEquals(TEST_VALUES, dblConst.getValues(), 1e-6);
		assertEquals(false, dblConst.isRandomizeOrder());
		assertEquals(1.0, dblConst.nextDbl(), 1e-6);
		assertEquals(3.0, dblConst.nextDbl(), 1e-6);
		assertEquals(2.0, dblConst.nextDbl(), 1e-6);
		assertEquals(4.0, dblConst.nextDbl(), 1e-6);
	}

	@Test
	public void shouldReturnNumbersRepeatedly() {
		DblConst dblConst = new DblConst(TEST_VALUES);
		for (int n = 0; n < 3; n++) {
			assertEquals(1.0, dblConst.nextDbl(), 1e-6);
			assertEquals(3.0, dblConst.nextDbl(), 1e-6);
			assertEquals(2.0, dblConst.nextDbl(), 1e-6);
			assertEquals(4.0, dblConst.nextDbl(), 1e-6);
		}
	}

	@Test
	public void shouldReturnNumericalValues() {
		DblConst dblConst = new DblConst(TEST_VALUES);
		assertEquals(2.5, dblConst.getNumericalMean(), 1e-6);
		assertEquals(1.0, dblConst.min(), 1e-6);
		assertEquals(4.0, dblConst.max(), 1e-6);
		assertEquals(new Pair<Double, Double>(1.0, 4.0), dblConst.getValueRange());
		// second call returns cached value
		assertEquals(2.5, dblConst.getNumericalMean(), 1e-6);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNpeIfRandomizationButRandomNotInitialized() {
		DblConst dblConst = new DblConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);

		dblConst.nextDbl(); // raises NPE
	}

	@Test
	public void shouldRandomizeOnce() {
		DblConst dblConst = new DblConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);
		dblConst.setRndGen(new Random(123));

		assertEquals(1.0, dblConst.nextDbl(), 1e-6);
		assertEquals(3.0, dblConst.nextDbl(), 1e-6);
		assertEquals(4.0, dblConst.nextDbl(), 1e-6);
		assertEquals(2.0, dblConst.nextDbl(), 1e-6);

		// values should still be in original order
		assertArrayEquals(TEST_VALUES, dblConst.getValues(), 1e-6);
	}

	@Test
	public void shouldRandomizeTwice() {
		DblConst dblConst = new DblConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);
		dblConst.setRndGen(new Random(123));

		// first iteration
		assertEquals(1.0, dblConst.nextDbl(), 1e-6);
		assertEquals(3.0, dblConst.nextDbl(), 1e-6);
		assertEquals(4.0, dblConst.nextDbl(), 1e-6);
		assertEquals(2.0, dblConst.nextDbl(), 1e-6);

		// second iteration
		assertEquals(4.0, dblConst.nextDbl(), 1e-6);
		assertEquals(2.0, dblConst.nextDbl(), 1e-6);
		assertEquals(1.0, dblConst.nextDbl(), 1e-6);
		assertEquals(3.0, dblConst.nextDbl(), 1e-6);
	}

	@Test
	public void resettingRndGenShouldClearRandomOrder() {
		DblConst dblConst = new DblConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);

		for (int n = 0; n < 3; n++) {
			dblConst.setRndGen(new Random(123));

			assertEquals(1.0, dblConst.nextDbl(), 1e-6);
			assertEquals(3.0, dblConst.nextDbl(), 1e-6);
			assertEquals(4.0, dblConst.nextDbl(), 1e-6);
			assertEquals(2.0, dblConst.nextDbl(), 1e-6);
		}
	}

	@Test
	public void uninitializedDblConstShouldReturnDefinedValues() {
		DblConst dblConst = new DblConst();
		assertArrayEquals(null, dblConst.getValues(), 1e-6);
		assertThat(dblConst.min(), is(notANumber()));
		assertThat(dblConst.max(), is(notANumber()));
		assertThat(dblConst.getNumericalMean(), is(notANumber()));
	}

	@Test
	public void emptyValuesShouldReturnDefinedValues() {
		DblConst dblConst = new DblConst(new double[] {});
		assertArrayEquals(new double[] {}, dblConst.getValues(), 1e-6);
		assertThat(dblConst.min(), is(notANumber()));
		assertThat(dblConst.max(), is(notANumber()));
		assertThat(dblConst.getNumericalMean(), is(notANumber()));
	}

	@Test
	public void shouldBeCloneable() {
		DblConst original = new DblConst(TEST_VALUES);
		DblConst clone = original.clone();

		assertEquals(2.5, clone.getNumericalMean(), 1e-6);
		assertEquals(1.0, clone.min(), 1e-6);
		assertEquals(4.0, clone.max(), 1e-6);
		assertEquals("toString", "DblConst[1.0, 3.0, 2.0, 4.0]", new DblConst(TEST_VALUES).toString());
	}

	@Test
	public void testStringRepresentation() {
		assertEquals("toString", "DblConst[1.0, 3.0, 2.0, 4.0]", new DblConst(TEST_VALUES).toString());
	}

}
