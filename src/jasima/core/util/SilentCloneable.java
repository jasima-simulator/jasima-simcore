package jasima.core.util;

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
