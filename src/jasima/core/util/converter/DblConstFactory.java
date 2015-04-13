package jasima.core.util.converter;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblStream;
import jasima.core.util.ArgListTokenizer;
import jasima.core.util.ArgListTokenizer.TokenType;
import jasima.core.util.Util;
import jasima.core.util.converter.TypeConverterDblStream.StreamFactory;

import java.util.ArrayList;

public class DblConstFactory implements StreamFactory {

	private static final String[] PREFIXES = { "const", "dblConst",
			DblConst.class.getName() };

	public DblConstFactory() {
		super();
	}

	@Override
	public String[] getTypePrefixes() {
		return PREFIXES;
	}

	@Override
	public DblStream stringToStream(ArgListTokenizer tk) {
		TypeToStringConverter doubleConv = TypeToStringConverter
				.lookupConverter(Double.class);
		assert doubleConv != null;

		String prefix = tk.currTokenText();
		
		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.PARENS_OPEN);

		ArrayList<Double> values = new ArrayList<Double>();

		// there has to be at least one value
		Double d = doubleConv.fromString(tk, Double.class, prefix, this
				.getClass().getClassLoader(), Util.DEF_CLASS_SEARCH_PATH);
		values.add(d);

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.SEMICOLON,
				TokenType.PARENS_CLOSE);

		// read further values until closing parenthesis
		while (tk.currTokenType() != TokenType.PARENS_CLOSE) {
			Double d2 = doubleConv.fromString(tk, Double.class, prefix, this
					.getClass().getClassLoader(), Util.DEF_CLASS_SEARCH_PATH);
			values.add(d2);

			tk.assureTokenTypes(tk.nextTokenNoWhitespace(),
					TokenType.SEMICOLON, TokenType.PARENS_CLOSE);
		}

		// convert to double[]
		double[] vs = new double[values.size()];
		for (int i = 0; i < vs.length; i++) {
			vs[i] = values.get(i).doubleValue();
		}

		DblConst dblConst = new DblConst();
		dblConst.setValues(vs);

		return dblConst;
	}

	@Override
	public String streamToString(DblStream s) {
		TypeToStringConverter doubleConv = TypeToStringConverter
				.lookupConverter(Double.class);
		assert doubleConv != null;

		DblConst dblConst = (DblConst) s;

		StringBuilder sb = new StringBuilder();
		sb.append(PREFIXES[0]).append('(');
		for (double d : dblConst.getValues()) {
			sb.append(doubleConv.toString(d)).append(';');
		}
		sb.setCharAt(sb.length() - 1, ')');

		return sb.toString();
	}

}
