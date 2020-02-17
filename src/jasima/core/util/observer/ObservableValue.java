package jasima.core.util.observer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * This class provides functionality to store a value and allows listeners to
 * get notified whenever this value is changing. Together with its subclass
 * {@link DerivedObservable} it allows a simple way to create reactive
 * expressions.
 * <p>
 * Instances of this class can be seen s a kind of variables that can be used in
 * more complex expressions provided by {@link DerivedObservable} or used
 * directly to e.g. collect statistics or update GUI elements.
 * 
 * @author Torsten Hildebrandt
 *
 * @param <VALUE> The type of the value stored.
 * @see DerivedObservable
 * @see ObservableValues
 */
public class ObservableValue<VALUE> {

	public static enum EventType {
		VALUE_CHANGED, MIGHT_HAVE_CHANGED;
	}

	private VALUE currentValue;
	private long versionId;

	private VALUE lastValue;

	private List<Supplier<BiConsumer<ObservableValue<? super VALUE>, EventType>>> listenerRefs;

	/**
	 * Creates new instance with its value being initialized with {@code null}.
	 */
	public ObservableValue() {
		this(null);
	}

	/**
	 * Creates new instance with a certain initial value.
	 * 
	 * @param initialValue The initial value of this observable value.
	 */
	public ObservableValue(VALUE initialValue) {
		super();
		this.currentValue = initialValue;
	}

	/**
	 * Returns the currently stored value.
	 */
	public VALUE get() {
		return currentValue;
	}

	/**
	 * Returns the last value stored. This method returns null if only the initial
	 * value was set.
	 */
	public VALUE getLastValue() {
		return lastValue;
	}

	/**
	 * Sets a new value. If the new values differs from the old one (checked by
	 * {@link Objects#equals(Object, Object)}), then all registered listeners are
	 * notified about changes.
	 *
	 * @param newValue The new value to store. Can be {@code null}.
	 */
	public void set(VALUE newValue) {
		internalSet(newValue);
	}

	protected void internalSet(VALUE newValue) {
		if (!Objects.equals(currentValue, newValue)) {
			lastValue = currentValue;

			currentValue = newValue;
			versionId++;

			if (numListener() > 0) {
				fireEvent(EventType.VALUE_CHANGED);
			}
		}
	}

	/**
	 * Provides a version id. The version id is increasing whenever the stored value
	 * changes.
	 */
	public long versionId() {
		return versionId;
	}

	/**
	 * Returns {@code true} if this the currently stored value needs updating.The
	 * implementation here always returns {@code false}.
	 */
	public boolean isStale() {
		return false;
	}

	/**
	 * Returns the set of immediate dependencies, i.e., all values this observable
	 * value depends on. The implementation here always returns an empty set.
	 */
	public Set<ObservableValue<?>> dependencySet() {
		return Collections.emptySet();
	}

	/**
	 * Returns the number of listeners currently registered for this observable
	 * value.
	 */
	public int numListener() {
		return listenerRefs == null ? 0 : listenerRefs.size();
	}

	/**
	 * Adds a new listener to be notified whenever the stored value is changing.
	 * This method stores the listener via a strong reference.
	 * 
	 * @param l The listener.
	 */
	public void addListener(BiConsumer<ObservableValue<? super VALUE>, EventType> l) {
		Objects.requireNonNull(l);
		initListenerList();

		listenerRefs.add(() -> l);
	}

	/**
	 * Adds a new listener to be notified whenever the stored value is changing.
	 * This method stores the listener using a weak reference so it can be garbage
	 * collected automatically when there are no further references to it.
	 * 
	 * @param l The listener.
	 */
	public void addWeakListener(BiConsumer<ObservableValue<? super VALUE>, EventType> l) {
		Objects.requireNonNull(l);
		initListenerList();

		WeakReference<BiConsumer<ObservableValue<? super VALUE>, EventType>> weakRef = new WeakReference<>(l);
		listenerRefs.add(weakRef::get);
	}

	private void initListenerList() {
		if (listenerRefs == null) {
			listenerRefs = new ArrayList<>();
		}
	}

	/**
	 * Broadcast an event to all listeners.
	 * 
	 * @param event The event denoted by a string.
	 */
	protected void fireEvent(EventType event) {
		for (int i = 0, n = numListener(); i < n; i++) {
			BiConsumer<ObservableValue<? super VALUE>, EventType> l = listenerRefs.get(i).get();
			if (l != null) {
				l.accept(this, event);
			}
		}
	}

	/**
	 * Returns {@code true} if the value currently stored in this object is either
	 * equal to {@code obj} or if {@code obj} is another {@link ObservableValue}
	 * with the stored values being equals.
	 * <p>
	 * Warning: be careful when storing {@code ObservableValue}s in Java Collections
	 * (especially Maps and Sets), as they might require a somewhat different
	 * contract of equals/hashCode.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		VALUE currentValue = get();
		if (obj instanceof ObservableValue<?>) {
			// compare to another observable
			ObservableValue<?> other = (ObservableValue<?>) obj;
			return Objects.equals(currentValue, other.get());
		} else {
			// assume 'obj' is a VALUE
			return Objects.equals(obj, currentValue);
		}
	}

}
