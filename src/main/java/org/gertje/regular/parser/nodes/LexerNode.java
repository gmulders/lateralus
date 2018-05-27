package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

public interface LexerNode {

	/**
	 * Accepts the visitor.
	 *
	 * @param visitor The visitor.
	 */
	<R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X;
}
