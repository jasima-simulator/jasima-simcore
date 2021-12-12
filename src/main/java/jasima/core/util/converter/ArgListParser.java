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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import jasima.core.util.converter.ArgListTokenizer.ParseException;
import jasima.core.util.converter.ArgListTokenizer.TokenType;
import jasima.core.util.i18n.I18n;

public class ArgListParser {

	public static class ParseTree {
		private String classOrXmlName;
		private Map<String, ParseTree> params;

		public ParseTree(String classOrXmlName, Map<String, ParseTree> params) {
			super();
			this.classOrXmlName = classOrXmlName;
			this.params = params;
		}

		public String getClassOrXmlName() {
			return classOrXmlName;
		}

		public void setClassOrXmlName(String classOrXmlName) {
			this.classOrXmlName = classOrXmlName;
		}

		public Map<String, ParseTree> getParams() {
			return params;
		}

		public void setParams(Map<String, ParseTree> params) {
			this.params = params;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			toString(this, sb);
			return sb.toString();
		}

		public static StringBuilder toString(ParseTree t, StringBuilder sb) {
			sb.append(t.getClassOrXmlName());

			if (t.getParams() != null) {
				sb.append('(');
				for (Entry<String, ParseTree> e : t.getParams().entrySet()) {
					sb.append(e.getKey()).append('=');
					toString(e.getValue(), sb);
					sb.append(';');
				}
				if (t.getParams().entrySet().size() != 0) {
					sb.setCharAt(sb.length() - 1, ')');
				} else {
					sb.append(')');
				}
			}
			return sb;
		}

	}

	private final ArgListTokenizer tk;

	public ArgListParser(ArgListTokenizer tk) {
		super();
		this.tk = tk;
	}

	/**
	 * Parses class/property definitions in a form similar to:
	 * {@code "a.b.c.ATC(prop1=abc;prop2=1.23;prop3=Test(abc=xyz;def=123))"} All
	 * names and values are returned as Strings.
	 */
	public ParseTree parseClassAndPropDef() {
		// required class name
		TokenType token = tk.nextTokenNoWhitespace();
		assureTokenTypes(token, TokenType.STRING);
		String className = tk.currTokenText();
		Map<String, ParseTree> params = null;

		// optional parameter list in round parenthesis
		token = tk.nextTokenNoWhitespace();
		if (token == TokenType.PARENS_OPEN) {
			params = new LinkedHashMap<>();
			while (true) {
				// name
				token = tk.nextTokenNoWhitespace();
				assureTokenTypes(token, TokenType.STRING, TokenType.PARENS_CLOSE);
				if (token == TokenType.PARENS_CLOSE)
					break; // end of parameter list
				String paramName = tk.currTokenText();

				// equals
				assureTokenTypes(tk.nextTokenNoWhitespace(), TokenType.EQUALS);

				// value: create sub-parser
				ParseTree paramValue = new ArgListParser(tk).parseClassAndPropDef();
				assert paramValue != null;

				// save parsed parameter
				params.put(paramName, paramValue);

				// more parameters?
				token = tk.nextTokenNoWhitespace();
				assureTokenTypes(token, TokenType.SEMICOLON, TokenType.PARENS_CLOSE);
				if (token == TokenType.SEMICOLON) {
					// nothing special, start next iteration
				} else if (token == TokenType.PARENS_CLOSE) {
					break; // found end of list
				}
			}
		} else {
			// let parent handle it
			tk.pushBackToken();
		}

		return new ParseTree(className, params);
	}

	private void assureTokenTypes(TokenType actual, TokenType... expected) {
		for (TokenType e : expected) {
			if (actual == e)
				return;
		}

		String msg = "expected one of: %s, but found: %s, '%s'";
		if (expected.length == 1)
			msg = "expected %s, but found: %s, '%s'";
		throw new ParseException(tk.currTokenStart(), msg, Arrays.deepToString(expected), actual, tk.currTokenText());
	}

	// ************* static methods below ******************************

	/**
	 * Constructs a new ListTokenizer around {@code input} and then calls
	 * {@link #parseClassAndPropDef()}. This class assumes it can read and parse the
	 * whole string, otherwise it throws a {@link ParseException}.
	 * 
	 * @param input The input string to parse.
	 * @return The
	 */
	public static ParseTree parseClassAndPropDef(String input) throws ParseException {
		ArgListTokenizer tk = new ArgListTokenizer(input);
		ParseTree res = new ArgListParser(tk).parseClassAndPropDef();

		// full input read?
		if (tk.nextToken() != null) {
			throw new ParseException(tk.currTokenStart(),
					I18n.defFormat("There is data after the last token: '%s'.", input.substring(tk.currTokenStart())));
		}

		return res;
	}

}
