package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

import java.util.List;

public class LexerClassNode implements LexerNode {

	private String name;
	private List<LexerTokenNode> lexerTokenList;

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}

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
