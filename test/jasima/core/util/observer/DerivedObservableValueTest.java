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

		ObservableValue<Boolean> eq = BooleanExpressions.equals(v1, v2);
		assertFalse(eq.get());

		v1.set(2);
		assertTrue(eq.get());
	}

	@Test
	public void testCallbacks() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(1);
		ObservableValue<Integer> v2 = new ObservableValue<Integer>(2);

		ObservableValue<Boolean> eq = BooleanExpressions.equals(v1, v2);
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

		ObservableValue<Boolean> eq = BooleanExpressions.equals(v1, v2);
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

		ObservableValue<Boolean> eq1 = BooleanExpressions.equals(v1, 2);
		ObservableValue<Boolean> eq2 = BooleanExpressions.equals(v2, 1);

		ObservableValue<Boolean> and1 = BooleanExpressions.and(eq1, eq2);

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

		ObservableValue<Boolean> eq1 = BooleanExpressions.equals(v1, 2);
		ObservableValue<Boolean> eq2 = BooleanExpressions.equals(v2, 1);

		ObservableValue<Boolean> or1 = BooleanExpressions.or(eq1, eq2);

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

		ObservableValue<Boolean> eq1 = BooleanExpressions.equals(v1, 2);
		ObservableValue<Boolean> not1 = BooleanExpressions.not(eq1);

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
