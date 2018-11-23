package org.gertje.regular.parser.description;

import org.gertje.regular.parser.RegExException;
import org.gertje.regular.parser.RegExParser;
import org.gertje.regular.parser.description.lexer.Lexer;
import org.gertje.regular.parser.description.lexer.LexerDescriptionLexer;
import org.gertje.regular.parser.description.lexer.LexerException;
import org.gertje.regular.parser.description.lexer.LexerReader;
import org.gertje.regular.parser.description.lexer.LexerReaderImpl;
import org.gertje.regular.parser.description.lexer.Token;
import org.gertje.regular.parser.description.lexer.TokenType;
import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDescriptionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a lexer description file and returns a {@link LexerDescriptionNode} that represents that file.
 */
public class LexerDescriptionParser {

	private Token token;

	private final Lexer lexer;

	public LexerDescriptionParser(Reader reader) {
		LexerReader lexerReader = new LexerReaderImpl(reader);
		lexer = new LexerDescriptionLexer(lexerReader);
	}

	public LexerDescriptionNode parse() throws ParserException {

		List<LexerClassNode> lexerClassNodeList = parseLexerClassNodeList();

		String startLexerStateName = lexerClassNodeList.stream()
				.findFirst()
				.map(LexerClassNode::getName)
				.orElse(null);

		return new LexerDescriptionNode(lexerClassNodeList, startLexerStateName);
	}

	private List<LexerClassNode> parseLexerClassNodeList() throws ParserException {

		List<LexerClassNode> lexerClassNodeList = new ArrayList<>();

		token = nextToken();

		while (!token.is(TokenType.EOF)) {
			expect(token, TokenType.LEXER_CLASS);

			// Create a new LexerClassNode
			LexerClassNode lexerClassNode = new LexerClassNode();
			lexerClassNode.setName(token.getValue());
			lexerClassNode.setLexerTokenList(parseLexerTokenNodeList());

			lexerClassNodeList.add(lexerClassNode);
		}

		return lexerClassNodeList;
	}

	private List<LexerTokenNode> parseLexerTokenNodeList() throws ParserException {

		List<LexerTokenNode> lexerTokenNodeList = new ArrayList<>();

		token = nextToken();
		while (token.is(TokenType.TOKEN_NAME)) {

			LexerTokenNode lexerTokenNode = new LexerTokenNode();
			lexerTokenNode.setName(determineTokenName(token));

			token = nextToken();
			expect(token, TokenType.REGEX);
			lexerTokenNode.setRegEx(parseRegEx(token.getValue()));

			token = nextToken();
			expect(token, TokenType.NEXT_STATE);

			token = nextToken();
			expect(token, TokenType.LEXER_CLASS);
			lexerTokenNode.setResultClassName(token.getValue());

			lexerTokenNodeList.add(lexerTokenNode);
			token = nextToken();
		}

		return lexerTokenNodeList;
	}

	private Token nextToken() throws ParserException {
		try {
			return lexer.nextToken();
		} catch (LexerException e) {
			throw new ParserException("An exception has occurred when reading the input.", e);
		}
	}

	private AbstractRegExNode parseRegEx(String regEx) throws ParserException {
		try {
			return new RegExParser(regEx).parse();
		} catch (RegExException e) {
			throw new ParserException("The regular expression '" + regEx + "' could not be parsed.", token, e);
		}
	}

	private static String determineTokenName(Token token) {
		String rawName = token.getValue();
		return rawName.substring(1, rawName.length() - 1);
	}

	private static void expect(Token token, TokenType tokenType) throws ParserException {
		// Expect that the token is a lexer class.
		if (!token.is(tokenType)) {
			throw new ParserException("Expected a token of type '" + tokenType + "' found '" + token.getTokenType()
					+ "' instead.", token);
		}
	}
}
