package org.gertje.regular.lexer;

public class Token {
	private int lineNumber;
	private int columnNumber;
	private String value;
	private TestTokenType tokenType;

	public Token(int lineNumber, int columnNumber, String value, TestTokenType tokenType) {
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.value = value;
		this.tokenType = tokenType;
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

	public TestTokenType getTokenType() {
		return tokenType;
	}
}
