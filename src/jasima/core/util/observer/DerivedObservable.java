package jasima.core.util.observer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class extends {@link ObservableValue} in order to create values that are
 * derived from other observable values. The way they are created is determined
 * by a function of type {@code Supplier<T>} passed in the constructor. As
 * another parameter when constructing a DerivedObservable the set of
 * {@code ObservableValue}s used in the expression has to be given explicitly.
 * <p>
 * Internally this derived observable registers itself as a listener to its
 * dependencies. Whenever one of them changes, the stored value is marked as
 * invalid and recalculated upon the net call to {@link #get()}. Changes to a
 * derived observable are also propagated to any registered listeners, allowing
 * arbitrarily complex hierarchies of derived observable values.
 * 
 * @author Torsten Hildebrandt
 *
 * @param <T> Type of the value produced.
 */
public class DerivedObservable<T> extends ObservableValue<T> {

	private final Supplier<T> expression;
	private Set<ObservableValue<?>> dependencies;
	private boolean isStale;

	// there has to be a strong ref to the listener, because the dependencies only
	// use weak references to avoid memory leaks
	@SuppressWarnings("rawtypes") // TODO: remove
	private final ObservableListener depChangeListener;
	private boolean listenerInstalled;

	public DerivedObservable(Supplier<T> expression, ObservableValue<?>... dependencies) {
		super();

		this.expression = expression;
		this.dependencies = new HashSet<>(Arrays.asList(dependencies));
		this.depChangeListener = this::onDependencyChanged;
		this.isStale = true;
		this.listenerInstalled = false;
	}

	/**
	 * Returns the current value of this derived observable value. Calling this
	 * method calls {@link #update()} when needed, so the value returned is always
	 * the current one.
	 */
	@Override
	public T get() {
		if (!listenerInstalled) {
			installListener();
		}
		if (isStale) {
			update();
		}

		return super.get();
	}

	/**
	 * Calling this method on a derived observable doesn't make sense and will
	 * always throw an {@code UnsupportedOperationException}. Updates to the store
	 * value can be triggered explicitly by calling {@link #update()} or
	 * {@link #invalidate()} followed by a {@link #get()}, but should usually not be
	 * necessary if all dependencies of {@code expression} were provided.
	 */
	@Override
	public void set(T newValue) {
		throw new UnsupportedOperationException("Can't explicitly set the value of a derived observable.");
	}

	/**
	 * Marks the currently stored value as invalid so subsequent calls to
	 * {@code #get()} will trigger a call of {@link #update()}. This method is also
	 * called automatically whenever a dependent observable value has changed or
	 * could have changed.
	 */
	public void invalidate() {
		if (!isStale) {
			isStale = true;

			if (numListener() > 0) {
				fireEvent(EventType.MIGHT_HAVE_CHANGED);
			}
		}
	}

	/**
	 * Updated the stored value by evaluating {@link #expression} again. This method
	 * usually does not have to be called explicitly but is triggered automatically
	 * by {@link #get()} whenever the value produces by {@link #expression} might
	 * have changed.
	 */
	public T update() {
		T v = expression.get();
		internalSet(v);

		isStale = false;
		return v;
	}

	/**
	 * Returns {@code true} is the stored value was marked as (potentially) being
	 * invalid (see {@link #invalidate()}, so an {@link #update()} is required.
	 */
	@Override
	public boolean isStale() {
		return isStale;
	}

	/**
	 * Returns the set of all dependencies used directly in the expression.
	 * 
	 * @return The dependency set as immutable instance.
	 */
	@Override
	public Set<ObservableValue<?>> dependencySet() {
		return Collections.unmodifiableSet(dependencies);
	}

	@SuppressWarnings("unchecked") // TODO: remove warning
	private void installListener() {
		for (ObservableValue<?> v : dependencies) {
			v.addWeakListener(depChangeListener);
		}

		listenerInstalled = true;
	}

	private void onDependencyChanged(Object sender, Object event) {
		assert EventType.VALUE_CHANGED.equals(event) || EventType.MIGHT_HAVE_CHANGED.equals(event);

		invalidate();
	}

}
