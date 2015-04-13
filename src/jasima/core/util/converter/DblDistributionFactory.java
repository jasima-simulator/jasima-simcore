package jasima.core.util.converter;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.util.ArgListTokenizer;
import jasima.core.util.ArgListTokenizer.TokenType;
import jasima.core.util.Util;
import jasima.core.util.converter.TypeConverterDblStream.StreamFactory;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class DblDistributionFactory implements StreamFactory {

	private static final String[] PREFIXES = { "dblExp",
			DblDistribution.class.getName() };

	public DblDistributionFactory() {
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
		Double v1 = doubleConv.fromString(tk, Double.class, prefix, this
				.getClass().getClassLoader(), Util.DEF_CLASS_SEARCH_PATH);
		values.add(v1);

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.PARENS_CLOSE);

		return new DblDistribution(
				new ExponentialDistribution(v1.doubleValue()));
	}

	@Override
	public String streamToString(DblStream s) {
		DblDistribution dist = (DblDistribution) s;
		assert dist.getDistribution() instanceof ExponentialDistribution;

		TypeToStringConverter doubleConv = TypeToStringConverter
				.lookupConverter(Double.class);
		assert doubleConv != null;

		StringBuilder sb = new StringBuilder();
		sb.append(PREFIXES[0]).append('(');

		double v = ((ExponentialDistribution) dist.getDistribution()).getMean();
		sb.append(doubleConv.toString(v));

		sb.setCharAt(sb.length() - 1, ')');

		return sb.toString();
	}

}
