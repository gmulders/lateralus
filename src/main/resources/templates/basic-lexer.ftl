package ${packageName};

import java.io.IOException;
import java.util.Arrays;

/**
 * Basic {@link Lexer} implementation.
 */
public class ${lexerName}Lexer implements Lexer {

	private static final int START_STATE = ${startState};

	private final int ERROR_STATE = ${errorState};

	private static final boolean[] IS_END_STATE = new boolean[] { ${isEndState} };

	private static final TokenType[] TOKEN_TYPES = new TokenType[] { ${tokenTypes} };

	private static final int[] TRANSITIONS;

	static {
		int[] transitions = new int[] { ${transitions} };

		TRANSITIONS = new int[${stateCount} * ${alphabetSize}];

		int k = 0;
		for (int i = 0; i < transitions.length; i+=2) {
			for (int j = 0; j < transitions[i]; j++) {
				TRANSITIONS[k++] = transitions[i + 1];
			}
		}
	}

	private static final int[] ALPHABET_MAP;

	static {
		int[] intervals = new int[] { ${intervals} };

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

	private int lexerState = ${startLexerState};

	public ${lexerName}Lexer(LexerReader reader) {
		this.reader = reader;
	}

	@Override
	public Token determineNextToken() throws IOException, LexerException {

		reader.markStart();

		int lineNumber = reader.getCurrentLineNumber();
		int columnNumber = reader.getCurrentColumnNumber();

		// Bring the state to the starting state for the current lexer state.
		int state = TRANSITIONS[START_STATE + ${stateCount} * lexerState];
		int lastMatchState = -1;

		int t;
		while ((t = reader.peek()) != -1) {

			// Determine the next state.
			int newState = TRANSITIONS[state + ${stateCount} * ALPHABET_MAP[t]];

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

		// There are three reasons for the above loop to end;
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
