package org.gertje.regular.parser.visitors;

import org.gertje.regular.Interval;
import org.gertje.regular.parser.nodes.CharSetNode;
import org.gertje.regular.parser.nodes.ConcatNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.gertje.regular.parser.nodes.OptionalNode;
import org.gertje.regular.parser.nodes.PlusNode;
import org.gertje.regular.parser.nodes.LexerNode;
import org.gertje.regular.parser.nodes.StarNode;
import org.gertje.regular.parser.nodes.UnionNode;

import java.util.ArrayList;
import java.util.List;

public class IntervalCollectorVisitor implements RegExNodeVisitor<Void, VisitingException> {

	private List<Interval> intervalList;

	public int[] collectIntervals(LexerNode node) throws VisitingException {
		intervalList = new ArrayList<>();

		node.accept(this);

		int[] intervalArray = new int[2 * intervalList.size()];
		int i = 0;

		for (Interval interval : intervalList) {
			intervalArray[i++] = interval.getStart();
			intervalArray[i++] = interval.getEnd();
		}

		return intervalArray;
	}

	@Override
	public Void visit(CharSetNode node) throws VisitingException {
		intervalList.addAll(node.getIntervalList());
		return null;
	}

	@Override
	public Void visit(ConcatNode node) throws VisitingException {
		node.getLhs().accept(this);
		node.getRhs().accept(this);
		return null;
	}

	@Override
	public Void visit(LexerClassNode node) throws VisitingException {
		for (LexerTokenNode lexerTokenNode : node.getLexerTokenList()) {
			lexerTokenNode.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(LexerDefinitionNode node) throws VisitingException {
		for (LexerClassNode lexerClassNode : node.getLexerClassNodeList()) {
			lexerClassNode.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(LexerTokenNode node) throws VisitingException {
		node.getRegEx().accept(this);
		return null;
	}

	@Override
	public Void visit(OptionalNode node) throws VisitingException {
		node.getOperand().accept(this);
		return null;
	}

	@Override
	public Void visit(PlusNode node) throws VisitingException {
		node.getOperand().accept(this);
		return null;
	}

	@Override
	public Void visit(StarNode node) throws VisitingException {
		node.getOperand().accept(this);
		return null;
	}

	@Override
	public Void visit(UnionNode node) throws VisitingException {
		node.getLhs().accept(this);
		node.getRhs().accept(this);
		return null;
	}
}
