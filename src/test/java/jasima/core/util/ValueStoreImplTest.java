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
package jasima.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class ValueStoreImplTest {

	private ValueStore vs;

	@Before
	public void init() {
		vs = new ValueStoreImpl();
	}

	@Test
	public void testPutGet() {
		assertFalse("contains", vs.valueStoreContains("key"));

		Integer putResult = vs.valueStorePut("key", 42);
		assertEquals("putResult", 42, putResult.intValue());

		assertEquals("getValue", 42, vs.valueStoreGet("key"));
		assertTrue("contains", vs.valueStoreContains("key"));
	}

	@Test
	public void testGetUnknownShouldGiveNull() {
		assertFalse("contains", vs.valueStoreContains("key"));
		assertNull("getResult", vs.valueStoreGet("key"));
	}

	@Test
	public void testGetDefaultUnknown() {
		assertEquals("getResultDefault", 23, vs.valueStoreGet("key", 23));
		assertFalse("contains", vs.valueStoreContains("key"));
	}

	@Test
	public void testGetDefaultExisting() {
		vs.valueStorePut("key", 42);
		assertEquals("getResultDefault", 42, vs.valueStoreGet("key", 23));
	}

	@Test
	public void testKeySet() {
		vs.valueStorePut("key", 42);

		assertEquals("keySet", new HashSet<>(Arrays.asList("key")), vs.valueStoreGetAllKeys());
		assertEquals("numKeys", 1, vs.valueStoreGetNumKeys());
	}

	@Test(expected = NullPointerException.class)
	public void testPutNullShouldRaiseNPE() {
		vs.valueStorePut("key", null);
	}

	@Test
	public void testRemove() {
		vs.valueStorePut("key", 42);
		assertTrue("contains", vs.valueStoreContains("key"));

		assertEquals("removeResult", 42, vs.valueStoreRemove("key"));
		assertFalse("contains", vs.valueStoreContains("key"));
	}

	@Test
	public void testRemoveUnknown() {
		assertNull("removeResult", vs.valueStoreRemove("key"));
	}

	@Test
	public void testUpdateExistingValue() {
		vs.valueStorePut("key", 42);
		Object updateResult = vs.valueStoreUpdate("key", n -> (Integer) n + 1);
		assertEquals("updateResult", 43, updateResult);
		assertEquals("getValue", 43, vs.valueStoreGet("key"));
	}

	@Test
	public void testUpdateUnkownValue() {
		Object updateResult = vs.valueStoreUpdate("key", n -> n == null ? 42 : 23);
		assertEquals("getValue", 42, vs.valueStoreGet("key"));
		assertEquals("updateResult", 42, updateResult);
	}

	@Test
	public void testRemoveWithUpdate() {
		vs.valueStorePut("key", 42);
		Object updateResult = vs.valueStoreUpdate("key", n -> null);
		assertNull("getValue", vs.valueStoreGet("key"));
		assertFalse("contains", vs.valueStoreContains("key"));
		assertNull("updateResult", updateResult);
	}

	@Test
	public void testRemoveUnknownWithUpdate() {
		Object updateResult = vs.valueStoreUpdate("key", n -> null);
		assertNull("updateResult", updateResult);
	}

	@Test
	public void testGetImplShouldReturnThis() {
		assertEquals("valueStoreImpl", vs, vs.valueStoreImpl());
	}

	@Test
	public void testCopyExisting() {
		vs.valueStorePut("key", 42);
		ValueStoreImpl other = new ValueStoreImpl();

		boolean copyRes = ValueStore.copy(vs, other, "key");

		assertEquals("copiedValue", 42, other.valueStoreGet("key"));
		assertTrue("copyRes", copyRes);
	}

	@Test
	public void testCopyUnknown() {
		vs.valueStorePut("key", 42);
		ValueStoreImpl other = new ValueStoreImpl();

		boolean copyRes = ValueStore.copy(vs, other, "key2");

		assertNull("copiedValue", other.valueStoreGet("key2"));
		assertFalse("copyRes", copyRes);
	}

	@Test
	public void testCloning() {
		vs.valueStorePut("key", 42);

		ValueStoreImpl other = ((ValueStoreImpl) vs).clone();

		assertEquals("copiedValue", 42, other.valueStoreGet("key"));
	}

	@Test
	public void testCloneIsIndependentFromParent() {
		vs.valueStorePut("key", 42);

		ValueStoreImpl other = ((ValueStoreImpl) vs).clone();
		vs.valueStoreRemove("key");

		assertNull("original", vs.valueStoreGet("key"));
		assertEquals("copiedValue", 42, other.valueStoreGet("key"));
	}

}
