package org.gertje.regular.util;

import org.gertje.regular.definition.LexerDefinition;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;

import java.util.ArrayList;
import java.util.List;

public class LexerDefinitionBuilder {

	private List<LexerClassNode> lexerClassNodeList;
	private String lexerStartStateName;

	public LexerDefinitionBuilder() {
		lexerClassNodeList = new ArrayList<>();
	}

	public LexerClassNodeBuilder startLexerClass(String name) {
		return new LexerClassNodeBuilder(this, lexerClassNodeList, name);
	}

	public LexerDefinitionBuilder lexerStartStateName(String lexerStartStateName) {
		this.lexerStartStateName = lexerStartStateName;
		return this;
	}

	public LexerDefinition build() {
		LexerDefinitionNode lexerDefinitionNode = new LexerDefinitionNode(lexerClassNodeList, lexerStartStateName);
		return new org.gertje.regular.definition.LexerDefinitionBuilder().build(lexerDefinitionNode);
	}
}
