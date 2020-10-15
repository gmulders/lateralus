package io.lateralus.lexergenerator.core.parser.nodes;

import io.lateralus.lexergenerator.core.parser.visitors.RegExNodeVisitor;
import io.lateralus.lexergenerator.core.parser.visitors.VisitingException;

public class UnionNode extends AbstractBinaryNode {

	public UnionNode(AbstractRegExNode lhs, AbstractRegExNode rhs) {
		super(lhs, rhs);
	}

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}
}
