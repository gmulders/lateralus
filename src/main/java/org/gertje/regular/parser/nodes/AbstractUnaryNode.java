package org.gertje.regular.parser.nodes;

public abstract class AbstractUnaryNode extends AbstractRegExNode {

	private AbstractRegExNode operand;

	public AbstractUnaryNode(AbstractRegExNode operand) {
		this.operand = operand;
	}

	public AbstractRegExNode getOperand() {
		return operand;
	}
}
