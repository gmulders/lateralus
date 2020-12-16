package ${parserPackageName};

import ${lexerPackageName}.Token;

public class ParserSyntaxException extends ParserException {

	private final Token token;

	public ParserSyntaxException(String message, Token token) {
		super(message);
		this.token = token;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " - unexpected token " + token.getTokenType() + " (" + token.getValue() + ") at " +
			token.getLineNumber() + ":" + token.getColumnNumber();
	}

	public Token getToken() {
		return token;
	}
}
