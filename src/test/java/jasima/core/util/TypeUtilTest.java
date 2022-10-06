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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TypeUtilTest {

	@Test
	public void testComputeClasses() {
		Class<?>[] expected = new Class<?>[] { X.class, Y.class, A.class, B.class, C.class, D.class, M.class, N.class,
				O.class, Object.class };
		Class<?>[] actual = TypeUtil.computeClasses(X.class);

		assertThat(actual, equalTo(expected));
	}

	public interface O {

	}

	public interface B extends O {

	}

	public interface C {

	}

	public interface D {

	}

	public interface M {

	}

	public interface N {

	}

	public interface A extends M, N {

	}

	public static class Y implements C, D {

	}

	public static class X extends Y implements A, B {

	}

	@Test
	public void loadClass__validClassName__classObject() throws Exception {
		assertEquals(Y.class, TypeUtil.loadClass(Y.class.getName(), Y.class));
	}

	@Test
	public void loadClass__correctParentClass__classObject() throws Exception {
		assertEquals(Y.class, TypeUtil.loadClass(Y.class.getName(), D.class));
	}

	@Test(expected = ClassCastException.class)
	public void loadClass__incompatibleParentClass__classCastException() throws Exception {
		TypeUtil.loadClass(Y.class.getName(), A.class);
	}

	@Test(expected = ClassNotFoundException.class)
	public void loadClass__unknownClassName__classObject() throws Exception {
		TypeUtil.loadClass(Y.class.getName() + "NotExisting", Y.class);
	}

	@Test
	public void loadClass__shouldUseContextClassLoader() throws Exception {
		Class<Y> testClass = Y.class;

		// should work before changing class loader
		assertEquals(testClass, TypeUtil.loadClass(testClass.getName(), testClass));

		// change context class loader ignoring 'testClass'
		ClassLoader oldCtxClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader noYClassLoader = new ClassLoader(oldCtxClassLoader) {
				@Override
				public Class<?> loadClass(String name) throws ClassNotFoundException {
					if (testClass.getName().equals(name))
						throw new ClassNotFoundException();

					return super.loadClass(name);
				}
			};
			Thread.currentThread().setContextClassLoader(noYClassLoader);

			// should trigger exception now
			try {
				TypeUtil.loadClass(testClass.getName(), testClass);
				fail("no exception triggered");
			} catch (ClassNotFoundException expected) {
			}
		} finally {
			// restore old class loader in order not to screw up other tests
			Thread.currentThread().setContextClassLoader(oldCtxClassLoader);
		}
	}

}
