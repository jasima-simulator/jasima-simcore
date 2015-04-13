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
