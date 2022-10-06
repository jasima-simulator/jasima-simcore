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
package jasima.core.experiment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import jasima.core.util.TypeUtil;

public class Test_FFE_Array_Setter {

	public static class TestArrays extends Experiment {

		private int intTest;
		private int[] test0;
		private Object[] test1;
		private TestArrays[] test2;
		private ArrayList<TestArrays> testList = new ArrayList<>();

		public TestArrays() {
			super();
		}

		@Override
		protected void performRun() {

		}

		@Override
		protected void produceResults() {
			super.produceResults();
			resultMap.put("test0[0]", getTest0() == null ? null : getTest0()[0]);
		}

		public int getIntTest() {
			return intTest;
		}

		public void setIntTest(int intTest) {
			this.intTest = intTest;
		}

		public int[] getTest0() {
			return test0;
		}

		public void setTest0(int[] test0) {
			this.test0 = test0;
		}

		public Object[] getTest1() {
			return test1;
		}

		public void setTest1(Object[] test1) {
			this.test1 = test1;
		}

		public TestArrays[] getTest2() {
			return test2;
		}

		public void setTest2(TestArrays[] test2) {
			this.test2 = test2;
		}

		public ArrayList<TestArrays> getTestList() {
			return testList;
		}

		public void setTestList(ArrayList<TestArrays> testList) {
			this.testList = testList;
		}

	}

	@Test
	public void testSimple() {
		TestArrays e = new TestArrays();
		assertEquals(0, e.getIntTest());
		TypeUtil.setPropertyValue(e, "intTest", "23");
		assertEquals(23, e.getIntTest());
	}

	@Test
	public void testArray1() {
		TestArrays e = new TestArrays();
		e.setTest0(new int[5]);
		TypeUtil.setPropertyValue(e, "test0[4]", 42);
		assertEquals(42, e.getTest0()[4]);
	}

	@Test
	public void testArrayType1() {
		TestArrays e = new TestArrays();
		e.setTest0(new int[5]);

		assertEquals(int[].class, TypeUtil.getPropertyType(e, "test0"));
		assertEquals(TestArrays[].class, TypeUtil.getPropertyType(e, "test2"));

		assertEquals(int.class, TypeUtil.getPropertyType(e, "test0[1]"));
		assertEquals(TestArrays.class, TypeUtil.getPropertyType(e, "test2[3]"));
	}

	@Test
	public void testArray2() {
		TestArrays e = new TestArrays();
		e.setTest2(new TestArrays[5]);
		TypeUtil.setPropertyValue(e, "test2[4]", "jasima.core.experiment.Test_FFE_Array_Setter$TestArrays(intTest=42)");
		assertNotNull(e.getTest2()[4]);
		assertEquals(42, e.getTest2()[4].getIntTest());
		TypeUtil.setPropertyValue(e, "test2[4].intTest", 23);
		assertEquals(23, e.getTest2()[4].getIntTest());
	}

	@Test
	public void testList1() {
		TestArrays e = new TestArrays();
		e.getTestList().addAll(Arrays.asList(new TestArrays(), null, null));
		TypeUtil.setPropertyValue(e, "testList[2]",
				"jasima.core.experiment.Test_FFE_Array_Setter$TestArrays(intTest=43)");
		assertEquals(43, e.getTestList().get(2).getIntTest());
	}

	@Test
	public void testList2() {
		TestArrays e = new TestArrays();
		e.getTestList().addAll(Arrays.asList(new TestArrays(), null, null));
		TypeUtil.setPropertyValue(e, "testList[0].intTest", "48");
		assertEquals(48, e.getTestList().get(0).getIntTest());
	}

	@Test
	public void testList3() {
		TestArrays e = new TestArrays();
		e.getTestList().addAll(Arrays.asList(new TestArrays(), null, null));

		assertEquals(ArrayList.class, TypeUtil.getPropertyType(e, "testList"));
		assertEquals(Object.class, TypeUtil.getPropertyType(e, "testList[0]"));
	}

}
