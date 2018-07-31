package ${packageName};

import java.io.IOException;

public interface LexerReader {
	int peek() throws IOException;
	void eat() throws IOException;

	int getCurrentLineNumber();
	int getCurrentColumnNumber();

	String readLexeme();

	void startLexeme();
}
