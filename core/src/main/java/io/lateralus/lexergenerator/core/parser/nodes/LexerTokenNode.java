package io.lateralus.lexergenerator.core.parser.nodes;

public class LexerTokenNode implements LexerNode {

	private String name;

	private AbstractRegExNode regEx;

	private String resultClassName;

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
