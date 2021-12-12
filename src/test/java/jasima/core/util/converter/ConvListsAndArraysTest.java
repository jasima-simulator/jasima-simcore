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
