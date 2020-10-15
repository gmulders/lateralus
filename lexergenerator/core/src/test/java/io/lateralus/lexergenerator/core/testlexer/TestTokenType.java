package io.lateralus.lexergenerator.core.testlexer;

public class TestTokenType implements TokenType {

	private int ordinal;

	private String name;

	private int lexerState;

	public TestTokenType(int ordinal, String name, int lexerState) {
		this.ordinal = ordinal;
		this.name = name;
		this.lexerState = lexerState;
	}

	@Override
	public int ordinal() {
		return ordinal;
	}

	@Override
	public int lexerState() {
		return lexerState;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TestTokenType that = (TestTokenType) o;
		return ordinal == that.ordinal;
	}

	@Override
	public int hashCode() {
		return ordinal;
	}

	@Override
	public String toString() {
		return "TestTokenType{" +
				"ordinal=" + ordinal +
				", name='" + name + '\'' +
				", lexerState=" + lexerState +
				'}';
	}
}
