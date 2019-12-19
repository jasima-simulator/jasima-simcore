package jasima.core.util.observer;

import java.util.Objects;

public class BooleanExpressions {

	public static <T> DerivedObservable<Boolean> equals(ObservableValue<? extends T> v1,
			ObservableValue<? extends T> v2) {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		return new DerivedObservable<Boolean>(() -> v1.equals(v2), v1, v2);
	}

	public static <T, T2 extends T> DerivedObservable<Boolean> equals(ObservableValue<? extends T> v1, T2 v2) {
		Objects.requireNonNull(v1);
		return new DerivedObservable<Boolean>(() -> v1.equals(v2), v1);
	}

	public static <T, T2 extends T> DerivedObservable<Boolean> equals(T2 v1, ObservableValue<? extends T> v2) {
		return equals(v2, v1);
	}

	public static DerivedObservable<Boolean> and(ObservableValue<Boolean> v1, ObservableValue<Boolean> v2) {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		return new DerivedObservable<Boolean>(() -> v1.get() && v2.get(), v1, v2);
	}

	public static DerivedObservable<Boolean> or(ObservableValue<Boolean> v1, ObservableValue<Boolean> v2) {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		return new DerivedObservable<Boolean>(() -> v1.get() || v2.get(), v1, v2);
	}

	public static DerivedObservable<Boolean> not(ObservableValue<Boolean> v) {
		Objects.requireNonNull(v);
		return new DerivedObservable<Boolean>(() -> !v.get(), v);
	}

	private BooleanExpressions() { // prevent initialization
		throw new AssertionError();
	}

}
