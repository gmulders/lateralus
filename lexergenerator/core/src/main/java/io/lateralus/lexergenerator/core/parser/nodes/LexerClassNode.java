package io.lateralus.lexergenerator.core.parser.nodes;

import java.util.List;

public class LexerClassNode implements LexerNode {

	private String name;
	private List<LexerTokenNode> lexerTokenList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<LexerTokenNode> getLexerTokenList() {
		return lexerTokenList;
	}

	public void setLexerTokenList(List<LexerTokenNode> lexerTokenList) {
		this.lexerTokenList = lexerTokenList;
	}
}
