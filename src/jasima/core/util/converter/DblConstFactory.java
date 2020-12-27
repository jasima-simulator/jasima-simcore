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

import java.util.ArrayList;

import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblSequence;
import jasima.core.util.Util;
import jasima.core.util.converter.ArgListTokenizer.TokenType;
import jasima.core.util.converter.TypeConverterDblStream.StreamFactory;

public class DblConstFactory implements StreamFactory {

	private static final String[] PREFIXES = { "const", "dblConst", DblConst.class.getName() };

	public DblConstFactory() {
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
		Double d = doubleConv.fromString(tk, Double.class, prefix, this.getClass().getClassLoader(),
				Util.DEF_CLASS_SEARCH_PATH);
		values.add(d);

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.SEMICOLON, TokenType.PARENS_CLOSE);

		// read further values until closing parenthesis
		while (tk.currTokenType() != TokenType.PARENS_CLOSE) {
			Double d2 = doubleConv.fromString(tk, Double.class, prefix, this.getClass().getClassLoader(),
					Util.DEF_CLASS_SEARCH_PATH);
			values.add(d2);

			tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.SEMICOLON, TokenType.PARENS_CLOSE);
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
	public String streamToString(DblSequence s) {
		TypeToStringConverter doubleConv = TypeToStringConverter.lookupConverter(Double.class);
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
