package io.lateralus.lexergenerator.parser.lexer;

import java.io.IOException;
import java.util.Arrays;

/**
 * Basic {@link Lexer} implementation.
 */
public class BaseLexerDescriptionLexer implements Lexer {

	private static final int START_STATE = 1;

	private static final int ERROR_STATE = 0;

	private static final boolean[] IS_END_STATE = new boolean[] { false, false, false, true, false, true, true, false, false, true, true, true, true, true, true, true, true, true, true };

	private static final TokenType[] TOKEN_TYPES = new TokenType[] { null, null, null, TokenType.REGEX_PART, null, TokenType.NEW_LINE, TokenType.REGEX_START, null, null, TokenType.WHITE_SPACE, TokenType.REGEX_PART, TokenType.REGEX_END, TokenType.REGEX_BACKSLASH, TokenType.NEXT_STATE, TokenType.REGEX_SINGLE_QUOTE, TokenType.REGEX_DOUBLE_BACKSLASH, TokenType.NEW_LINE, TokenType.TOKEN_NAME, TokenType.LEXER_CLASS };

	private static final int[] TRANSITIONS;

	static {
		int[] transitions = new int[] { 20, 0, 1, 2, 1, 0, 1, 10, 6, 0, 1, 10, 9, 0, 1, 3, 1, 9, 1, 10, 5, 0, 1, 9, 1, 10, 10, 0, 1, 16, 1, 10, 1, 0, 1, 16, 4, 0, 1, 10, 11, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 5, 1, 10, 6, 0, 1, 10, 11, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 9, 1, 10, 5, 0, 1, 9, 1, 10, 11, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 6, 1, 11, 8, 0, 1, 14, 9, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 7, 1, 10, 6, 0, 1, 10, 11, 0, 1, 10, 6, 0, 1, 10, 11, 0, 1, 10, 1, 4, 5, 0, 1, 10, 7, 0, 1, 18, 3, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 8, 1, 10, 6, 0, 1, 10, 11, 0, 1, 10, 6, 0, 1, 10, 11, 0, 1, 10, 1, 17, 2, 0, 1, 13, 2, 0, 1, 10, 11, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 18, 1, 10, 1, 4, 3, 0, 1, 4, 1, 0, 1, 10, 7, 0, 1, 18, 3, 0, 1, 10, 6, 0, 1, 10, 11, 0, 1, 12, 8, 0, 1, 15, 9, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 18, 1, 10, 1, 4, 3, 0, 1, 4, 1, 0, 1, 10, 7, 0, 1, 18, 3, 0, 1, 10, 6, 0, 1, 10, 10, 0, 1, 18, 1, 10, 1, 4, 3, 0, 1, 4, 1, 0, 1, 10, 7, 0, 1, 18, 3, 0, 1, 10, 6, 0, 1, 10, 8, 0 };

		TRANSITIONS = new int[19 * 27];

		int k = 0;
		for (int i = 0; i < transitions.length; i+=2) {
			for (int j = 0; j < transitions[i]; j++) {
				TRANSITIONS[k++] = transitions[i + 1];
			}
		}
	}

	private static final int[] ALPHABET_MAP;

	static {
		int[] intervals = new int[] { 0, 9, 9, 10, 10, 11, 11, 13, 13, 14, 14, 32, 32, 33, 33, 39, 39, 40, 40, 45, 45, 46, 46, 48, 48, 58, 58, 60, 60, 61, 61, 62, 62, 63, 63, 65, 65, 91, 91, 92, 92, 93, 93, 95, 95, 96, 96, 97, 97, 123, 123, 1114112 };

		// Translates a unicode codepoint to the correct input element from the alphabet.
		ALPHABET_MAP = new int[0x110000];

		// Fill the array that translates unicode codepoints.
		for (int i = 0; i < intervals.length; i+=2) {
			// Get the start and end from the ranges array.
			int fromIndex = intervals[i];
			int toIndex = intervals[i + 1];

			// The input is the number of the current range plus one (0 input will lead to the error state).
			int input = (i >> 1) + 1;

			// Fill the range.
			Arrays.fill(ALPHABET_MAP, fromIndex, toIndex, input);
		}
	}

	private final LexerReader reader;

	private int lexerState = 1;

	public BaseLexerDescriptionLexer(final LexerReader reader) {
		this.reader = reader;
	}

	@Override
	public Token nextToken() throws LexerException {
		try {
			return determineNextToken();
		} catch (IOException e) {
			throw new LexerException("An exception has occurred while reading.", reader.getCurrentLineNumber(),
					reader.getCurrentColumnNumber(), e);
		}
	}

	private Token determineNextToken() throws IOException, LexerException {
		reader.markStart();

		int lineNumber = reader.getCurrentLineNumber();
		int columnNumber = reader.getCurrentColumnNumber();

		// Bring the state to the starting state for the current lexer state.
		int state = TRANSITIONS[START_STATE + 19 * lexerState];
		int lastMatchState = -1;

		int t;
		while ((t = reader.peek()) != -1) {

			// Determine the next state.
			int newState = TRANSITIONS[state + 19 * ALPHABET_MAP[t]];

			// If we have come in the error state we can stop this loop.
			if (newState == ERROR_STATE) {
				break;
			}

			// Remove the code point from the stack.
			reader.eat();

			state = newState;

			// Check whether the state is an accepting state.
			if (IS_END_STATE[state]) {
				lastMatchState = state;
				reader.markEnd();
			}
		}

		// There are three reasons for the above loop to end:
		// - if a match was found,
		// - if there are no more items in the input,
		// - if we ended in an error state.

		if (lastMatchState != -1) {
			TokenType tokenType = TOKEN_TYPES[lastMatchState];
			lexerState = tokenType.lexerState();
			return new Token(lineNumber, columnNumber, reader.readLexeme(), tokenType);
		} else if (t == -1) {
			return new Token(lineNumber, columnNumber, reader.readLexeme(), TokenType.EOF);
		}

		throw new LexerException("Unexpected codepoint '" + new String(Character.toChars(t)) + "'.",
				reader.getCurrentLineNumber(), reader.getCurrentColumnNumber());
	}
}
