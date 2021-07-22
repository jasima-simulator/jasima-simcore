package jasima.core.util.observer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import jasima.core.util.observer.ObservableValue.ObservableListener;

/**
 * Static factory methods to ease the creation of {@link ObservableValue}s and
 * {@link DerivedObservable}s.
 * 
 * @author Torsten Hildebrandt
 * 
 * @see ObservableValue
 * @see DerivedObservable
 */
public final class ObservableValues {

	/**
	 * Creates a new observable value with a certain initial value. Shorthand for
	 * calling the constructor {@link ObservableValue#ObservableValue(Object)}.
	 * 
	 * @param <T>          Type of the value stored in the observable.
	 * @param initialValue The initial value of the observable.
	 * @return The new observable value.
	 */
	public static <T> ObservableValue<T> observable(T initialValue) {
		return new ObservableValue<T>(initialValue);
	}

	public static <T> ObservableValue<T> observable(String name, T initialValue) {
		return new ObservableValue<T>(initialValue);
	}

	/**
	 * Creates a new observable value with a value as calculated by some functional
	 * expression. Shorthand for calling the constructor
	 * {@link DerivedObservable#DerivedObservable(java.util.function.Supplier, ObservableValue...)}.
	 * 
	 * @param <T>          Type of the value stored in the observable.
	 * @param expression   Expression to calculated the observable's value.
	 * @param dependencies All observable values used in the expression.
	 * @return The new derived observable value.
	 */
	public static <T> DerivedObservable<T> derived(Supplier<T> expression, ObservableValue<?>... dependencies) {
		return new DerivedObservable<T>(expression, dependencies);
	}

	public static <T> DerivedObservable<T> derived(String name, Supplier<T> expression,
			ObservableValue<?>... dependencies) {
		return new DerivedObservable<T>(expression, dependencies);
	}

	/**
	 * Returns a derived observable for comparing the values of two other observable
	 * values.
	 * 
	 * @param <T> Type or common sub-type of the values to compare.
	 * @param v1  The first value.
	 * @param v2  The second value.
	 * @return A derived observable of type {@code Boolean} containing the result of
	 *         comparing both values.
	 */
	public static <T> DerivedObservable<Boolean> isEqual(ObservableValue<? extends T> v1,
			ObservableValue<? extends T> v2) {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		return derived(() -> v1.equals(v2), v1, v2);
	}

	/**
	 * Same as {@link #isEqual(ObservableValue, ObservableValue)}, but comparing an
	 * observable value directly to a constant value.
	 */
	public static <T, T2 extends T> DerivedObservable<Boolean> isEqual(ObservableValue<? extends T> v1, T2 v2) {
		Objects.requireNonNull(v1);
		return derived(() -> v1.equals(v2), v1);
	}

	/**
	 * Same as {@link #isEqual(ObservableValue, ObservableValue)}, but comparing an
	 * observable value directly to a constant value.
	 */
	public static <T, T2 extends T> DerivedObservable<Boolean> isEqual(T2 v1, ObservableValue<? extends T> v2) {
		return isEqual(v2, v1);
	}

	/**
	 * Runs the given action when the value of the given observable becomes true.
	 * This can happen either immediately if the condition is already true when
	 * calling this method, or at a later point when the condition changes later.
	 * 
	 * @param ov     The observable that should be true.
	 * @param action The action to run when "ov" is true.
	 * @return The listener registered with "ov" triggering the execution of
	 *         "action", or null if ov was already true.
	 */
	public static ObservableListener<Boolean> whenTrueExecuteOnce(ObservableValue<Boolean> ov, Runnable action) {
		Objects.requireNonNull(ov);
		Objects.requireNonNull(action);

		boolean currentValue = ov.get().booleanValue();

		// already true?
		if (currentValue) {
			action.run();
			return null;
		}

		// currently false, so install listener and run action once it becomes true
		AtomicReference<ObservableListener<Boolean>> listener = new AtomicReference<>();
		listener.set(ov.addListener((ob, evt) -> {
			if (ov.get().booleanValue()) {
				try {
					action.run();
				} finally {
					boolean removeRes = ov.removeListener(listener.get()); // remove the listener
					assert removeRes;
				}
			}
		}));
		return listener.get();
	}

	/**
	 * Creates a derived observable of type {@code Boolean} containing a logical
	 * 'and' between two other observable values.
	 */
	public static DerivedObservable<Boolean> and(ObservableValue<Boolean> v1, ObservableValue<Boolean> v2) {
		return fromBinaryOperation((b1, b2) -> b1 && b2, v1, v2);
	}

	/**
	 * Creates a derived observable of type {@code Boolean} containing a logical
	 * 'or' between two other observable values.
	 */
	public static DerivedObservable<Boolean> or(ObservableValue<Boolean> v1, ObservableValue<Boolean> v2) {
		return fromBinaryOperation((b1, b2) -> b1 || b2, v1, v2);
	}

	/**
	 * Creates a derived observable of type {@code Boolean} containing a logical
	 * 'not' of another observable value.
	 */
	public static DerivedObservable<Boolean> not(ObservableValue<Boolean> v) {
		return fromUnaryOperation(b -> !b, v);
	}

	/**
	 * Creates a new DerivedObservable by applying a certain binary function to two
	 * other observable values.
	 */
	public static <T1, T2, R> DerivedObservable<R> fromBinaryOperation(BiFunction<T1, T2, R> operation,
			ObservableValue<? extends T1> v1, ObservableValue<? extends T2> v2) {
		Objects.requireNonNull(v1);
		Objects.requireNonNull(v2);
		return derived(() -> operation.apply(v1.get(), v2.get()), v1, v2);
	}

	/**
	 * Creates a new DerivedObservable by applying a certain unary function to
	 * another observable value.
	 */
	public static <T, R> DerivedObservable<R> fromUnaryOperation(Function<T, R> operation,
			ObservableValue<? extends T> v) {
		Objects.requireNonNull(v);
		return derived(() -> operation.apply(v.get()), v);
	}

	public static final ObservableValue<Boolean> TRUE = new ConstValue<>(Boolean.TRUE);
	public static final ObservableValue<Boolean> FALSE = new ConstValue<>(Boolean.FALSE);

	/**
	 * prevent instantiation
	 */
	private ObservableValues() {
		throw new AssertionError();
	}

}
