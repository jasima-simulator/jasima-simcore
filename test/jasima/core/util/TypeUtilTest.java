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
