package jasima.core.random.continuous;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsNaN.notANumber;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import jasima.core.random.discrete.IntConst;
import jasima.core.util.Pair;

public class IntConstTest {

	private static final int[] TEST_VALUES = new int[] { 1, 3, 2, 4 };

	@Test
	public void shouldReturnNumbersInSequenceOnce() {
		IntConst dblConst = new IntConst(TEST_VALUES);
		assertArrayEquals(TEST_VALUES, dblConst.getValues());
		assertEquals(false, dblConst.isRandomizeOrder());
		assertEquals(1, dblConst.nextInt());
		assertEquals(3, dblConst.nextInt());
		assertEquals(2, dblConst.nextInt());
		assertEquals(4, dblConst.nextInt());
	}

	@Test
	public void shouldReturnNumbersRepeatedly() {
		IntConst dblConst = new IntConst(TEST_VALUES);
		for (int n = 0; n < 3; n++) {
			assertEquals(1, dblConst.nextInt());
			assertEquals(3, dblConst.nextInt());
			assertEquals(2, dblConst.nextInt());
			assertEquals(4, dblConst.nextInt());
		}
	}

	@Test
	public void shouldReturnNumericalValues() {
		IntConst dblConst = new IntConst(TEST_VALUES);
		assertEquals(2.5, dblConst.getNumericalMean(), 1e-6);
		assertEquals(1.0, dblConst.min(), 1e-6);
		assertEquals(4.0, dblConst.max(), 1e-6);
		assertEquals(new Pair<Double, Double>(1.0, 4.0), dblConst.getValueRange());
		// second call returns cached value
		assertEquals(2.5, dblConst.getNumericalMean(), 1e-6);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNpeIfRandomizationButRandomNotInitialized() {
		IntConst dblConst = new IntConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);

		dblConst.nextInt(); // raises NPE
	}

	@Test
	public void shouldRandomizeOnce() {
		IntConst dblConst = new IntConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);
		dblConst.setRndGen(new Random(123));

		assertEquals(1, dblConst.nextInt());
		assertEquals(3, dblConst.nextInt());
		assertEquals(4, dblConst.nextInt());
		assertEquals(2, dblConst.nextInt());

		// values should still be in original order
		assertArrayEquals(TEST_VALUES, dblConst.getValues());
	}

	@Test
	public void shouldRandomizeTwice() {
		IntConst dblConst = new IntConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);
		dblConst.setRndGen(new Random(123));

		// first iteration
		assertEquals(1, dblConst.nextInt());
		assertEquals(3, dblConst.nextInt());
		assertEquals(4, dblConst.nextInt());
		assertEquals(2, dblConst.nextInt());

		// second iteration
		assertEquals(4, dblConst.nextInt());
		assertEquals(2, dblConst.nextInt());
		assertEquals(1, dblConst.nextInt());
		assertEquals(3, dblConst.nextInt());
	}

	@Test
	public void resettingRndGenShouldClearRandomOrder() {
		IntConst dblConst = new IntConst();
		dblConst.setValues(TEST_VALUES);
		dblConst.setRandomizeOrder(true);

		for (int n = 0; n < 3; n++) {
			dblConst.setRndGen(new Random(123));

			assertEquals(1, dblConst.nextInt());
			assertEquals(3, dblConst.nextInt());
			assertEquals(4, dblConst.nextInt());
			assertEquals(2, dblConst.nextInt());
		}
	}

	@Test
	public void uninitializedIntConstShouldReturnDefinedValues() {
		IntConst dblConst = new IntConst();
		assertArrayEquals(null, dblConst.getValues());
		assertThat(dblConst.min(), is(notANumber()));
		assertThat(dblConst.max(), is(notANumber()));
		assertThat(dblConst.getNumericalMean(), is(notANumber()));
	}

	@Test
	public void emptyValuesShouldReturnDefinedValues() {
		IntConst dblConst = new IntConst(new int[] {});
		assertArrayEquals(new int[] {}, dblConst.getValues());
		assertThat(dblConst.min(), is(notANumber()));
		assertThat(dblConst.max(), is(notANumber()));
		assertThat(dblConst.getNumericalMean(), is(notANumber()));
	}

	@Test
	public void shouldBeCloneable() {
		IntConst original = new IntConst(TEST_VALUES);
		IntConst clone = original.clone();

		assertEquals(2.5, clone.getNumericalMean(), 1e-6);
		assertEquals(1.0, clone.min(), 1e-6);
		assertEquals(4.0, clone.max(), 1e-6);
		assertEquals("toString", "IntConst[1, 3, 2, 4]", new IntConst(TEST_VALUES).toString());
	}

	@Test
	public void testStringRepresentation() {
		assertEquals("toString", "IntConst[1, 3, 2, 4]", new IntConst(TEST_VALUES).toString());
	}

}
