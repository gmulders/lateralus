package io.lateralus.lexergenerator.core.definition.builder;

import io.lateralus.lexergenerator.core.definition.LexerDefinition;
import io.lateralus.lexergenerator.core.parser.nodes.LexerClassNode;
import io.lateralus.lexergenerator.core.parser.nodes.LexerDescriptionNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class that builds a {@link LexerDefinition}, that can be used as input for the lexer generator.
 */
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
		final LexerDescriptionNode lexerDescriptionNode = new LexerDescriptionNode(lexerClassNodeList, lexerStartStateName);
		return new io.lateralus.lexergenerator.core.definition.LexerDefinitionBuilder().build(lexerDescriptionNode);
	}
}
