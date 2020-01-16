package jasima.core.util.i18n;

import static jasima.core.util.i18n.I18n.I18nConsts.RES_NOT_FOUND;
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

import jasima.core.util.StandardExtensionImpl;

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
		I18n.requireResourceBundle(StandardExtensionImpl.JASIMA_CORE_RES_BUNDLE, I18nConsts.class);
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

	public static String message(Enum<?> key) {
		return message(DEF_LOCALE, key);
	}

	public static String message(String keyName) {
		return message(DEF_LOCALE, keyName);
	}

	public static String message(Locale l, Enum<?> key) {
		return message(l, keyName(key));
	}

	public static String message(Locale l, String keyName) {
		ArrayDeque<ResourceBundle> bundles = loadedBundles.computeIfAbsent(l, I18n::loadNewLocale);
		Optional<String> firstMatchingMsg = bundles.stream().map(rb -> bundleLookup(rb, keyName))
				.filter(Objects::nonNull).findFirst();
		return firstMatchingMsg.orElseThrow(() -> new MissingResourceException(formattedMessage(RES_NOT_FOUND, keyName),
				I18n.class.getName(), keyName));
	}

	public static String formattedMessage(Enum<?> key, Object... args) {
		return formattedMessage(DEF_LOCALE, key, args);
	}

	public static String formattedMessage(String keyName, Object... args) {
		return formattedMessage(DEF_LOCALE, keyName, args);
	}

	public static String formattedMessage(Locale l, Enum<?> key, Object... args) {
		return formattedMessage(l, message(l, key), args);
	}

	public static String formattedMessage(Locale l, String keyName, Object... args) {
		return String.format(l, message(l, keyName), args);
	}

	public static String keyName(Enum<?> key) {
		return key.getClass().getCanonicalName() + "." + key.toString();
	}

	public static void registerResourceBundle(String name) {
		if (bundleNames.contains(requireNonNull(name))) {
			return;
		}

		// load the new ResourceBundle for each Locale requested so far, latest first
		loadedBundles.forEach((l, rbs) -> rbs.addFirst(getBundle(name, l)));
		bundleNames.add(name);
	}

	@SafeVarargs
	public static <E extends Enum<E>> void requireResourceBundle(String bundleName, Class<E>... enumTypes) {
		registerResourceBundle(bundleName);
		for (Class<E> et : enumTypes) {
			checkEnumTypeHasDefaultMessages(et);
		}
	}

	public static <E extends Enum<E>> void checkEnumTypeHasDefaultMessages(Class<E> enumType) {
		for (E e : enumType.getEnumConstants()) {
			message(UNKNOWN_LOCALE, e); // throws an exception if nothing is found
		}
	}

	private static String bundleLookup(ResourceBundle rb, String keyName) {
		try {
			return rb.getString(keyName);
		} catch (Exception e) {
			return (String) null;
		}
	}

	private static ArrayDeque<ResourceBundle> loadNewLocale(Locale l) {
		return bundleNames.stream().map(n -> getBundle(n, l)).collect(toCollection(ArrayDeque::new));
	}

	static enum I18nConsts {
		RES_NOT_FOUND;
	}

}
