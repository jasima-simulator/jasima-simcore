package jasima.core.util;

/**
 * Static helper methods dealing with Strings.
 * 
 * @author Torsten.Hildebrandt
 *
 */
public class StringUtil {

	/**
	 * Repeats a given String/{@link CharSequence} a certain number of times.
	 * 
	 * @param toRepeat The CharSequence (usually a String) to repeat. Mustn't be
	 *                 null.
	 * @param n        How many times to repeat it. If n is 0 or negative, an empty
	 *                 String will be returned.
	 * @return A String containing {@code n} copies of {@code toRepeat}.
	 */
	public static String repeat(CharSequence toRepeat, int n) {
		if (n <= 0 || toRepeat.length() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(n * toRepeat.length());
		for (int i = 0; i < n; i++) {
			sb.append(toRepeat);
		}
		return sb.toString();
	}

	/**
	 * Return true is the given parameter (usually a String) is either null or
	 * empty.
	 * 
	 * @param cs The CharSequence to check.
	 * @return true, is the parameter was null or had a length of 0; false
	 *         otherwise.
	 */
	public static boolean isNullOrEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

}
