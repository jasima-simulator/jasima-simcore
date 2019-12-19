package jasima.core.util.observer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DerivedObservable<T> extends ObservableValue<T> {

	private final Supplier<T> expression;
	private Set<ObservableValue<?>> dependencies;
	private boolean isStale;

	// there has to be a strong ref to the listener, because the dependencies only
	// use weak references to avoid memory leaks
	@SuppressWarnings("rawtypes") // TODO: remove
	private final BiConsumer depChangeListener;
	private boolean listenerInstalled;

	public DerivedObservable(Supplier<T> expression, ObservableValue<?>... dependencies) {
		super();

		this.expression = expression;
		this.dependencies = new HashSet<>(Arrays.asList(dependencies));
		this.depChangeListener = this::onDependencyChanged;
		this.isStale = true;
		this.listenerInstalled = false;
	}

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

	public void invalidate() {
		if (!isStale) {
			isStale = true;

			if (numListener() > 0) {
				fireEvent("MIGHT_HAVE_CHANGED");
			}
		}
	}

	protected T update() {
		T v = expression.get();
		set(v);

		isStale = false;
		return v;
	}

	@Override
	public boolean isStale() {
		return isStale;
	}

	@Override
	public Set<ObservableValue<?>> dependencySet() {
		return Collections.unmodifiableSet(dependencies);
	}

	@SuppressWarnings("unchecked") // TODO: remove
	private void installListener() {
		for (ObservableValue<?> v : dependencies) {
			v.addWeakListener(depChangeListener);
		}

		listenerInstalled = true;
	}

	private void onDependencyChanged(Object sender, Object event) {
		assert "VALUE_CHANGED".equals(event) || "MIGHT_HAVE_CHANGED".equals(event);

		invalidate();
	}

}
