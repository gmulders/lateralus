package io.lateralus.parsergenerator.codegenerator.model;

import java.util.List;

public class Reduction {
	private final int productionId;
	private final boolean isCast;
	private final String nodeType;
	private final List<Parameter> parameterList;

	public Reduction(int productionId, boolean isCast, String nodeType, List<Parameter> parameterList) {
		this.productionId = productionId;
		this.isCast = isCast;
		this.nodeType = nodeType;
		this.parameterList = parameterList;
	}

	public int getProductionId() {
		return productionId;
	}

	public boolean getIsCast() {
		return isCast;
	}

	public String getNodeType() {
		return nodeType;
	}

	public List<Parameter> getParameterList() {
		return parameterList;
	}
}
