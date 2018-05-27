package org.gertje.regular.util;

import org.gertje.regular.Automaton;
import org.gertje.regular.LexerDefinition;
import org.gertje.regular.lexer.TestLexer;
import org.gertje.regular.lexer.TestTokenType;

import java.io.Reader;
import java.util.Arrays;
import java.util.Map;

public class TestLexerBuilder {

	private Reader reader;

	private LexerDefinition lexerDefinition;

	public TestLexerBuilder(Reader reader, LexerDefinition lexerDefinition) {
		this.reader = reader;
		this.lexerDefinition = lexerDefinition;
	}

	public TestLexer build() {
		int stateCount = lexerDefinition.getDfa().getStateCount();
		int alphabetSize = lexerDefinition.getDfa().getAlphabetSize();
		int[][] table = new int[stateCount][alphabetSize + 1];

		for (Automaton.Transition t : lexerDefinition.getDfa().getTransitions()) {
			table[t.fromState][t.input + 1] = t.toState;
		}

		boolean[] isEndState = new boolean[stateCount];
		for (int acceptingState : lexerDefinition.getDfa().getAcceptingStates()) {
			isEndState[acceptingState] = true;
		}

		TestTokenType[] testTokenTypes = new TestTokenType[stateCount];

		for (Map.Entry<Integer, LexerDefinition.TokenType> entry : lexerDefinition.getAcceptingStateTokenTypes().entrySet()) {
			testTokenTypes[entry.getKey()] = new TestTokenType(entry.getValue().ordinal() + 1, entry.getValue().getLexerClass() + 1);
		}

		int[] intervals = lexerDefinition.getAlphabetIntervals();

		// Translates a unicode codepoint to the correct input element from the alphabet.
		int[] alphabetMap = new int[0x110000];

		// Fill the array that translates unicode codepoints.
		for (int i = 0; i < intervals.length; i+=2) {
			// Get the start and end from the ranges array.
			int start = intervals[i];
			// Add one to the end, to make next loop prettier.
			int end = intervals[i + 1] + 1;

			// The input is the number of the current range plus one (0 input will lead to the 0 (error) state).
			int input = (i >> 1) + 1;

			// Fill the range.
			Arrays.fill(alphabetMap, start, end, input);
		}


		return new TestLexer(reader, table, alphabetMap, isEndState, testTokenTypes, new TestTokenType(0, -1),
				lexerDefinition.getDfa().getStartState(), lexerDefinition.getLexerStartState() + 1);
	}
}
