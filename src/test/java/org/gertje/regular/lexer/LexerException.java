package org.gertje.regular.lexer;

public class LexerException extends Exception {

	private int lineNumber;
	private int columnNumber;

	public LexerException(String message, int currentLineNumber, int currentColumnNumber) {
		super(message);
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
