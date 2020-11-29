package io.lateralus.parsergenerator.codegenerator.model;

import java.util.List;

public class Node {
	private final String className;
	private final String baseClassName;
	private final boolean isAbstract;
	private final boolean isBinaryNode;
	private final List<Parameter> parameterList;
	private final String firstTokenName;

	public Node(String className, String baseClassName, boolean isAbstract, boolean isBinaryNode,
	            List<Parameter> parameterList, String firstTokenName) {
		this.className = className;
		this.baseClassName = baseClassName;
		this.isAbstract = isAbstract;
		this.isBinaryNode = isBinaryNode;
		this.parameterList = parameterList;
		this.firstTokenName = firstTokenName;
	}

	public String getClassName() {
		return className;
	}

	public String getBaseClassName() {
		return baseClassName;
	}

	public boolean getIsAbstract() {
		return isAbstract;
	}

	public boolean getIsBinaryNode() {
		return isBinaryNode;
	}

	public List<Parameter> getParameterList() {
		return parameterList;
	}

	public String getFirstTokenName() {
		return firstTokenName;
	}
}
