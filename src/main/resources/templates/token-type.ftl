package ${packageName};

public enum TokenType {
	EOF(-1),
<#list tokenTypes as tokenType>	${tokenType.name}(${tokenType.lexerClass + 1})<#sep>,
</#list>;

	private int lexerState;

	TokenType(int lexerState) {
		this.lexerState = lexerState;
	}

	public int lexerState() {
		return lexerState;
	}
}
