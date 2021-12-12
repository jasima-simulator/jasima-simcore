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

import java.time.Instant;

import jasima.core.util.TypeUtil.TypeConversionException;
import jasima.core.util.converter.ArgListTokenizer.TokenType;

public class TypeConverterDateTime extends TypeToStringConverter {

	@Override
	public Class<?>[] handledTypes() {
		return new Class<?>[] { Instant.class }; // TODO: add Data, Duration, Period, ...
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> klass, String context, ClassLoader loader,
			String[] packageSearchPath) throws NumberFormatException, TypeConversionException {
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.STRING);
		String s = tk.currTokenText();

		if (klass == Instant.class) {
			return (T) Instant.parse(s);
		}

		throw new AssertionError(); // should never be reached
	}

}