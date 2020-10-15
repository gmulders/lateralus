package io.lateralus.lexergenerator.core.testlexer;

import java.io.IOException;

public class TestLexer {

	private LexerReader reader;

	private final int[][] transitions;

	private final boolean[] isEndState;

	private final TestTokenType[] tokenTypes;

	private final TestTokenType eofTokenType;

	private final int startState;

	private int lexerState;

	private int[] alphabetMap;

	private int errorState;

	public TestLexer(LexerReader reader, int[][] transitions, int[] alphabetMap, boolean[] isEndState,
			TestTokenType[] tokenTypes, TestTokenType eofTokenType, int startState, int lexerState, int errorState) {
		this.reader = reader;
		this.transitions = transitions;
		this.isEndState = isEndState;
		this.tokenTypes = tokenTypes;
		this.eofTokenType = eofTokenType;
		this.startState = startState;
		this.alphabetMap = alphabetMap;
		this.lexerState = lexerState;
		this.errorState = errorState;
	}

	public Token determineNextToken() throws IOException, LexerException {

		reader.markStart();

		int lineNumber = reader.getCurrentLineNumber();
		int columnNumber = reader.getCurrentColumnNumber();

		// Bring the state to the starting state for the current lexer state.
		int state = transitions[startState][lexerState];
		int lastMatchState = -1;

		int t;
		while ((t = reader.peek()) != -1) {

			// Determine the next state.
			int newState = transitions[state][alphabetMap[t]];

			// If we have come in the error state we can stop this loop.
			if (newState == errorState) {
				break;
			}

			// Remove the code point from the stack.
			reader.eat();

			state = newState;

			// Check whether the state is an accepting state.
			if (isEndState[state]) {
				lastMatchState = state;
				reader.markEnd();
			}
		}

		// There are three reasons for the above loop to end;
		// - if a match was found,
		// - if there are no more items in the input,
		// - if we ended in an error state.

		if (lastMatchState != -1) {
			TestTokenType tokenType = tokenTypes[lastMatchState];
			lexerState = tokenType.lexerState();
			return new Token(lineNumber, columnNumber, reader.readLexeme(), tokenType);
		} else if (t == -1) {
			return new Token(lineNumber, columnNumber, reader.readLexeme(), eofTokenType);
		}

		throw new LexerException("Unexpected codepoint '" + new String(Character.toChars(t)) + "'.",
				reader.getCurrentLineNumber(), reader.getCurrentColumnNumber());
	}
}
