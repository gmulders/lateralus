package io.lateralus.lexergenerator.core.parser.visitors;

import io.lateralus.lexergenerator.core.parser.nodes.CharSetNode;
import io.lateralus.lexergenerator.core.parser.nodes.ConcatNode;
import io.lateralus.lexergenerator.core.parser.nodes.OptionalNode;
import io.lateralus.lexergenerator.core.parser.nodes.PlusNode;
import io.lateralus.lexergenerator.core.parser.nodes.StarNode;
import io.lateralus.lexergenerator.core.parser.nodes.UnionNode;

public interface RegExNodeVisitor<R, X extends VisitingException> {
	R visit(CharSetNode node) throws X;
	R visit(ConcatNode node) throws X;
	R visit(OptionalNode node) throws X;
	R visit(PlusNode node) throws X;
	R visit(StarNode node) throws X;
	R visit(UnionNode node) throws X;
}
