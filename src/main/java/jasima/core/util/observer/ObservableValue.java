/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.util.observer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

	@FunctionalInterface
	public static interface ObservableListener<V> {
		void onEvent(ObservableValue<V> ov, EventType et);
	}

	public static enum EventType {
		VALUE_CHANGED, MIGHT_HAVE_CHANGED;
	}

	private VALUE currentValue;
	private VALUE lastValue;
	private long versionId;

	private List<Supplier<ObservableListener<VALUE>>> listenerRefs;
	private Map<ObservableListener<VALUE>, Supplier<ObservableListener<VALUE>>> supplierLookup;

	private int firingInProgress;
	private List<ObservableListener<VALUE>> removeWhileFiring;

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

		firingInProgress = 0;
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

	/**
	 * Updates the current value of this observable by calling the function passed
	 * as a parameter. The current value is passed as a parameter, the value
	 * returned is used as the new value.
	 * 
	 * @param updateFunc The update function to use.
	 * @return the current observable instance to allow chaining calls.
	 */
	public ObservableValue<VALUE> update(Function<VALUE, VALUE> updateFunc) {
		set(updateFunc.apply(get()));
		return this;
	}

	protected void internalSet(VALUE newValue) {
		if (!Objects.equals(currentValue, newValue)) {
			lastValue = currentValue;

			currentValue = newValue;
			versionId++;

			if (numListener() > 0 && !isStale()) {
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

	public void whenEquals(VALUE v, Runnable action) {
		Objects.requireNonNull(action);

		final ObservableListener<VALUE> l = new ObservableListener<VALUE>() {
			@Override
			public void onEvent(ObservableValue<VALUE> ov, EventType et) {
				assert ov == ObservableValue.this;
				if (ov.equals(v)) {
					action.run();
					removeListener(this);
				}
			}
		};
		addListener(l);
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
	 * @return the listener (same as l).
	 */
	public ObservableListener<VALUE> addListener(ObservableListener<VALUE> l) {
		return addListener(l, () -> l);
	}

	/**
	 * Adds a new listener to be notified whenever the stored value is changing.
	 * This method stores the listener using a weak reference so it can be garbage
	 * collected automatically when there are no further references to it.
	 * 
	 * @param l The listener.
	 * @return the listener (same as l).
	 */
	public ObservableListener<VALUE> addWeakListener(ObservableListener<VALUE> l) {
		WeakReference<ObservableListener<VALUE>> weakRef = new WeakReference<>(l);
		return addListener(l, weakRef::get);
	}

	private ObservableListener<VALUE> addListener(ObservableListener<VALUE> l, Supplier<ObservableListener<VALUE>> e) {
		Objects.requireNonNull(l);
		initListenerList();

		supplierLookup.put(l, e);
		listenerRefs.add(e);

		return l;
	}

	/**
	 * Removes a listener that was previously added using
	 * {@link #addListener(BiConsumer)} or
	 * {@link #addWeakListener(ObservableListener)}.
	 * 
	 * @param l The listener.
	 * @return {@code true} if l was still registered as a listener and could be
	 *         removed.
	 */
	public boolean removeListener(ObservableListener<VALUE> l) {
		Objects.requireNonNull(l);
		initListenerList();

		if (firingInProgress > 0) {
			if (removeWhileFiring == null) {
				removeWhileFiring = new ArrayList<>();
			}
			boolean stillContains = supplierLookup.containsKey(l) && !removeWhileFiring.contains(l);
			if (stillContains) {
				removeWhileFiring.add(l);
			}

			return stillContains;
		} else {
			Supplier<ObservableListener<VALUE>> supp = supplierLookup.remove(l);
			if (supp != null) {
				listenerRefs.remove(supp);
			}

			return supp != null;
		}
	}

	private void initListenerList() {
		if (listenerRefs == null) {
			listenerRefs = new ArrayList<>();
			supplierLookup = new WeakHashMap<>();
		}
	}

	/**
	 * Broadcast an event to all listeners.
	 * 
	 * @param event The event denoted by a string.
	 */
	protected void fireEvent(EventType event) {
		firingInProgress++;

		for (Supplier<ObservableListener<VALUE>> s : listenerRefs) {
			ObservableListener<VALUE> l = s.get();
			if (l != null) {
				l.onEvent(this, event);
			}
		}

		if (--firingInProgress == 0 && removeWhileFiring != null) {
			for (ObservableListener<VALUE> l : removeWhileFiring) {
				boolean removeRes = removeListener(l);
				assert removeRes;
			}
			removeWhileFiring.clear();
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
