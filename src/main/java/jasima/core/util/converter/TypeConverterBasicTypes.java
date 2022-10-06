/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jasima.core.util.converter;

import static jasima.core.util.i18n.I18n.defFormat;

import jasima.core.util.TypeUtil.TypeConversionException;
import jasima.core.util.converter.ArgListTokenizer.TokenType;

public class TypeConverterBasicTypes extends TypeToStringConverter {

	@Override
	public Class<?>[] handledTypes() {
		return new Class<?>[] { int.class, Integer.class, long.class, Long.class, double.class, Double.class,
				boolean.class, Boolean.class, byte.class, Byte.class, short.class, Short.class, float.class,
				Float.class, char.class, Character.class, Enum.class, String.class };
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> klass, String context, ClassLoader loader,
			String[] packageSearchPath) throws NumberFormatException, TypeConversionException {
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
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("1"))
				return (T) Boolean.TRUE;
			if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("0"))
				return (T) Boolean.FALSE;
			throw new TypeConversionException(defFormat("Can't convert '%s' to bool.", s));
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
				return (T) Character.valueOf(s.charAt(0));
		}

		throw new AssertionError(); // should never be reached
	}

}