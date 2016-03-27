package jasima.core.util;

/**
 * Tags a class as supporting cloning. It publishes {@link #clone()} as a public
 * method and adds a method {@link #silentClone()} that calls {@link #clone()}
 * but does not throw the checked exception {@code CloneNotSupportedException}.
 * 
 * @author Torsten Hildebrandt
 *
 * @param <T>
 *            The class that implements this interface.
 */
public interface SilentCloneable<T> extends Cloneable {

	/**
	 * Enforce a public clone method.
	 * 
	 * @return The cloned object.
	 */
	T clone() throws CloneNotSupportedException;

	/**
	 * Same as {@link #clone()} but wrappes the
	 * {@link CloneNotSupportedException} in an unchecked
	 * {@link RuntimeException} if it occurs. Therefore client code is not
	 * required to catch {@link CloneNotSupportedException}s when it isclear
	 * that the cloned object supports it.
	 * 
	 * @return The clones object.
	 */
	default T silentClone() {
		try {
			return clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
