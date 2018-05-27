package org.gertje.regular;

import org.gertje.regular.lexer.LexerException;
import org.gertje.regular.lexer.TestLexer;
import org.gertje.regular.lexer.Token;
import org.gertje.regular.parser.RegExException;
import org.gertje.regular.util.LexerDefinitionBuilder;
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
}
