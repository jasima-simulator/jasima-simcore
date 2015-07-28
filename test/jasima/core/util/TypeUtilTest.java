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
package jasima.core.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class TypeUtilTest {

	@Test
	public void testComputeClasses() {
		Class<?>[] expected = new Class<?>[] { X.class, Y.class, A.class,
				B.class, C.class, D.class, M.class, N.class, O.class,
				Object.class };
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
}
