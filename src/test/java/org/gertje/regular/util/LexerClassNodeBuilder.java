
package org.gertje.regular.util;

import org.gertje.regular.parser.RegExException;
import org.gertje.regular.parser.RegExParser;
import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;

import java.util.ArrayList;
import java.util.List;

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

	public LexerClassNodeBuilder addLexerToken(String name, String regex, String className) throws RegExException {
		AbstractRegExNode node = new RegExParser(regex).parse();
		LexerTokenNode lexerTokenNode = new LexerTokenNode();
		lexerTokenNode.setName(name);
		lexerTokenNode.setRegEx(node);
		lexerTokenNode.setResultClassName(className);

		lexerTokenNodeList.add(lexerTokenNode);

		return this;
	}

	public LexerDefinitionBuilder end() {
		LexerClassNode lexerClassNode = new LexerClassNode();
		lexerClassNode.setName(name);
		lexerClassNode.setLexerTokenList(lexerTokenNodeList);
		lexerClassNodeList.add(lexerClassNode);

		return parent;
	}
}
