package org.gertje.regular.lexer;

public class TestTokenType implements TokenType {

	private int ordinal;

	private int lexerState;

	public TestTokenType(int ordinal, int lexerState) {
		this.ordinal = ordinal;
		this.lexerState = lexerState;
	}

	@Override
	public int ordinal() {
		return ordinal;
	}

	@Override
	public int lexerState() {
		return lexerState;
	}
}
