package jasima.core.util.i18n;

import static java.util.Objects.requireNonNull;
import static java.util.ResourceBundle.getBundle;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public final class I18n {

	/**
	 * The default locale used, e.g., to format strings. Defaults to {@code en_US}.
	 */
	public static final Locale DEF_LOCALE = Locale.US;

	private static final Locale UNKNOWN_LOCALE = Locale.forLanguageTag("yq-YQ");
	private static final Set<String> bundleNames = new HashSet<>();
	private static final Map<Locale, ArrayDeque<ResourceBundle>> loadedBundles = new HashMap<>();

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
			throw new IllegalArgumentException("Resource bundle '" + name + "' already defined.");
		}

		// load the new ResourceBundle for each Locale requested so far, latest first
		loadedBundles.forEach((l, rbs) -> rbs.addFirst(getBundle(name, l)));

		bundleNames.add(name);
	}

	@SafeVarargs
	public static <E extends Enum<E>> void checkEnumTypeHasDefaultMessages(Class<E>... enumTypes) {
		for (Class<E> et : enumTypes) {
			for (E e : et.getEnumConstants()) {
				getMessage(UNKNOWN_LOCALE, e); // throws an exception if nothing is found
			}
		}
	}

	public static String getMessage(Locale l, Enum<?> key) {
		return getMessage(l, getKeyName(key));
	}

	public static String getMessage(Locale l, String keyName) {
		ArrayDeque<ResourceBundle> bundles = loadedBundles.computeIfAbsent(l, I18n::loadNewLocale);
		Optional<String> firstMatchingMsg = bundles.stream().map(rb -> bundleLookup(rb, keyName))
				.filter(Objects::nonNull).findFirst();
		return firstMatchingMsg.orElseThrow(() -> new MissingResourceException(
				"Can't find message for resource key '" + keyName + "'.", I18n.class.getName(), keyName));
	}

	private static String bundleLookup(ResourceBundle rb, String keyName) {
		try {
			return rb.getString(keyName);
		} catch (Exception e) {
			return (String) null;
		}
	}

	public static String getKeyName(Enum<?> key) {
		return key.getClass().getTypeName() + "." + key.toString();
	}

	private static ArrayDeque<ResourceBundle> loadNewLocale(Locale l) {
		return bundleNames.stream().map(n -> getBundle(n, l)).collect(toCollection(ArrayDeque::new));
	}

}
