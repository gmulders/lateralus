package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

import java.util.List;

public class LexerDefinitionNode implements LexerNode {

	private List<LexerClassNode> lexerClassNodeList;
	private String startLexerStateName;

	public LexerDefinitionNode(List<LexerClassNode> lexerClassNodeList, String startLexerStateName) {
		this.lexerClassNodeList = lexerClassNodeList;
		this.startLexerStateName = startLexerStateName;
	}

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}

	public List<LexerClassNode> getLexerClassNodeList() {
		return lexerClassNodeList;
	}

	public String getStartLexerStateName() {
		return startLexerStateName;
	}
}
