package org.gertje.regular;

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
	}

	private String[] lexerClassNames;

	private int[] alphabetIntervals;

	private Map<Integer, TokenType> acceptingStateTokenTypes;

	private Automaton dfa;

	private int lexerStartState;


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

	public int getLexerStartState() {
		return lexerStartState;
	}

	public void setLexerStartState(int lexerStartState) {
		this.lexerStartState = lexerStartState;
	}

}
