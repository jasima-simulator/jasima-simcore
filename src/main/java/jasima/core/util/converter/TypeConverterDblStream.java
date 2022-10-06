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

import java.util.HashMap;

import jasima.core.random.continuous.DblSequence;
import jasima.core.util.converter.ArgListTokenizer.TokenType;
import jasima.core.util.i18n.I18n;

public class TypeConverterDblStream extends TypeToStringConverter {

	public interface StreamFactory {

		public String[] getTypePrefixes();

		public DblSequence stringToStream(ArgListTokenizer tk);

		public String streamToString(DblSequence s);
	}

	public TypeConverterDblStream() {
		super();
	}

	@Override
	public Class<?>[] handledTypes() {
		return new Class<?>[] { DblSequence.class };
	}

	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> requiredType, String context, ClassLoader loader,
			String[] packageSearchPath) throws ClassCastException {
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.STRING);

		String prefix = tk.currTokenText().trim().toLowerCase(I18n.DEF_LOCALE);
		StreamFactory fact = lookupFactory(prefix);

		DblSequence stream = fact.stringToStream(tk);
		return requiredType.cast(stream);
	}

	@Override
	public String toString(Object o) {
		DblSequence s = (DblSequence) o;
		StreamFactory fact = lookupFactory(s.getClass().getName());
		return fact.streamToString(s);
	}

	private static HashMap<String, StreamFactory> streamFactoryReg = new HashMap<String, StreamFactory>();

	public static void registerStreamFactory(StreamFactory fact) {
		for (String s : fact.getTypePrefixes()) {
			streamFactoryReg.put(s, fact);
		}
	}

	public static StreamFactory lookupFactory(String prefix) {
		return streamFactoryReg.get(prefix);
	}

	static {
		registerConverter(new TypeConverterDblStream());

		@SuppressWarnings("unused")
		Class<? extends StreamFactory> c;

		// trigger class load, so sub-classes can register themselves
		// c = DblConstDef.class;
		// c = DblExponentialDef.class;
		// c = DblUniformDef.class;
		// c = DblTriangularDef.class;
		// c = IntUniformDef.class;
		// c = IntEmpDef.class;
		// c = IntConstDef.class;
	}

}
