package org.gertje.regular;

import org.gertje.regular.definition.LexerDefinition;
import org.gertje.regular.definition.builder.LexerDefinitionBuilder;
import org.gertje.regular.parser.RegExException;
import org.gertje.regular.testlexer.LexerException;
import org.gertje.regular.testlexer.TestLexer;
import org.gertje.regular.testlexer.Token;
import org.gertje.regular.util.TestLexerBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleTest {

	@Test
	public void testRegex1() throws RegExException, IOException, LexerException {

		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("A", "a*", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("aaaa");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("aaaa", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(5, token.getColumnNumber());
		assertEquals("", token.getValue());
		assertEquals(0, token.getTokenType().ordinal());


		reader = new StringReader("aaaaaaab");
		testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		token = testLexer.determineNextToken();

		assertEquals(1, token.getColumnNumber());
		assertEquals(1, token.getLineNumber());
		assertEquals("aaaaaaa", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		TestLexer testException1 = testLexer;
		LexerException exception = assertThrows(LexerException.class, testException1::determineNextToken);
		assertEquals("Unexpected codepoint 'b'.", exception.getMessage());
		assertEquals(1, exception.getLineNumber());
		assertEquals(8, exception.getColumnNumber());
	}

	@Test
	public void testRegex2() throws RegExException, IOException, LexerException {

		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("A", "a+", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("aaaa");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("aaaa", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(5, token.getColumnNumber());
		assertEquals("", token.getValue());
		assertEquals(0, token.getTokenType().ordinal());


		reader = new StringReader("aaaaaaab");
		testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		token = testLexer.determineNextToken();

		assertEquals(1, token.getColumnNumber());
		assertEquals(1, token.getLineNumber());
		assertEquals("aaaaaaa", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		TestLexer testException1 = testLexer;
		LexerException exception = assertThrows(LexerException.class, testException1::determineNextToken);
		assertEquals("Unexpected codepoint 'b'.", exception.getMessage());
		assertEquals(1, exception.getLineNumber());
		assertEquals(8, exception.getColumnNumber());
	}

	@Test
	public void testRegex3() throws RegExException, IOException, LexerException {

		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("A", "a|b", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("abab");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("a", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(2, token.getColumnNumber());
		assertEquals("b", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());
	}

	@Test
	public void testRegex4() throws RegExException, IOException, LexerException {

		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("A", "a|b", "DEFAULT")
					.addLexerToken("C", "c", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("abc");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("a", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(2, token.getColumnNumber());
		assertEquals("b", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(3, token.getColumnNumber());
		assertEquals("c", token.getValue());
		assertEquals(2, token.getTokenType().ordinal());
	}

	@Test
	public void testRegex5() throws RegExException, IOException, LexerException {

		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("OPTIONALTEST", "ab?a", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("abaaab");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("aba", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(4, token.getColumnNumber());
		assertEquals("aa", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		LexerException exception = assertThrows(LexerException.class, testLexer::determineNextToken);
		assertEquals("Unexpected codepoint 'b'.", exception.getMessage());
		assertEquals(1, exception.getLineNumber());
		assertEquals(6, exception.getColumnNumber());
	}

	@Test
	public void testRegex6() throws RegExException, IOException, LexerException {

		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("INTEGER", "0|[1-9][0-9]*", "DEFAULT")
					.addLexerToken("LEFT_CURLY_BRACE", "{", "DEFAULT")
					.addLexerToken("RIGHT_CURLY_BRACE", "}", "DEFAULT")
					.addLexerToken("WHITESPACE", "( |\t)*", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("1234567890 {  }");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("1234567890", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(11, token.getColumnNumber());
		assertEquals(" ", token.getValue());
		assertEquals(4, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(12, token.getColumnNumber());
		assertEquals("{", token.getValue());
		assertEquals(2, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(13, token.getColumnNumber());
		assertEquals("  ", token.getValue());
		assertEquals(4, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(15, token.getColumnNumber());
		assertEquals("}", token.getValue());
		assertEquals(3, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(16, token.getColumnNumber());
		assertEquals("", token.getValue());
		assertEquals(0, token.getTokenType().ordinal());
	}

//	@Test
//	public void testRegex7() throws RegExException, IOException, LexerException {
//		LexerDefinition lexerDefinition = createSimpleExpressionLexer();
//
//		String input = "<=>= ? 7..8 / 55\t _dd 'bla' + '\\t\\n' D'2017-06-21';\n && ()|| % * / ! != true false";
//
//		Reader reader = new StringReader(input);
//		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();
//
//		List<Token> tokens = new ArrayList<>();
//
//		Token token = testLexer.determineNextToken();
//		while (!isType(token, "EOF")) {
////			System.out.println(token.getTokenType().getName());
//			tokens.add(token);
//			token = testLexer.determineNextToken();
//		}
//
//		List<bla.Token> tokens1 = new ArrayList<>();
//		try {
//			BasicLexer basicLexer = new BasicLexer(new LexerReaderImpl(new StringReader(input)));
//			bla.Token t = basicLexer.determineNextToken();
//			while (t.getTokenType() != TokenType.EOF) {
//				tokens1.add(t);
//				t = basicLexer.determineNextToken();
//			}
//		} catch (bla.LexerException e) {
//			e.printStackTrace();
//		}
//
//		for (int i = 0; i < tokens.size(); i++) {
//			assertEquals(tokens.get(i).getTokenType().getName(), tokens1.get(i).getTokenType().name());
//		}
//	}

	@Test
	public void testRegex8() throws RegExException, IOException, LexerException {
		LexerDefinition lexerDefinition = new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("SHORT", "ab", "DEFAULT")
					.addLexerToken("LONG", "(ab)+c", "DEFAULT")
				.end()
				.build();

		Reader reader = new StringReader("ababab");
		TestLexer testLexer = new TestLexerBuilder(reader, lexerDefinition).build();

		Token token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(1, token.getColumnNumber());
		assertEquals("ab", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(3, token.getColumnNumber());
		assertEquals("ab", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());

		token = testLexer.determineNextToken();

		assertEquals(1, token.getLineNumber());
		assertEquals(5, token.getColumnNumber());
		assertEquals("ab", token.getValue());
		assertEquals(1, token.getTokenType().ordinal());
	}

//	private LexerDefinition createSimpleExpressionLexer() throws RegExException {
//		return new LexerDefinitionBuilder()
//				.lexerStartStateName("DEFAULT")
//				.startLexerClass("DEFAULT")
//					.addLexerToken("WHITE_SPACE", "( |\\t)+", "DEFAULT")
//					.addLexerToken("END_OF_EXPRESSION", ";", "DEFAULT")
//					.addLexerToken("NEW_LINE", "\\r\\n|\\r|\\n", "DEFAULT")
//					.addLexerToken("DATE_START", "D'", "DATE")
//					.addLexerToken("BOOLEAN", "true|false", "DEFAULT")
//					.addLexerToken("IDENTIFIER", "([a-zA-Z_])[a-zA-Z0-9_]*", "DEFAULT")
//					.addLexerToken("LEFT_PARENTHESIS", "\\(", "DEFAULT")
//					.addLexerToken("RIGHT_PARENTHESIS", ")", "DEFAULT")
//					.addLexerToken("LEFT_BRACKET", "{", "DEFAULT")
//					.addLexerToken("RIGHT_BRACKET", "}", "DEFAULT")
//					.addLexerToken("STRING_START", "'", "STRING")
//					.addLexerToken("DECIMAL", "([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)", "DEFAULT")
//					.addLexerToken("INTEGER", "[0-9]+", "DEFAULT")
//					.addLexerToken("BOOLEAN_AND", "&&", "DEFAULT")
//					.addLexerToken("BOOLEAN_OR", "[|][|]", "DEFAULT")
//					.addLexerToken("PLUS", "+", "DEFAULT")
//					.addLexerToken("MINUS", "-", "DEFAULT")
//					.addLexerToken("POWER", "^", "DEFAULT")
//					.addLexerToken("MULTIPLY", "*", "DEFAULT")
//					.addLexerToken("DIVIDE", "/", "DEFAULT")
//					.addLexerToken("PERCENT", "%", "DEFAULT")
//					.addLexerToken("EQ", "==", "DEFAULT")
//					.addLexerToken("NEQ", "!=", "DEFAULT")
//					.addLexerToken("LEQ", "<=", "DEFAULT")
//					.addLexerToken("LT", "<", "DEFAULT")
//					.addLexerToken("GEQ", ">=", "DEFAULT")
//					.addLexerToken("GT", ">", "DEFAULT")
//					.addLexerToken("NOT", "!", "DEFAULT")
//					.addLexerToken("ASSIGNMENT", "=", "DEFAULT")
//					.addLexerToken("IF", "?", "DEFAULT")
//					.addLexerToken("COLON", ":", "DEFAULT")
//					.addLexerToken("COMMA", ",", "DEFAULT")
//				.end()
//				.startLexerClass("STRING")
//					.addLexerToken("STRING_PART", "[^'\\\\]*", "STRING")
//					.addLexerToken("STRING_END", "'", "DEFAULT")
//					.addLexerToken("STRING_SINGLE_QUOTE", "\\\\'", "STRING")
//					.addLexerToken("STRING_TAB", "\\\\t", "STRING")
//					.addLexerToken("STRING_NEW_LINE", "\\\\n", "STRING")
//					.addLexerToken("STRING_CARRIAGE_RETURN", "\\\\r", "STRING")
//					.addLexerToken("STRING_BACKSLASH", "\\\\\\\\", "STRING")
//				.end()
//				.startLexerClass("DATE")
//					.addLexerToken("DATE_PART", "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]", "DATE")
//					.addLexerToken("DATE_END", "'", "DEFAULT")
//				.end()
//				.build();
//	}

	private boolean isType(Token token, String name) {
		return token.getTokenType().getName().equals(name);
	}
}
