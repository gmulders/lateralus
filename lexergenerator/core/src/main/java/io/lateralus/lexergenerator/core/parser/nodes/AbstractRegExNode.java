package io.lateralus.lexergenerator.core.parser.nodes;

import io.lateralus.lexergenerator.core.parser.visitors.RegExNodeVisitor;
import io.lateralus.lexergenerator.core.parser.visitors.VisitingException;

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
