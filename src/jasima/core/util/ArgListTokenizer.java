package jasima.core.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Splits an input string in tokens, so lists and parameter lists can be
 * recognized. This class recognizes (quoted) Strings with back-slash as an
 * escape character, white space, and as special one-character tokens: "(", ")",
 * ";", "=".
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class ArgListTokenizer {

	/**
	 * The recognized tokens.
	 */
	public static enum TokenType {
		STRING, PARENS_OPEN, PARENS_CLOSE, EQUALS, SEMICOLON, WHITE_SPACE
	};

	/**
	 * A {@code ParseException} is thrown if there were problems splitting the
	 * input string into tokens.
	 */
	public static class ParseException extends RuntimeException {

		private static final long serialVersionUID = 3473197915435659395L;

		private String msg;
		private Object[] msgParams;

		public ParseException(int errorPos, String msg, Object... msgParams) {
			super();
			this.msg = msg;
			this.msgParams = new Object[1 + msgParams.length];
			this.msgParams[0] = errorPos;
			for (int i = 0; i < msgParams.length; i++) {
				this.msgParams[i + 1] = msgParams[i];
			}
		}

		@Override
		public String getMessage() {
			return String.format(Util.DEF_LOCALE,
					"Parse error at or before position %d: " + msg, msgParams);
		}
	}

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
	 * {@link TokenType}, or {@code null}, if the end of the input was reached.
	 * This method can throw the unchecked {@link ParseException}, if there was
	 * a problem splitting the input string in tokens.
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
			throw new ParseException(tokenStart,
					"Escape character at end of input.");

		if (isQuoted) {
			if (c == '"') {
				currPos++;
			} else {
				throw new ParseException(tokenStart,
						"Quoted string not closed.");
			}
		}
		tokenEnd = currPos;
	}

	/**
	 * Returns the portion of the input text that is associated with the current
	 * token. This method does not return surrounding quotes of a quoted
	 * {@code STRING} and unescapes any escaped characters.
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
		} else
			return input.substring(start, end);
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
	 * Resets the current reading position back to beginning of the current
	 * token, so {@link #nextToken()} will see the same token again. This is
	 * useful, if a parser detects a token he can't handle but has to pass back
	 * to a parent parser for proper processing.
	 */
	public void pushBackToken() {
		if (currPos == tokenStart)
			throw new IllegalStateException(); // this works only once
		currPos = tokenStart;
	}

	/**
	 * Sets the input string to work on.
	 */
	public void setInput(String input) {
		this.input = Objects.requireNonNull(input);
	}


}
