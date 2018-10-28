package org.gertje.regular.definition.builder;

import org.gertje.regular.parser.RegExException;
import org.gertje.regular.parser.RegExParser;
import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for lexer class nodes. Can be used from a {@link LexerDefinitionBuilder}
 */
public class LexerClassNodeBuilder {

	private LexerDefinitionBuilder parent;
	private List<LexerClassNode> lexerClassNodeList;
	private String name;

	private List<LexerTokenNode> lexerTokenNodeList;

	public LexerClassNodeBuilder(LexerDefinitionBuilder parent, List<LexerClassNode> lexerClassNodeList,
			String name) {
		this.parent = parent;
		this.lexerClassNodeList = lexerClassNodeList;
		this.name = name;

		lexerTokenNodeList = new ArrayList<>();
	}

	/**
	 * Adds a lexer token to the class node.
	 * @param name The name of the lexer token.
	 * @param regex The regex that defines the structure of the token.
	 * @param className The name of the lexer class that the lexer should transition to after a match of this type.
	 * @return The current instance of this class, so that this method can be chained.
	 */
	public LexerClassNodeBuilder addLexerToken(String name, String regex, String className) throws RegExException {
		AbstractRegExNode node = new RegExParser(regex).parse();
		LexerTokenNode lexerTokenNode = new LexerTokenNode();
		lexerTokenNode.setName(name);
		lexerTokenNode.setRegEx(node);
		lexerTokenNode.setResultClassName(className);

		lexerTokenNodeList.add(lexerTokenNode);

		return this;
	}

	/**
	 * Ends the current lexer class.
	 * @return the parent {@link LexerClassNodeBuilder}
	 */
	public LexerDefinitionBuilder end() {
		LexerClassNode lexerClassNode = new LexerClassNode();
		lexerClassNode.setName(name);
		lexerClassNode.setLexerTokenList(lexerTokenNodeList);
		lexerClassNodeList.add(lexerClassNode);

		return parent;
	}
}
