package ${packageName};

import java.io.IOException;

public interface Lexer {
	Token determineNextToken() throws IOException, LexerException;
}
