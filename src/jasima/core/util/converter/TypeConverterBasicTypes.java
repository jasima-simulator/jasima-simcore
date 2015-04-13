package jasima.core.util.converter;

import jasima.core.util.ArgListTokenizer;
import jasima.core.util.ArgListTokenizer.TokenType;
import jasima.core.util.TypeUtil.TypeConversionException;
import jasima.core.util.Util;

public class TypeConverterBasicTypes extends TypeToStringConverter {

	@Override
	public Class<?>[] handledTypes() {
		return new Class<?>[] { int.class, Integer.class, long.class,
				Long.class, double.class, Double.class, boolean.class,
				Boolean.class, byte.class, Byte.class, short.class,
				Short.class, float.class, Float.class, char.class,
				Character.class, Enum.class, String.class };
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> klass,
			String context, ClassLoader loader, String[] packageSearchPath)
			throws NumberFormatException, TypeConversionException {
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.STRING);
		String s = tk.currTokenText();

		if (klass == String.class) {
			return (T) s;
		}

		if (klass == int.class || klass == Integer.class) {
			return (T) Integer.valueOf(s);
		}

		if (klass == long.class || klass == Long.class) {
			return (T) Long.valueOf(s);
		}

		if (klass == double.class || klass == Double.class) {
			return (T) Double.valueOf(s);
		}

		if (klass == boolean.class || klass == Boolean.class) {
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes")
					|| s.equalsIgnoreCase("1"))
				return (T) Boolean.TRUE;
			if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no")
					|| s.equalsIgnoreCase("0"))
				return (T) Boolean.FALSE;
			throw new TypeConversionException(String.format(Util.DEF_LOCALE,
					"Can't convert '%s' to bool.", s));
		}

		if (klass.isEnum()) {
			return (T) Enum.valueOf(klass.asSubclass(Enum.class), s);
		}

		if (klass == byte.class || klass == Byte.class) {
			return (T) Byte.valueOf(s);
		}

		if (klass == short.class || klass == Short.class) {
			return (T) Short.valueOf(s);
		}

		if (klass == float.class || klass == Float.class) {
			return (T) Float.valueOf(s);
		}

		if (klass == char.class || klass == Character.class) {
			if (s.length() == 1)
				return (T) new Character(s.charAt(0));
		}

		throw new AssertionError(); // should never be reached
	}

}