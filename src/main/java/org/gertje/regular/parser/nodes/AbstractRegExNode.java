package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

/**
 * Abstract super class for all regular expression nodes.
 */
public abstract class AbstractRegExNode implements LexerNode {

	/**
	 * Accepts the visitor.
	 *
	 * @param visitor The visitor.
	 */
	abstract public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X;

}
