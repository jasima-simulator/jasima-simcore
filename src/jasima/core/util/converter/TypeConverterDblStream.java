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

import jasima.core.random.continuous.DblStream;
import jasima.core.util.ArgListTokenizer;
import jasima.core.util.Util;
import jasima.core.util.ArgListTokenizer.TokenType;

import java.util.HashMap;

public class TypeConverterDblStream extends TypeToStringConverter {

	public interface StreamFactory {

		public String[] getTypePrefixes();

		public DblStream stringToStream(ArgListTokenizer tk);

		public String streamToString(DblStream s);
	}

	public TypeConverterDblStream() {
		super();
	}

	@Override
	public Class<?>[] handledTypes() {
		return new Class<?>[] { DblStream.class };
	}

	@Override
	public <T> T fromString(ArgListTokenizer tk, Class<T> requiredType,
			String context, ClassLoader loader, String[] packageSearchPath)
			throws ClassCastException {
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.STRING);

		String prefix = tk.currTokenText().trim().toLowerCase(Util.DEF_LOCALE);
		StreamFactory fact = lookupFactory(prefix);

		DblStream stream = fact.stringToStream(tk);
		return requiredType.cast(stream);
	}

	@Override
	public String toString(Object o) {
		DblStream s = (DblStream) o;
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
