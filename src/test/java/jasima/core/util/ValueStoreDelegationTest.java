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

import org.junit.Test;

/**
 * Basically the same as {@link ValueStoreImplTest}, but tests indirectly
 * implementing ValueStore by implementing {@link ValueStore} and providing a
 * {@link #valueStoreImpl()} method. Any class can implement {@link ValueStore}
 * this way.
 */
public class ValueStoreDelegationTest implements ValueStore {

	// this provides the actual implementation
	private ValueStore impl = new ValueStoreImpl();

	@Override
	public ValueStore valueStoreImpl() {
		return impl;
	}

	@Test
	public void testPutGet() {
		assertFalse("contains", valueStoreContains("key"));

		Integer putResult = valueStorePut("key", 42);
		assertEquals("putResult", 42, putResult.intValue());

		assertEquals("getValue", 42, valueStoreGet("key"));
		assertTrue("contains", valueStoreContains("key"));
	}

	@Test
	public void testGetUnknownShouldGiveNull() {
		assertFalse("contains", valueStoreContains("key"));
		assertNull("getResult", valueStoreGet("key"));
	}

	@Test
	public void testGetDefaultUnknown() {
		assertEquals("getResultDefault", 23, valueStoreGet("key", 23));
		assertFalse("contains", valueStoreContains("key"));
	}

	@Test
	public void testGetDefaultExisting() {
		valueStorePut("key", 42);
		assertEquals("getResultDefault", 42, valueStoreGet("key", 23));
	}

	@Test
	public void testKeySet() {
		valueStorePut("key", 42);

		assertEquals("keySet", new HashSet<>(Arrays.asList("key")), valueStoreGetAllKeys());
		assertEquals("numKeys", 1, valueStoreGetNumKeys());
	}

	@Test(expected = NullPointerException.class)
	public void testPutNullShouldRaiseNPE() {
		valueStorePut("key", null);
	}

	@Test
	public void testRemove() {
		valueStorePut("key", 42);
		assertTrue("contains", valueStoreContains("key"));

		assertEquals("removeResult", 42, valueStoreRemove("key"));
		assertFalse("contains", valueStoreContains("key"));
	}

	@Test
	public void testRemoveUnknown() {
		assertNull("removeResult", valueStoreRemove("key"));
	}

	@Test
	public void testUpdateExistingValue() {
		valueStorePut("key", 42);
		Object updateResult = valueStoreUpdate("key", n -> (Integer) n + 1);
		assertEquals("updateResult", 43, updateResult);
		assertEquals("getValue", 43, valueStoreGet("key"));
	}

	@Test
	public void testUpdateUnkownValue() {
		Object updateResult = valueStoreUpdate("key", n -> n == null ? 42 : 23);
		assertEquals("getValue", 42, valueStoreGet("key"));
		assertEquals("updateResult", 42, updateResult);
	}

	@Test
	public void testRemoveWithUpdate() {
		valueStorePut("key", 42);
		Object updateResult = valueStoreUpdate("key", n -> null);
		assertNull("getValue", valueStoreGet("key"));
		assertFalse("contains", valueStoreContains("key"));
		assertNull("updateResult", updateResult);
	}

	@Test
	public void testRemoveUnknownWithUpdate() {
		Object updateResult = valueStoreUpdate("key", n -> null);
		assertNull("updateResult", updateResult);
	}

}
