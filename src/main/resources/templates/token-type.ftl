package ${packageName};

/**
 * Enum containing the possible token types.
 */
public enum TokenType {
	EOF(-1),
<#list tokenTypes as tokenType>	${tokenType.name}(${tokenType.lexerClass + 1})<#sep>,
</#list>;

	private final int lexerState;

	TokenType(final int lexerState) {
		this.lexerState = lexerState;
	}

	public int lexerState() {
		return lexerState;
	}
}
