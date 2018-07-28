package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

public class StarNode extends AbstractUnaryNode {

	public StarNode(AbstractRegExNode operand) {
		super(operand);
	}

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}
}
