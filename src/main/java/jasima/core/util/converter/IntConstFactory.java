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

import jasima.core.random.continuous.DblSequence;
import jasima.core.random.discrete.IntConst;
import jasima.core.util.Util;
import jasima.core.util.converter.ArgListTokenizer.TokenType;
import jasima.core.util.converter.TypeConverterDblStream.StreamFactory;

public class IntConstFactory implements StreamFactory {

	private static final String[] PREFIXES = { "intConst", IntConst.class.getName() };

	public IntConstFactory() {
		super();
	}

	@Override
	public String[] getTypePrefixes() {
		return PREFIXES;
	}

	@Override
	public DblSequence stringToStream(ArgListTokenizer tk) {
		TypeToStringConverter intConv = TypeToStringConverter.lookupConverter(int.class);
		assert intConv != null;

		String prefix = tk.currTokenText();

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.PARENS_OPEN);

		ArrayList<Integer> values = new ArrayList<Integer>();

		// there has to be at least one value
		Integer d = intConv.fromString(tk, Integer.class, prefix, this.getClass().getClassLoader(),
				Util.DEF_CLASS_SEARCH_PATH);
		values.add(d);

		tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.SEMICOLON, TokenType.PARENS_CLOSE);

		// read further values until closing parenthesis
		while (tk.currTokenType() != TokenType.PARENS_CLOSE) {
			Integer i2 = intConv.fromString(tk, Integer.class, prefix, this.getClass().getClassLoader(),
					Util.DEF_CLASS_SEARCH_PATH);
			values.add(i2);

			tk.assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.SEMICOLON, TokenType.PARENS_CLOSE);
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
	public String streamToString(DblSequence s) {
		TypeToStringConverter doubleConv = TypeToStringConverter.lookupConverter(Integer.class);
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
