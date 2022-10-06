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