package jasima.core.util.observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

}
