package jasima.core.util.i18n;

import static java.util.Objects.requireNonNull;
import static java.util.ResourceBundle.getBundle;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public final class I18n {

	/**
	 * The default locale used, e.g., to format strings. Defaults to {@code en_US}.
	 */
	public static final Locale DEF_LOCALE = Locale.US;

	private static Set<String> bundleNames = new HashSet<>();
	private static Map<Locale, ArrayDeque<ResourceBundle>> loadedBundles = new HashMap<>();

	static {
		loadedBundles.put(DEF_LOCALE, loadNewLocale(DEF_LOCALE));
	}

	/**
	 * Convenience method to call {@link String#format(String, Object...)} using the
	 * default locale as defined here (see {@link #DEF_LOCALE}) (instead of using
	 * the system's default locale).
	 * 
	 * @param formatString the format string to use
	 * @param args         any arguments to be used in the string
	 * @return The formatted string.
	 */
	public static String defFormat(String formatString, Object... args) {
		return String.format(DEF_LOCALE, formatString, args);
	}

	public static String defGetMessage(Enum<?> key) {
		return defGetMessage(getKeyName(key));
	}

	public static String defGetMessage(String keyName) {
		return getMessage(DEF_LOCALE, keyName);
	}

	public static void registerResourceBundle(String name) {
		if (bundleNames.contains(requireNonNull(name))) {
			throw new IllegalArgumentException("Resource bundle " + name + " already defined.");
		}

		// load the new ResourceBundle for each Locale requested so far, latest first
		loadedBundles.forEach((l, rbs) -> rbs.addFirst(getBundle(name, l)));

		bundleNames.add(name);
	}

	public static String getMessage(Locale l, Enum<?> key) {
		return getMessage(l, getKeyName(key));
	}

	public static String getMessage(Locale l, String keyName) {
		ArrayDeque<ResourceBundle> bundles = loadedBundles.computeIfAbsent(l, I18n::loadNewLocale);
		Optional<String> firstMatchingMsg = bundles.stream()
				.map(rb -> rb.getString(keyName))
				.filter(Objects::nonNull)
				.findFirst();
		return firstMatchingMsg.orElseThrow(IllegalArgumentException::new);
	}

	public static String getKeyName(Enum<?> key) {
		String keyName = key.getClass().getTypeName() + "." + key.toString();
		return keyName.toLowerCase(I18n.DEF_LOCALE);
	}

	private static ArrayDeque<ResourceBundle> loadNewLocale(Locale l) {
		return bundleNames.stream().map(n -> getBundle(n, l)).collect(toCollection(ArrayDeque::new));
	}

}
