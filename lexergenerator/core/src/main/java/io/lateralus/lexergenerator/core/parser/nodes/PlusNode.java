package io.lateralus.lexergenerator.core.parser.nodes;

import io.lateralus.lexergenerator.core.parser.visitors.RegExNodeVisitor;
import io.lateralus.lexergenerator.core.parser.visitors.VisitingException;

public class PlusNode extends AbstractUnaryNode {

	public PlusNode(AbstractRegExNode operand) {
		super(operand);
	}

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}
}
