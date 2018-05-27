package org.gertje.regular.lexer;

import java.io.IOException;
import java.io.Reader;

public class TestLexer {

	private LexerReader reader;

	private final int[][] transitions;

	private final boolean[] isEndState;

	private final TokenType[] tokenTypes;

	private final TokenType eofTokenType;

	private final int startState;

	private int lexerState;

	private int[] alphabetMap;

	public TestLexer(Reader reader, int[][] transitions, int[] alphabetMap, boolean[] isEndState,
			TokenType[] tokenTypes, TokenType eofTokenType, int startState, int lexerState) {
		this.reader = new LexerReaderImpl(reader);
		this.transitions = transitions;
		this.isEndState = isEndState;
		this.tokenTypes = tokenTypes;
		this.eofTokenType = eofTokenType;
		this.startState = startState;
		this.alphabetMap = alphabetMap;
		this.lexerState = lexerState;
	}

	public Token determineNextToken() throws IOException, LexerException {

		int t;

		reader.startLexeme();

		int lineNumber = reader.getCurrentLineNumber();
		int columnNumber = reader.getCurrentColumnNumber();

		boolean match = false;

		// The end state is the last
		int endState = startState;
		int state;

		// Bring the state to the starting state for the current lexer state.
		state = transitions[endState][lexerState];

		while ((t = reader.peek()) != -1) {

			// Determine the next state.
			state = transitions[state][alphabetMap[t]];

			// If this is the error state we can stop this loop.
			if (state == 0) {
				break;
			}

			endState = state;

			// Check whether the state is an accepting state.

			if (isEndState[endState]) {
				match = true;

			}

			// Remove the code point from the stack.
			reader.eat();
		}

		// There are three reasons for the above loop to end;
		// - if a match was found,
		// - if there are no more items in the input,
		// - if we ended in an error state.

		if (match) {
			TokenType tokenType = tokenTypes[endState];
			lexerState = tokenType.lexerState();
			return new Token(lineNumber, columnNumber, reader.readLexeme(), tokenType);
		} else if (t == -1) {
			return new Token(lineNumber, columnNumber, reader.readLexeme(), eofTokenType);
		}

		throw new LexerException("Unexpected codepoint '" + new String(Character.toChars(t)) + "'.",
				reader.getCurrentLineNumber(), reader.getCurrentColumnNumber());
	}


}


