package org.gertje.regular.util;

import org.gertje.regular.IntervalUtils;
import org.gertje.regular.LexerDefinition;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.visitors.BuildLexerVisitor;
import org.gertje.regular.parser.visitors.IntervalCollectorVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

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

	public LexerDefinition build() throws VisitingException {
		LexerDefinitionNode lexerDefinitionNode = new LexerDefinitionNode(lexerClassNodeList, lexerStartStateName);

		// Determine the array with the intervals that define the alphabet.
		int[] alphabetIntervals = IntervalUtils.splitIntervals(new IntervalCollectorVisitor().collectIntervals(lexerDefinitionNode));

		// Create the Lexer from the regex parse tree and the interval array.
		return new BuildLexerVisitor().convert(lexerDefinitionNode, alphabetIntervals);
	}
}
