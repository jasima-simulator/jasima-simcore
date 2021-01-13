package jasima.core.util;

/**
 * Represents a super type token to be able to access the generic type
 * information at runtime.
 * 
 * @author Torsten Hildebrandt
 *
 * @param <T> The (usually generic) type to represent.
 */
public abstract class TypeRef<T> {
//	private final Type type;

	protected TypeRef() {
//		ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
//		type = superclass.getActualTypeArguments()[0];
	}

}