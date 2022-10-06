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

import java.util.ArrayList;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblSequence;
import jasima.core.util.Util;
import jasima.core.util.converter.ArgListTokenizer.TokenType;
import jasima.core.util.converter.TypeConverterDblStream.StreamFactory;

public class DblDistributionFactory implements StreamFactory {

	private static final String[] PREFIXES = { "dblExp", DblDistribution.class.getName() };

	public DblDistributionFactory() {
		super();
	}

	@Override
	public String[] getTypePrefixes() {
		return PREFIXES;
	}

	@Override
	public DblSequence stringToStream(ArgListTokenizer tk) {
		TypeToStringConverter doubleConv = TypeToStringConverter.lookupConverter(Double.class);
		assert doubleConv != null;

		String prefix = tk.currTokenText();

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.PARENS_OPEN);

		ArrayList<Double> values = new ArrayList<Double>();

		// there has to be at least one value
		Double v1 = doubleConv.fromString(tk, Double.class, prefix, this.getClass().getClassLoader(),
				Util.DEF_CLASS_SEARCH_PATH);
		values.add(v1);

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.PARENS_CLOSE);

		return new DblDistribution(new ExponentialDistribution(v1.doubleValue()));
	}

	@Override
	public String streamToString(DblSequence s) {
		DblDistribution dist = (DblDistribution) s;
		assert dist.getDistribution() instanceof ExponentialDistribution;

		TypeToStringConverter doubleConv = TypeToStringConverter.lookupConverter(Double.class);
		assert doubleConv != null;

		StringBuilder sb = new StringBuilder();
		sb.append(PREFIXES[0]).append('(');

		double v = ((ExponentialDistribution) dist.getDistribution()).getMean();
		sb.append(doubleConv.toString(v));

		sb.setCharAt(sb.length() - 1, ')');

		return sb.toString();
	}

}
