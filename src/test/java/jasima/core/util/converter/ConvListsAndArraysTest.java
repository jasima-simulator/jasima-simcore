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
package jasima.core.util.converter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jasima.core.util.TypeUtil;

public class ConvListsAndArraysTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public static class SimpleBean {
		private int a;
		private String b;
		private Double[] doubleArray;

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}

		public Double[] getDoubleArray() {
			return doubleArray;
		}

		public void setDoubleArray(Double[] doubleArray) {
			this.doubleArray = doubleArray;
		}
	}

	public static class TestBean {
		private String[] stringArray;
		private Double[] doubleArray;
		private SimpleBean[] beanArray;

		public TestBean() {
			super();
		}

		public String[] getStringArray() {
			return stringArray;
		}

		public void setStringArray(String[] stringArray) {
			this.stringArray = stringArray;
		}

		public Double[] getDoubleArray() {
			return doubleArray;
		}

		public void setDoubleArray(Double[] doubleArray) {
			this.doubleArray = doubleArray;
		}

		public SimpleBean[] getBeanArray() {
			return beanArray;
		}

		public void setBeanArray(SimpleBean[] beanArray) {
			this.beanArray = beanArray;
		}
	}

	@Test
	public void testStringArray() {
		TestBean tb = new TestBean();

		TypeUtil.setPropertyValue(tb, "stringArray", "[test1;test2;test3]");

		assertThat(tb.getStringArray().length, is(3));
		assertThat(tb.getStringArray(), is(new String[] { "test1", "test2", "test3" }));
	}

	@Test
	public void testDoubleArray() {
		TestBean tb = new TestBean();

		TypeUtil.setPropertyValue(tb, "doubleArray", "[1;2.0;3.5]");

		assertThat(tb.getDoubleArray().length, is(3));
		assertThat(tb.getDoubleArray(), is(new Double[] { 1.0, 2.0, 3.5 }));
	}

	@Test
	public void testBeanArray() {
		TestBean tb = new TestBean();
		tb.setBeanArray(new SimpleBean[] { new SimpleBean(), null, new SimpleBean() });

		TypeUtil.setPropertyValue(tb, "beanArray[0].doubleArray", "[1;2.0;3.5]");

		assertThat(tb.getBeanArray()[0].getDoubleArray(), is(new Double[] { 1.0, 2.0, 3.5 }));
	}

}
