package io.lateralus.lexergenerator.parser.lexer;

import java.io.IOException;

/**
 * Represents a "Reader" for the lexer. This reader differs (besides some Lexer specific methods) from the normal Java
 * {@link java.io.Reader} in the value returned, in that it returns codepoints instead of characters (surrogates).
 */
public interface LexerReader {

	/**
	 * Peeks and returns the next codepoint from the stream.
	 * @return The next codepoint.
	 */
	int peek() throws IOException;

	/**
	 * Removes the next codepoint from the stream, without returning it.
	 */
	void eat() throws IOException;

	/**
	 * Returns the line number of the next codepoint to be read. Should be 1-indexed.
	 * @return the line number.
	 */
	int getCurrentLineNumber();

	/**
	 * Returns the column number of the next codepoint to be read. Should be 1-indexed.
	 * @return the column number.
	 */
	int getCurrentColumnNumber();

	/**
	 * Returns the actual matched lexeme.
	 * @return The lexeme.
	 */
	String readLexeme();

	/**
	 * Marks the start of a new lexeme.
	 */
	void markStart();

	/**
	 * Marks the end of a lexeme. This method can be called more then once (when the lexeme increases in length).
	 */
	void markEnd();
}
