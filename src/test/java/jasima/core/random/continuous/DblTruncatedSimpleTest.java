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

import static jasima.core.random.continuous.DblTruncatedSimple.valueInRange;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.notANumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DblTruncatedSimpleTest {

	@Test
	public void normalOperation1() {
		assertEquals("in range", valueInRange(4.0, 3.0, 5.0), 4.0, 1e-6);
		assertEquals("below min", valueInRange(1.0, 3.0, 5.0), 3.0, 1e-6);
		assertEquals("above max", valueInRange(6.0, 3.0, 5.0), 5.0, 1e-6);
	}

	@Test(expected = IllegalArgumentException.class)
	public void minSmallerThanMax() {
		valueInRange(4.0, 5.0, 3.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void minSmallerThanMaxInfinite() {
		valueInRange(4.0, POSITIVE_INFINITY, NEGATIVE_INFINITY);
	}

	@Test
	public void nanValues() {
		assertThat("value is NaN", valueInRange(NaN, 3.0, 5.0), notANumber());
		assertEquals("min is NaN", valueInRange(1.0, NaN, 5.0), 1.0, 1e-6);
		assertEquals("max is NaN", valueInRange(6.0, 3.0, NaN), 6.0, 1e-6);
		assertEquals("min and max are NaN", valueInRange(6.0, NaN, NaN), 6.0, 1e-6);
		assertThat("all NaN", valueInRange(NaN, NaN, NaN), notANumber());
	}

	@Test
	public void infValues() {
		assertThat("Inf range", valueInRange(5.0, NEGATIVE_INFINITY, POSITIVE_INFINITY), closeTo(5.0, 1e-6));
		assertThat("value is Inf", valueInRange(POSITIVE_INFINITY, 3.0, 5.0), closeTo(5.0, 1e-6));
		assertThat("value is -Inf", valueInRange(NEGATIVE_INFINITY, 3.0, 5.0), closeTo(3.0, 1e-6));

		assertEquals("value is Inf, no upper limit", valueInRange(POSITIVE_INFINITY, 3.0, NaN), POSITIVE_INFINITY, 1.0);
		assertEquals("value is -Inf, no lower limit", valueInRange(NEGATIVE_INFINITY, NaN, 5.0), NEGATIVE_INFINITY,
				1.0);
	}

}
