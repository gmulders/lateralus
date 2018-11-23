package org.gertje.regular.parser.description.lexer;

/**
 * Interface representing a lexer.
 */
public interface Lexer {
	Token nextToken() throws LexerException;
}
