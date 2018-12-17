package io.lateralus.lexergenerator.core.parser.description.lexer;

/**
 * Interface representing a lexer.
 */
public interface Lexer {
	Token nextToken() throws LexerException;
}
