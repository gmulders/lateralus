package io.lateralus.lexergenerator.core.description;

import io.lateralus.lexergenerator.core.definition.LexerDefinition;
import io.lateralus.lexergenerator.core.parser.nodes.LexerClassNode;
import io.lateralus.lexergenerator.core.parser.nodes.LexerDescriptionNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class that builds a {@link LexerDefinition}, that can be used as input for the lexer generator.
 */
public class LexerDescriptionBuilder {

	private final List<LexerClassNode> lexerClassNodeList;
	private String lexerStartStateName;
	private String firstStateName;

	public LexerDescriptionBuilder() {
		lexerClassNodeList = new ArrayList<>();
	}

	public LexerClassNodeBuilder startLexerClass(String name) {
		if (firstStateName == null) {
			firstStateName = name;
		}
		return new LexerClassNodeBuilder(this, lexerClassNodeList, name);
	}

	public LexerDescriptionBuilder lexerStartStateName(String lexerStartStateName) {
		this.lexerStartStateName = lexerStartStateName;
		return this;
	}

	public LexerDescriptionNode build() {
		if (lexerStartStateName == null) {
			lexerStartStateName = firstStateName;
		}
		return new LexerDescriptionNode(lexerClassNodeList, lexerStartStateName);
	}
}
