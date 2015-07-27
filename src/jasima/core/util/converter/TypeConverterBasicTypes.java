/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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