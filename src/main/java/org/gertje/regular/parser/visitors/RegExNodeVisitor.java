package org.gertje.regular.parser.visitors;

import org.gertje.regular.parser.nodes.ConcatNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.gertje.regular.parser.nodes.OptionalNode;
import org.gertje.regular.parser.nodes.PlusNode;
import org.gertje.regular.parser.nodes.CharSetNode;
import org.gertje.regular.parser.nodes.StarNode;
import org.gertje.regular.parser.nodes.UnionNode;

public interface RegExNodeVisitor<R, X extends VisitingException> {
	R visit(CharSetNode node) throws X;
	R visit(ConcatNode node) throws X;
	R visit(LexerClassNode node) throws X;
	R visit(LexerDefinitionNode node) throws X;
	R visit(LexerTokenNode node) throws X;
	R visit(OptionalNode node) throws X;
	R visit(PlusNode node) throws X;
	R visit(StarNode node) throws X;
	R visit(UnionNode node) throws X;
}
