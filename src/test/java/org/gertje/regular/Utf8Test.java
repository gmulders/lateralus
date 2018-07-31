package org.gertje.regular;

import org.gertje.regular.definition.LexerDefinition;
import org.gertje.regular.definition.LexerDefinitionBuilder;
import org.gertje.regular.parser.RegExException;
import org.gertje.regular.parser.RegExParser;
import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class Utf8Test {

	@Test
	public void buildUtf8Matcher() throws RegExException {
		// RFC3629 chapter 4:

		// Syntax of UTF-8 Byte Sequences
		//
		//   For the convenience of implementors using ABNF, a definition of UTF-8
		//   in ABNF syntax is given here.
		//
		//   A UTF-8 string is a sequence of octets representing a sequence of UCS
		//   characters.  An octet sequence is valid UTF-8 only if it matches the
		//   following syntax, which is derived from the rules for encoding UTF-8
		//   and is expressed in the ABNF of [RFC2234].
		//
		//   UTF8-octets = *( UTF8-char )
		//   UTF8-char   = UTF8-1 / UTF8-2 / UTF8-3 / UTF8-4
		//   UTF8-1      = %x00-7F
		//   UTF8-2      = %xC2-DF UTF8-tail
		//   UTF8-3      = %xE0 %xA0-BF UTF8-tail / %xE1-EC 2( UTF8-tail ) /
		//                 %xED %x80-9F UTF8-tail / %xEE-EF 2( UTF8-tail )
		//   UTF8-4      = %xF0 %x90-BF 2( UTF8-tail ) / %xF1-F3 3( UTF8-tail ) /
		//                 %xF4 %x80-8F 2( UTF8-tail )
		//   UTF8-tail   = %x80-BF


		// Parse the regular expression.
		AbstractRegExNode node = new RegExParser(
				"[\\U0000-\\U007f]" +                                                           // UTF8-1
				"|[\\U00c2-\\U00df][\\U0080-\\U00bf]" +                                         // UTF8-2
				"|[\\U00e0-\\U00e0][\\U00a0-\\U00bf][\\U0080-\\U00bf]" +                        // UTF8-3 1
				"|[\\U00e1-\\U00ec][\\U0080-\\U00bf][\\U0080-\\U00bf]" +                        // UTF8-3 2
				"|[\\U00ed-\\U00ed][\\U0080-\\U009f][\\U0080-\\U00bf]" +                        // UTF8-3 3
				"|[\\U00ee-\\U00ef][\\U0080-\\U00bf][\\U0080-\\U00bf]" +                        // UTF8-3 4
				"|[\\U00f0-\\U00f0][\\U0090-\\U00bf][\\U0080-\\U00bf][\\U0080-\\U00bf]" +       // UTF8-4 1
				"|[\\U00f1-\\U00f3][\\U0080-\\U00bf][\\U0080-\\U00bf][\\U0080-\\U00bf]" +       // UTF8-4 2
				"|[\\U00f4-\\U00f4][\\U0080-\\U008f][\\U0080-\\U00bf][\\U0080-\\U00bf]" +       // UTF8-4 3
				"").parse();

		LexerTokenNode lexerTokenNode = new LexerTokenNode();
		lexerTokenNode.setName("ABC");
		lexerTokenNode.setRegEx(node);
		lexerTokenNode.setResultClassName("DEFAULT");

		LexerClassNode lexerClassNode = new LexerClassNode();
		lexerClassNode.setLexerTokenList(Arrays.asList(lexerTokenNode));
		lexerClassNode.setName("DEFAULT");

		LexerDefinitionNode lexerDefinitionNode = new LexerDefinitionNode(Arrays.asList(lexerClassNode), "DEFAULT");

		// Create the Lexer from the regex parse tree and the interval array.
		LexerDefinition lexerDefinition = new LexerDefinitionBuilder().build(lexerDefinitionNode);

	}



}
