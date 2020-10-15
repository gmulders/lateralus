package io.lateralus.lexergenerator.core.definition;

import io.lateralus.lexergenerator.core.automaton.Automaton;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract representation of a lexer. This is the output of the lexer generator and the input for the code generator.
 */
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TokenType tokenType = (TokenType) o;
			return ordinal == tokenType.ordinal &&
					lexerClass == tokenType.lexerClass &&
					Objects.equals(name, tokenType.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(ordinal, name, lexerClass);
		}
	}

	private String[] lexerClassNames;

	private int[] alphabetIntervals;

	private List<TokenType> tokenTypeList;

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

	public List<TokenType> getTokenTypeList() {
		return tokenTypeList;
	}

	public void setTokenTypeList(List<TokenType> tokenTypeList) {
		this.tokenTypeList = tokenTypeList;
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
