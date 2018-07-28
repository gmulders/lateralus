package org.gertje.regular.parser.nodes;

import java.util.List;

public class LexerDefinitionNode implements LexerNode {

	private List<LexerClassNode> lexerClassNodeList;
	private String startLexerStateName;

	public LexerDefinitionNode(List<LexerClassNode> lexerClassNodeList, String startLexerStateName) {
		this.lexerClassNodeList = lexerClassNodeList;
		this.startLexerStateName = startLexerStateName;
	}

	public List<LexerClassNode> getLexerClassNodeList() {
		return lexerClassNodeList;
	}

	public String getStartLexerStateName() {
		return startLexerStateName;
	}
}
