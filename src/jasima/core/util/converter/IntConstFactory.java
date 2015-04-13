package jasima.core.util.converter;

import jasima.core.random.continuous.DblStream;
import jasima.core.random.discrete.IntConst;
import jasima.core.util.ArgListTokenizer;
import jasima.core.util.ArgListTokenizer.TokenType;
import jasima.core.util.Util;
import jasima.core.util.converter.TypeConverterDblStream.StreamFactory;

import java.util.ArrayList;

public class IntConstFactory implements StreamFactory {

	private static final String[] PREFIXES = { "intConst",
			IntConst.class.getName() };

	public IntConstFactory() {
		super();
	}

	@Override
	public String[] getTypePrefixes() {
		return PREFIXES;
	}

	@Override
	public DblStream stringToStream(ArgListTokenizer tk) {
		TypeToStringConverter intConv = TypeToStringConverter
				.lookupConverter(int.class);
		assert intConv != null;

		String prefix = tk.currTokenText();

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.PARENS_OPEN);

		ArrayList<Integer> values = new ArrayList<Integer>();

		// there has to be at least one value
		Integer d = intConv.fromString(tk, Integer.class, prefix, this
				.getClass().getClassLoader(), Util.DEF_CLASS_SEARCH_PATH);
		values.add(d);

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.SEMICOLON,
				TokenType.PARENS_CLOSE);

		// read further values until closing parenthesis
		while (tk.currTokenType() != TokenType.PARENS_CLOSE) {
			Integer i2 = intConv.fromString(tk, Integer.class, prefix, this
					.getClass().getClassLoader(), Util.DEF_CLASS_SEARCH_PATH);
			values.add(i2);

			tk.assureTokenTypes(tk.nextTokenNoWhitespace(),
					TokenType.SEMICOLON, TokenType.PARENS_CLOSE);
		}

		// convert to double[]
		int[] vs = new int[values.size()];
		for (int i = 0; i < vs.length; i++) {
			vs[i] = values.get(i).intValue();
		}

		IntConst intConst = new IntConst();
		intConst.setValues(vs);

		return intConst;
	}

	@Override
	public String streamToString(DblStream s) {
		TypeToStringConverter doubleConv = TypeToStringConverter
				.lookupConverter(Integer.class);
		assert doubleConv != null;

		IntConst dblConst = (IntConst) s;

		StringBuilder sb = new StringBuilder();
		sb.append(PREFIXES[0]).append('(');
		for (double d : dblConst.getValues()) {
			sb.append(doubleConv.toString(d)).append(';');
		}
		sb.setCharAt(sb.length() - 1, ')');

		return sb.toString();
	}

}
