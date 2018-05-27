package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

import java.util.List;

public class LexerDefinitionNode implements LexerNode {

	private List<LexerClassNode> lexerClassNodeList;
	private String lexerStartStateName;

	public LexerDefinitionNode(List<LexerClassNode> lexerClassNodeList, String lexerStartStateName) {
		this.lexerClassNodeList = lexerClassNodeList;
		this.lexerStartStateName = lexerStartStateName;
	}

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}

	public List<LexerClassNode> getLexerClassNodeList() {
		return lexerClassNodeList;
	}

	public String getLexerStartStateName() {
		return lexerStartStateName;
	}
}
