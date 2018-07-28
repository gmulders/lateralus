package org.gertje.regular.definition;

import org.gertje.regular.automaton.Automaton;

import java.util.Map;

public class LexerDefinition {

	public static class TokenType {

		private int ordinal;
		private String name;
		private int lexerClass;

		public TokenType(int ordinal, String name, int lexerClass) {
			this.ordinal = ordinal;
			this.name = name;
			this.lexerClass = lexerClass;
		}

		public int ordinal() {
			return ordinal;
		}

		public int getLexerClass() {
			return lexerClass;
		}

		public String getName() {
			return name;
		}
	}

	private String[] lexerClassNames;

	private int[] alphabetIntervals;

	private Map<Integer, TokenType> acceptingStateTokenTypes;

	private Automaton dfa;

	private int startLexerState;

	private int errorState;

	public String[] getLexerClassNames() {
		return lexerClassNames;
	}

	public void setLexerClassNames(String[] lexerClassNames) {
		this.lexerClassNames = lexerClassNames;
	}

	public int[] getAlphabetIntervals() {
		return alphabetIntervals;
	}

	public void setAlphabetIntervals(int[] alphabetRanges) {
		this.alphabetIntervals = alphabetRanges;
	}

	public Map<Integer, TokenType> getAcceptingStateTokenTypes() {
		return acceptingStateTokenTypes;
	}

	public void setAcceptingStateTokenTypes(Map<Integer, TokenType> acceptingStateTokenTypes) {
		this.acceptingStateTokenTypes = acceptingStateTokenTypes;
	}

	public Automaton getDfa() {
		return dfa;
	}

	public void setDfa(Automaton dfa) {
		this.dfa = dfa;
	}

	public int getStartLexerState() {
		return startLexerState;
	}

	public void setStartLexerState(int startLexerState) {
		this.startLexerState = startLexerState;
	}

	public int getErrorState() {
		return errorState;
	}

	public void setErrorState(int errorState) {
		this.errorState = errorState;
	}
}
