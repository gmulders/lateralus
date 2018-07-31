package ${packageName};

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