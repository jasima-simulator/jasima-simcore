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

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.Arrays;
import java.util.Objects;

/**
 * Splits an input string in tokens, so lists and parameter lists can be
 * recognized. This class recognizes (quoted) Strings with back-slash as an
 * escape character, white space, and as special one-character tokens: "(", ")",
 * ";", "=".
 * 
 * @author Torsten Hildebrandt
 */
public class ArgListTokenizer {

	/**
	 * The recognized tokens.
	 */
	public static enum TokenType {
		STRING, PARENS_OPEN, PARENS_CLOSE, BRACKETS_OPEN, BRACKETS_CLOSE, EQUALS, SEMICOLON, WHITE_SPACE
	};

	/**
	 * A {@code ParseException} is thrown if there were problems splitting the input
	 * string into tokens.
	 */
	public static class ParseException extends RuntimeException {

		private static final long serialVersionUID = 3473197915435659395L;

		private String msg;
		private Object[] msgParams;

		public ParseException(int errorPos, String msg, Object... msgParams) {
			super();
			this.msg = msg;
			this.msgParams = new Object[1 + msgParams.length];
			this.msgParams[0] = errorPos + 1;
			for (int i = 0; i < msgParams.length; i++) {
				this.msgParams[i + 1] = msgParams[i];
			}
		}

		@Override
		public String getMessage() {
			return defFormat("Parse error at or before position %d: " + msg, msgParams);
		}
	}

	private static String ESCAPE_CHARS = "()[]=;rnt \\\"";
	private static String CHARS_TO_ESCAPE = "()[]=;\r\n\t \\\"";

	private String input;
	private int currPos;

	private int tokenStart;
	private int tokenEnd;
	private TokenType tokenType;
	private boolean tokenContainsEscapedChars;

	public ArgListTokenizer() {
		this(null);
	}

	public ArgListTokenizer(String input) {
		super();
		reset();
		this.input = input;
	}

	protected void reset() {
		currPos = 0;

		tokenStart = tokenEnd = -1;
		tokenType = null;
		tokenContainsEscapedChars = false;
	}

	/**
	 * Returns the next token that is <em>not</em> whitespace.
	 * 
	 * @return The type of the next token that is not {@code WHITE_SPACE}.
	 */
	public TokenType nextTokenNoWhitespace() {
		TokenType t;
		while ((t = nextToken()) == TokenType.WHITE_SPACE) {
			// do nothing
		}
		return t;
	}

	/**
	 * Returns the next token in the input String. This is on of the values of
	 * {@link TokenType}, or {@code null}, if the end of the input was reached. This
	 * method can throw the unchecked {@link ParseException}, if there was a problem
	 * splitting the input string in tokens.
	 * 
	 * @return The current token's type.
	 */
	public TokenType nextToken() {
		tokenStart = currPos;
		tokenContainsEscapedChars = false;

		// end of input?
		if (currPos >= input.length()) {
			tokenEnd = tokenStart = currPos;
			currPos++;
			tokenType = null;
			return null;
		}

		char c = input.charAt(currPos);
		currPos++;
		tokenEnd = currPos;

		switch (c) {
		case '=':
			tokenType = TokenType.EQUALS;
			break;
		case '(':
			tokenType = TokenType.PARENS_OPEN;
			break;
		case ')':
			tokenType = TokenType.PARENS_CLOSE;
			break;
		case '[':
			tokenType = TokenType.BRACKETS_OPEN;
			break;
		case ']':
			tokenType = TokenType.BRACKETS_CLOSE;
			break;
		case ';':
			tokenType = TokenType.SEMICOLON;
			break;
		case ' ':
		case '\t':
		case '\r':
		case '\n':
			tokenType = TokenType.WHITE_SPACE;
			whiteSpace: while (currPos < input.length()) {
				switch (input.charAt(currPos)) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break; // switch
				default:
					// found end of whitespace
					tokenEnd = currPos;
					break whiteSpace;
				}

				currPos++;
			}
			break;
		default:
			tokenType = TokenType.STRING;
			readStringToken(c);
		}

