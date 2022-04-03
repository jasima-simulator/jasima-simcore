package jasima.core.util.observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import jasima.core.util.observer.ObservableValue.ObservableListener;

public class ObservableValueTest {

	@Test
	public void getterTest() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(2);

		assertEquals(2, v1.get().intValue());
	}

	@Test
	public void equalsTestBaseType() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(2);

		assertFalse(v1.equals(1));
		assertTrue(v1.equals(2));
	}

	@Test
	public void equalsTestObservable() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(2);

		assertFalse(v1.equals(new ObservableValue<>(3)));
		assertTrue(v1.equals(new ObservableValue<>(2)));
	}

	@Test
	public void equalsTestBaseTypeSubclass() {
		ObservableValue<Number> v1 = new ObservableValue<>(2);

		assertFalse(v1.equals(1));
		assertTrue(v1.equals(2));
	}

	@Test
	public void equalsTestObservableSubclass() {
		ObservableValue<Number> v1 = new ObservableValue<>(2);

		assertFalse(v1.equals(new ObservableValue<>(3)));
		assertTrue(v1.equals(new ObservableValue<>(2)));
	}

	@Test
	public void whenTrueTestShouldWaitUntilConditionIsTrue() {
		ObservableValue<Number> v1 = new ObservableValue<>(2);

		ObservableValue<Boolean> eq = ObservableValues.isEqual(v1, 3);
		assertFalse(eq.get());

		AtomicInteger actionExecutedCounter = new AtomicInteger(0);
		ObservableListener<Boolean> retVal = ObservableValues.whenTrueExecuteOnce(eq,
				() -> actionExecutedCounter.incrementAndGet());
		assertNotNull(retVal);

		assertEquals(0, actionExecutedCounter.get());

		v1.set(3); // should trigger action
		assertEquals(1, actionExecutedCounter.get());
	}

	@Test
	public void whenTrueTestShouldExecuteOnlyOnce() {
		ObservableValue<Number> v1 = new ObservableValue<>(2);

		ObservableValue<Boolean> eq = ObservableValues.isEqual(v1, 3);

		AtomicInteger actionExecutedCounter = new AtomicInteger(0);
		ObservableValues.whenTrueExecuteOnce(eq, () -> actionExecutedCounter.incrementAndGet());

		v1.set(3);
		assertTrue(eq.get());

		v1.set(2);
		assertFalse(eq.get());

		v1.set(3);
		assertTrue(eq.get());

		assertEquals("only executed once", 1, actionExecutedCounter.get());
	}

	@Test
	public void whenTrueTestShouldExecuteImmediatelyWhenConditionIsTrue() {
		ObservableValue<Number> v1 = new ObservableValue<>(2);

		ObservableValue<Boolean> eq = ObservableValues.isEqual(v1, 2);
		assertTrue(eq.get());

		AtomicInteger actionExecutedCounter = new AtomicInteger(0);
		ObservableListener<Boolean> retVal = ObservableValues.whenTrueExecuteOnce(eq,
				() -> actionExecutedCounter.incrementAndGet());
		assertEquals(1, actionExecutedCounter.get());
		assertNull(retVal);

		v1.set(3);
		assertFalse(eq.get());

		v1.set(2);
		assertTrue(eq.get());

		assertEquals("not executed twice", 1, actionExecutedCounter.get());
	}

	@Test
	public void deferredRemoveListShouldBeCleared() {
		ObservableValue<Integer> v1 = new ObservableValue<Integer>(2);
		ObservableListener<Integer> dummyListener = v1.addListener((ov,evt)->{});
		// trigges removal while firing
		ObservableListener<Integer> deferredRemove = v1.addListener((ov,evt)->v1.removeListener(dummyListener));
		
		assertEquals("numListener", 2, v1.numListener());
		
		// trigger removal
		v1.set(3);
		v1.removeListener(deferredRemove);
		assertEquals("numListener", 0, v1.numListener());

		ObservableListener<Integer> dummyListener2 = v1.addListener((ov,evt)->{});
		// referred removal
		v1.addListener((ov,evt)->v1.removeListener(dummyListener2));
		assertEquals("numListener", 2, v1.numListener());
		
		// trigger removal2
		v1.set(4);
		
		// everything worked if this point is reached without Exception
	}

}
