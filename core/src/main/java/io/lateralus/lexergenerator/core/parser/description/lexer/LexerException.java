package io.lateralus.lexergenerator.core.parser.description.lexer;

/**
 * Exception that can be thrown from a {@link Lexer}.
 */
public class LexerException extends Exception {

	private final int lineNumber;
	private final int columnNumber;

	public LexerException(final String message, final int currentLineNumber, final int currentColumnNumber) {
		super(message);
		this.columnNumber = currentColumnNumber;
		this.lineNumber = currentLineNumber;
	}

	public LexerException(final String message, final int currentLineNumber, final int currentColumnNumber,
			final Throwable cause) {
		super(message, cause);
		this.columnNumber = currentColumnNumber;
		this.lineNumber = currentLineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}
}
