package io.lateralus.lexergenerator.parser.lexer;

/**
 * Interface representing a lexer.
 */
public interface Lexer {
	Token nextToken() throws LexerException;
}
