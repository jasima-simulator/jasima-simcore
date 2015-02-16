package jasima.core.util;

import java.util.Objects;

/**
 * Splits an input string in tokens, so lists and parameter lists can be
 * recognized. This class recognizes (quoted) Strings with back-slash as an
 * escape character, white space, and as special one-character tokens: "(", ")",
 * ";", "=".
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class ListTokenizer {

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

		private int errorPos;
		private String msg;

		public ParseException(int errorPos, String msg) {
			super();
			this.errorPos = errorPos;
			this.msg = msg;
		}

		@Override
		public String getMessage() {
			return String.format(Util.DEF_LOCALE,
					"Parse error after position %d: %s", errorPos, msg);
		}

	}

	private String input;
	private int currPos;

	private int tokenStart;
	private int tokenEnd;
	private TokenType tokenType;
	private boolean tokenContainsEscapedChars;

	public ListTokenizer() {
		this(null);
	}

	public ListTokenizer(String input) {
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
		if (currPos >= input.length())
			return null;

		tokenStart = currPos;
		tokenContainsEscapedChars = false;

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
			tokenStart++;
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
				tokenEnd = currPos;
				currPos++;
			} else {
				throw new ParseException(tokenStart,
						"Quoted string not closed.");
			}
		} else {
			tokenEnd = currPos;
		}
	}

	/**
	 * Returns the portion of the input text that is associated with the current
	 * token.
	 */
	public String currTokenText() {
		if (tokenContainsEscapedChars) {
			boolean escape = false;
			StringBuilder sb = new StringBuilder(tokenEnd - tokenStart);
			for (int i = tokenStart; i < tokenEnd; i++) {
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
			return input.substring(tokenStart, tokenEnd);
	}

	/**
	 * Sets the input string to work on.
	 */
	public void setInput(String input) {
		this.input = Objects.requireNonNull(input);
	}

}
