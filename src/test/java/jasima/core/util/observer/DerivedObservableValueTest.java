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
package jasima.core.util.observer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DerivedObservableValueTest {

	private Boolean didNotify = null;

	@Test
	public void testNoCallbacks() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);
		ObservableValue<Integer> v2 = new ObservableValue<Integer>(2);

		ObservableValue<Boolean> eq = ObservableValues.isEqual(v1, v2);
		assertFalse(eq.get());

		v1.set(2);
		assertTrue(eq.get());
	}

	@Test
	public void testCallbacks() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);
		ObservableValue<Integer> v2 = new ObservableValue<Integer>(2);

		ObservableValue<Boolean> eq = ObservableValues.isEqual(v1, v2);
		assertFalse(eq.get());

		eq.addListener((obs, e) -> didNotify = true);
		assertTrue(didNotify == null);

		v1.set(2);
		assertTrue(didNotify);
		assertTrue(eq.get());
	}

	@Test
	public void testCallbacksImmediatelyTrue() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);
		ObservableValue<Integer> v2 = new ObservableValue<Integer>(2);

		ObservableValue<Boolean> eq = ObservableValues.isEqual(v1, v2);
		assertFalse(eq.get());

		v1.set(2);
		assertTrue(eq.get());

		eq.addListener((obs, e) -> didNotify = true);
		assertTrue(didNotify == null);

		v1.set(2); // same value set again, no set triggered
		assertTrue(didNotify == null);

		v1.set(1);
		assertTrue(didNotify);
		assertFalse(eq.get());
	}

	@Test
	public void testAnd() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);
		ObservableValue<Integer> v2 = new ObservableValue<Integer>(2);

		ObservableValue<Boolean> eq1 = ObservableValues.isEqual(v1, 2);
		ObservableValue<Boolean> eq2 = ObservableValues.isEqual(v2, 1);

		ObservableValue<Boolean> and1 = ObservableValues.and(eq1, eq2);

		assertFalse(eq1.get());
		assertFalse(eq2.get());
		assertFalse(and1.get());

		and1.addListener((obs, e) -> didNotify = true);
		assertTrue(didNotify == null);

		v1.set(2);
		assertTrue(didNotify);
		didNotify = null;

		assertTrue(and1.isStale());

		v2.set(1);
		// no notification here because 'and1' is already marked stale
		assertTrue(didNotify == null); 
		
		assertTrue(eq1.get());
		assertTrue(eq2.get());
		assertTrue(and1.get());

		assertFalse(and1.isStale());
	}

	@Test
	public void testOr() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);
		ObservableValue<Integer> v2 = new ObservableValue<Integer>(2);

		ObservableValue<Boolean> eq1 = ObservableValues.isEqual(v1, 2);
		ObservableValue<Boolean> eq2 = ObservableValues.isEqual(v2, 1);

		ObservableValue<Boolean> or1 = ObservableValues.or(eq1, eq2);

		assertFalse(eq1.get());
		assertFalse(eq2.get());
		assertFalse(or1.get());

		or1.addListener((obs, e) -> didNotify = true);
		assertTrue(didNotify == null);

		v2.set(1);
		assertTrue(didNotify);
		didNotify = null;

		assertFalse(eq1.get());
		assertTrue(eq2.get());
		assertTrue(or1.get());

		v2.set(2);
		assertTrue(didNotify);
		didNotify = null;

		assertFalse(eq1.get());
		assertFalse(eq2.get());
		assertFalse(or1.get());

		v1.set(2);
		assertTrue(didNotify);
		didNotify = null;

		assertTrue(eq1.get());
		assertFalse(eq2.get());
		assertTrue(or1.get());
	}

	@Test
	public void testNot() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);

		ObservableValue<Boolean> eq1 = ObservableValues.isEqual(v1, 2);
		ObservableValue<Boolean> not1 = ObservableValues.not(eq1);

		assertFalse(eq1.get());
		assertTrue(not1.get());

		not1.addListener((obs, e) -> didNotify = true);
		assertTrue(didNotify == null);

		v1.set(2);
		assertTrue(didNotify);
		didNotify = null;

		assertTrue(eq1.get());
		assertFalse(not1.get());
	}

}