		return tokenType;
	}

	private void readStringToken(char firstChar) {
		boolean isQuoted = false;
		if (firstChar == '"') {
			isQuoted = true;
		}

		boolean escape = firstChar == '\\';
		char c = 0;
		loop: while (currPos < input.length()) {
			c = input.charAt(currPos);
			if (escape) {
				c = 0;
				escape = false;
				tokenContainsEscapedChars = true;
			}

			if (isQuoted) {
				switch (c) {
				case '\\':
					escape = true;
					break;
				case '"':
					// found end of quoted String
					break loop;
				default: // do nothing
				}
			} else {
				switch (c) {
				case '\\':
					escape = true;
					break;
				case '"':// begin of new quoted String
				case '(':
				case ')':
				case '[':
				case ']':
				case ';':
				case '=':
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break loop;
				default: // do nothing
				}
			}

			currPos++;
		}

		if (escape)
			throw new ParseException(tokenStart, "escape character at end of input '%s'", input);

		if (isQuoted) {
			if (c == '"') {
				currPos++;
			} else {
				throw new ParseException(tokenStart, "quoted string not closed in input '%s'", input);
			}
		}
		tokenEnd = currPos;
	}

	/**
	 * Returns the portion of the input text that is associated with the current
	 * token. This method does not return surrounding quotes of a quoted
	 * {@code STRING} and unescapes any escaped characters.
	 * 
	 * @return The current token's text.
	 */
	public String currTokenText() {
		// was pushBackToken() called before
		if (currPos == tokenStart)
			throw new IllegalStateException();

		if (tokenType == null)
			return null; // end of input

		int start = tokenStart;
		int end = tokenEnd;
		if (input.charAt(start) == '"') {
			assert tokenType == TokenType.STRING;
			assert input.charAt(end - 1) == '"';
			start++;
			end--;
			assert start <= end;
		}
		if (tokenContainsEscapedChars) {
			boolean escape = false;
			StringBuilder sb = new StringBuilder(end - start);
			for (int i = start; i < end; i++) {
				char c = input.charAt(i);
				if (escape) {
					if (ESCAPE_CHARS.indexOf(c) < 0) {
						throw new ParseException(i, "invalid escaped character in input '%s'", input);
					} else {
						// replace with special values if needed
						if (c == 't') {
							c = '\t';
						} else if (c == 'r') {
							c = '\r';
						} else if (c == 'n') {
							c = '\n';
						}
					}
					sb.append(c);
					escape = false;
				} else {
					if (c != '\\') {
						sb.append(c);
					} else {
						escape = true;
					}
				}
			}

			return sb.toString();
		} else {
			return input.substring(start, end);
		}
	}

	public TokenType currTokenType() {
		// was pushBackToken() called before?
		if (currPos == tokenStart)
			throw new IllegalStateException();

		return tokenType;
	}

	public int currTokenStart() {
		// was pushBackToken() called before?
		if (currPos == tokenStart)
			throw new IllegalStateException();

		return tokenStart;
	}

	public int currTokenEnd() {
		// was pushBackToken() called before?
		if (currPos == tokenStart)
			throw new IllegalStateException();

		return tokenEnd;
	}

	/**
	 * Resets the current reading position back to beginning of the current token,
	 * so {@link #nextToken()} will see the same token again. This is useful, if a
	 * parser detects a token he can't handle but has to pass back to a parent
	 * parser for proper processing.
	 */
	public void pushBackToken() {
		if (currPos == tokenStart)
			throw new IllegalStateException(); // this works only once
		currPos = tokenStart;
	}

	/**
	 * Sets the input string to work on.
	 * 
	 * @param input The input string.
	 */
	public void setInput(String input) {
		this.input = Objects.requireNonNull(input);
	}

	/**
	 * Checks whether the actual token's type matches a certain set of expected
	 * types. If the types do not match, then a {@link ParseException} is raised.
	 * 
	 * @param actual   The current token's type.
	 * @param expected All token types that are currently valid.
	 * @throws ParseException If {@code actual} if not contained in
	 *                        {@code expected}.
	 */
	public void assureTokenTypes(TokenType actual, TokenType... expected) throws ParseException {
		for (TokenType e : expected) {
			if (actual == e)
				return;
		}

		String msg = "expected one of: %s, but found: %s, '%s'";
		if (expected.length == 1)
			msg = "expected %s, but found: %s, '%s'";
		throw new ParseException(currTokenStart(), msg, Arrays.deepToString(expected), actual, currTokenText());
	}

	public static String escapeString(String raw) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);

			if (CHARS_TO_ESCAPE.indexOf(c) >= 0) {
				// needs escaping
				sb.append('\\');
				if (c == '\t') {
					sb.append('t');
				} else if (c == '\r') {
					sb.append('r');
				} else if (c == '\n') {
					sb.append('n');
				} else {
					sb.append(c);
				}
			} else {
				// TODO: avoid copying if no character has to be escaped?
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static String quoteString(String raw) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);

			if (CHARS_TO_ESCAPE.indexOf(c) >= 0 && "(); ".indexOf(c) < 0) {
				// needs escaping
				sb.append('\\');
				if (c == '\t') {
					sb.append('t');
				} else if (c == '\r') {
					sb.append('r');
				} else if (c == '\n') {
					sb.append('n');
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}

		return '"' + sb.toString() + '"';
	}

}
