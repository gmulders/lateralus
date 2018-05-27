package org.gertje.regular.parser.nodes;

import org.gertje.regular.parser.visitors.RegExNodeVisitor;
import org.gertje.regular.parser.visitors.VisitingException;

public class LexerTokenNode implements LexerNode {

	private String name;

	private AbstractRegExNode regEx;

	private String resultClassName;

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AbstractRegExNode getRegEx() {
		return regEx;
	}

	public void setRegEx(AbstractRegExNode regEx) {
		this.regEx = regEx;
	}

	public String getResultClassName() {
		return resultClassName;
	}

	public void setResultClassName(String resultClassName) {
		this.resultClassName = resultClassName;
	}
}
