package org.gertje.regular.parser.nodes;

public abstract class AbstractUnaryNode extends AbstractRegExNode {

	private LexerNode operand;

	public AbstractUnaryNode(LexerNode operand) {
		this.operand = operand;
	}

	public LexerNode getOperand() {
		return operand;
	}

	public void setOperand(LexerNode operand) {
		this.operand = operand;
	}

}
