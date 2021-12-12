package jasima.core.util;

import jasima.core.util.observer.Notifier;
import jasima.core.util.observer.NotifierListener;

/**
 * TypeHint for the compiler. This is required for, e.g.,
 * {@link Notifier#addListener(TypeHint, NotifierListener)}
 * to add a generic listener.
 */
public final class TypeHint<T> {
//	 * Represents a super type token to be able to access the generic type
//	 * information at runtime.
//	 * 
//	 * @author Torsten Hildebrandt
//	 *
//	 * @param <T> The (usually generic) type to represent.
//	private final Type type;

	public TypeHint() {
//		ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
//		type = superclass.getActualTypeArguments()[0];
	}

}