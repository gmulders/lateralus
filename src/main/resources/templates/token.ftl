package ${packageName};

import java.util.Arrays;

/**
 * Represents a token (lexeme).
 */
public class Token {
	private int lineNumber;
	private int columnNumber;
	private String value;
	private TokenType tokenType;

	public Token(int lineNumber, int columnNumber, String value, TokenType tokenType) {
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.value = value;
		this.tokenType = tokenType;
	}

	/**
	 * Checks whether {@code this} {@link Token} is of the given {@link TokenType}.
	 * @param tokenType The {@link TokenType} to check against
	 * @return {@code true} if {@code this} {@link Token} is of the given {@link TokenType}.
	 */
	public boolean is(TokenType tokenType) {
		return this.tokenType == tokenType;
	}

	/**
	 * Checks whether {@code this} {@link Token} is any of the given {@link TokenType}s.
	 * @param tokenTypes The {@link TokenType}s to check against
	 * @return {@code true} if {@code this} {@link Token} is any of the given {@link TokenType}s.
	 */
	public boolean is(TokenType... tokenTypes) {
		return Arrays.stream(tokenTypes)
			.anyMatch(this::is);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public String getValue() {
		return value;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	@Override
	public String toString() {
		return "Token{" +
				"lineNumber=" + lineNumber +
				", columnNumber=" + columnNumber +
				", value='" + value + '\'' +
				", tokenType=" + tokenType +
				'}';
	}
}
