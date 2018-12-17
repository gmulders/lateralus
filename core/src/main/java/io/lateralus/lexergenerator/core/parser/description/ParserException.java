package io.lateralus.lexergenerator.core.parser.description;

import io.lateralus.lexergenerator.core.parser.description.lexer.Token;

/**
 * An exception that can be thrown from the {@link LexerDescriptionParser}.
 */
public class ParserException extends Exception {

	private final Token token;

	public ParserException(final String message, final Throwable cause) {
		super(message, cause);
		token = null;
	}

	public ParserException(String message, Token token) {
		super(message);
		this.token = token;
	}

	public ParserException(String message, Token token, Throwable cause) {
		super(message, cause);
		this.token = token;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + (token != null ? " " + token : "");
	}
}
