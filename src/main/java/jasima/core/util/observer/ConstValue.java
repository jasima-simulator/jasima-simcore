package jasima.core.util.observer;

/**
 * Technically an observable, but can't be changed. An attempt to set a new
 * value will result in an {@link UnsupportedOperationException}.
 * 
 * @author Torsten Hildebrandt
 *
 * @param <T>
 */
public class ConstValue<T> extends ObservableValue<T> {

	public ConstValue(T initialValue) {
		super(initialValue);
	}

	@Override
	public final void set(T newValue) {
		throw new UnsupportedOperationException("Can't change value of a ConstValue.");
	}
	

}
