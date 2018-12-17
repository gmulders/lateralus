package ${packageName};

import java.io.IOException;

/**
 * Interface representing a lexer.
 */
public interface Lexer {
	Token nextToken() throws LexerException;
}
