package org.gertje.regular.parser.nodes;

public abstract class AbstractBinaryNode extends AbstractRegExNode {

	private LexerNode lhs;
	private LexerNode rhs;

	public AbstractBinaryNode(LexerNode lhs, LexerNode rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public LexerNode getLhs() {
		return lhs;
	}

	public void setLhs(LexerNode lhs) {
		this.lhs = lhs;
	}

	public LexerNode getRhs() {
		return rhs;
	}

	public void setRhs(LexerNode rhs) {
		this.rhs = rhs;
	}

}
