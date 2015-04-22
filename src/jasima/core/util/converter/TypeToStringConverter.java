package jasima.core.util.converter;

import jasima.core.util.ArgListTokenizer;
import jasima.core.util.ArgListTokenizer.ParseException;
import jasima.core.util.TypeUtil;
import jasima.core.util.TypeUtil.TypeConversionException;
import jasima.core.util.Util;

import java.util.HashMap;

public abstract class TypeToStringConverter {

	public TypeToStringConverter() {
		super();
	}

	public abstract Class<?>[] handledTypes();

	public abstract <T> T fromString(ArgListTokenizer tk,
			Class<T> requiredType, String context, ClassLoader loader,
			String[] packageSearchPath);

	public String toString(Object o) {
		return String.valueOf(o);
	}

	// ********** static methods **********

	private static HashMap<Class<?>, TypeToStringConverter> converterReg;

	public static void registerConverter(TypeToStringConverter fact) {
		for (Class<?> handledType : fact.handledTypes()) {
			converterReg.put(handledType, fact);
		}
	}

	public static String convertToString(Object o) {
		TypeToStringConverter conv = lookupConverter(o.getClass());
		return conv.toString(o);
	}

	/**
	 * * Constructs a new ListTokenizer around {@code input} and then calls
	 * {@link #convertFromString(ArgListTokenizer, Class, String, ClassLoader, String[])}
	 * . This class assumes it can read and parse the whole string, otherwise it
	 * throws a {@link ParseException}.
	 * 
	 * @param s
	 *            The String value to convert.
	 * @param requiredType
	 *            The type it should be converted to.
	 * @param context
	 *            An optional property path.
	 * @param loader
	 *            The class loader to use.
	 * @param packageSearchPath
	 *            Search path to resolve abbreviated class names.
	 * @return {@code s} converted to the required type.
	 * @param <T>
	 *            The required type.
	 */
	public static <T> T convertFromString(String s, Class<T> requiredType,
			String context, ClassLoader loader, String[] packageSearchPath) {

		ArgListTokenizer tk = new ArgListTokenizer(s);
		T value = convertFromString(tk, requiredType, context, loader,
				packageSearchPath);

		// full input read?
		if (tk.nextToken() != null) {
			throw new TypeConversionException(
					String.format(
							Util.DEF_LOCALE,
							"Can't create object for value '%s' (property path: '%s'): There is data after the last token: '%s'.",
							s, context, s.substring(tk.currTokenStart())));
		}

		return value;
	}

	/**
	 * This method is used internally to create an object as specified by the
	 * given parse tree.
	 * 
	 * @param tk
	 *            The {@link ArgListTokenizer} used to split the input String in
	 *            its parts.
	 * @param requiredType
	 *            Type the converted parse tree should be compatible with.
	 * @param context
	 *            Optional property context path.
	 * @param loader
	 *            The classloader to use.
	 * @param packageSearchPath
	 *            The package search path.
	 * @param <T>
	 *            Type of returned object.
	 * @return The object as specified in the parse tree.
	 */
	public static <T> T convertFromString(ArgListTokenizer tk,
			Class<T> requiredType, String context, ClassLoader loader,
			String[] packageSearchPath) {
		TypeToStringConverter conv = lookupConverter(requiredType);
		return conv.fromString(tk, requiredType, context, loader,
				packageSearchPath);
	}

	/**
	 * Returns the most specific converter responsible for a certain type.
	 * 
	 * @param requiredType
	 *            The required class or interface.
	 * @return the converter for this {@code requiredType}
	 * @see TypeUtil#computeClasses(Class)
	 */
	public static TypeToStringConverter lookupConverter(Class<?> requiredType) {
		// try direct lookup
		TypeToStringConverter res = converterReg.get(requiredType);
		if (res == null) {
			// no match, so we check the complete class hierarchy for the most
			// specific match
			for (Class<?> c : TypeUtil.computeClasses(requiredType)) {
				res = converterReg.get(c);
				if (res != null)
					break; // for
			}
		}

		assert res != null;
		return res;
	}

	static {
		converterReg = new HashMap<>();

		registerConverter(new TypeConverterBasicTypes());
		registerConverter(new TypeConverterJavaBean());
	}

}
