package io.lateralus.lexergenerator.core.parser.nodes;

import java.util.List;

public class LexerDescriptionNode implements LexerNode {

	private List<LexerClassNode> lexerClassNodeList;
	private String startLexerStateName;

	public LexerDescriptionNode(List<LexerClassNode> lexerClassNodeList, String startLexerStateName) {
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
