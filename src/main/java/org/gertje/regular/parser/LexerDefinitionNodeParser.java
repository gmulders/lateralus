package org.gertje.regular.parser;

//import org.gertje.regular.lexer.Lexer;
//import org.gertje.regular.lexer.LexerException;
//import org.gertje.regular.lexer.LexerReader;
//import org.gertje.regular.lexer.LexerReaderImpl;
//import org.gertje.regular.lexer.Token;
//import org.gertje.regular.lexer.TokenType;


public class LexerDefinitionNodeParser {

//	public static LexerDescriptionNode parse(Reader reader) {
//		LexerReader lexerReader = new LexerReaderImpl(reader);
//		return null;
//	}
//
//	private static List<LexerClassNode> parseLexerClassNodeList(Lexer lexer) throws IOException, LexerException, RegExException {
//		List<LexerClassNode> lexerClassNodeList = new ArrayList<>();
//
//		Token token;
//		while ((token = lexer.determineNextToken()).getTokenType() != TokenType.EOF) {
//			expect(token, TokenType.LEXER_CLASS);
//
//			// Create a new LexerClassNode
//			LexerClassNode lexerClassNode = new LexerClassNode();
//			lexerClassNode.setName(token.getValue());
//			lexerClassNode.setLexerTokenList(parseLexerTokenNodeList(lexer));
//
//			lexerClassNodeList.add(lexerClassNode);
//		}
//
//		return lexerClassNodeList;
//	}
//
//	private static List<LexerTokenNode> parseLexerTokenNodeList(Lexer lexer) throws IOException, LexerException, RegExException {
//		List<LexerTokenNode> lexerTokenNodeList = new ArrayList<>();
//
//		Token token;
//		while ((token = lexer.determineNextToken()).getTokenType() != TokenType.EOF) {
//			LexerTokenNode lexerTokenNode = new LexerTokenNode();
//
//			expect(token, TokenType.TOKEN_NAME);
//			lexerTokenNode.setName(determineTokenName(token));
//
//			expect(token, TokenType.REGEX_END);
//			lexerTokenNode.setRegEx(parseRegEx(token.getValue()));
//
//			expect(token, TokenType.NEXT_STATE);
//
//			expect(token, TokenType.NEXT_STATE);
//			lexerTokenNode.setName(token.getValue());
//
//			lexerTokenNodeList.add(lexerTokenNode);
//		}
//
//		return lexerTokenNodeList;
//	}
//
//	private static AbstractRegExNode parseRegEx(String regEx) throws RegExException {
//		RegExParser regExParser = new RegExParser(regEx);
//		return regExParser.parse();
//	}
//
//	private static String determineTokenName(Token token) {
//		String rawName = token.getValue();
//		return rawName.substring(1, rawName.length() - 1);
//	}
//
//	private static void expect(Token token, TokenType tokenType) {
//		// Expect that the token is a lexer class.
//		if (token.getTokenType() != tokenType) {
//			//throw new ...
//		}
//	}
}
