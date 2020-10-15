package io.lateralus.lexergenerator.parser.lexer;

/**
 * Enum containing the possible token types.
 */
public enum TokenType {
	EOF(-1),
	NEW_LINE(1),
	WHITE_SPACE(1),
	LEXER_CLASS(1),
	TOKEN_NAME(1),
	REGEX_START(2),
	NEXT_STATE(1),
	REGEX_END(1),
	REGEX_PART(2),
	REGEX_SINGLE_QUOTE(2),
	REGEX_DOUBLE_BACKSLASH(2),
	REGEX_BACKSLASH(2),
	REGEX(-1);

	private final int lexerState;

	TokenType(final int lexerState) {
		this.lexerState = lexerState;
	}

	public int lexerState() {
		return lexerState;
	}
}
