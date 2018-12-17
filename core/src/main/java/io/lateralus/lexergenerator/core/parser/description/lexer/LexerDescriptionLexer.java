package io.lateralus.lexergenerator.core.parser.description.lexer;

/**
 * A {@link Lexer} implementation that handles the escaping in the regular expression.
 */
public class LexerDescriptionLexer implements Lexer {

	private Lexer delegate;

	public LexerDescriptionLexer(LexerReader reader) {
		this.delegate = new BaseLexerDescriptionLexer(reader);
	}

	@Override
	public Token nextToken() throws LexerException {
		Token token = delegate.nextToken();

		while (token.is(TokenType.WHITE_SPACE, TokenType.NEW_LINE)) {
			token = delegate.nextToken();
		}

		if (!token.is(TokenType.REGEX_START)) {
			return token;
		}

		// Temporarily store the line and column numbers.
		int lineNumber = token.getLineNumber();
		int columnNumber = token.getColumnNumber();

		// Create a buffer to store the value for the string.
		StringBuilder buffer = new StringBuilder();
		while (!(token = delegate.nextToken()).is(TokenType.REGEX_END)) {
			switch (token.getTokenType()) {
				case REGEX_PART:
				case REGEX_DOUBLE_BACKSLASH:
				case REGEX_BACKSLASH:
					buffer.append(token.getValue());
					break;

				case REGEX_SINGLE_QUOTE:
					buffer.append("'");
					break;

				default:
					throw new LexerException("Found unexpected token '" + token.getTokenType() + "'.",
							token.getLineNumber(), token.getColumnNumber());
			}
		}

		return new Token(lineNumber, columnNumber, buffer.toString(), TokenType.REGEX);
	}

}
