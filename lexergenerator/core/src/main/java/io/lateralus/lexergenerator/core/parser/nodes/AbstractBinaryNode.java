package io.lateralus.lexergenerator.core.parser.nodes;

public abstract class AbstractBinaryNode extends AbstractRegExNode {

	private AbstractRegExNode lhs;
	private AbstractRegExNode rhs;

	public AbstractBinaryNode(AbstractRegExNode lhs, AbstractRegExNode rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public AbstractRegExNode getLhs() {
		return lhs;
	}

	public AbstractRegExNode getRhs() {
		return rhs;
	}
}
