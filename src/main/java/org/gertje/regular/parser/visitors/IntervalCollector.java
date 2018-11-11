package org.gertje.regular.parser.visitors;

import org.gertje.regular.parser.Interval;
import org.gertje.regular.parser.IntervalUtils;
import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.CharSetNode;
import org.gertje.regular.parser.nodes.ConcatNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDescriptionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.gertje.regular.parser.nodes.OptionalNode;
import org.gertje.regular.parser.nodes.PlusNode;
import org.gertje.regular.parser.nodes.StarNode;
import org.gertje.regular.parser.nodes.UnionNode;

import java.util.ArrayList;
import java.util.List;

public class IntervalCollector implements RegExNodeVisitor<Void, VisitingException> {

	private List<Interval> intervalList;

	private IntervalCollector() {
		intervalList = new ArrayList<>();
	}

	public static int[] collectIntervals(LexerDescriptionNode node) {
		IntervalCollector collector  = new IntervalCollector();

		for (LexerClassNode lexerClassNode : node.getLexerClassNodeList()) {
			for (LexerTokenNode lexerTokenNode : lexerClassNode.getLexerTokenList()) {
				collector.collectIntervals(lexerTokenNode.getRegEx());
			}
		}

		int[] intervalArray = new int[2 * collector.intervalList.size()];
		int i = 0;

		for (Interval interval : collector.intervalList) {
			intervalArray[i++] = interval.getStart();
			intervalArray[i++] = interval.getEnd();
		}

		return IntervalUtils.splitIntervals(intervalArray);
	}

	private void collectIntervals(AbstractRegExNode regExNode) {
		try {
			regExNode.accept(this);
		} catch (VisitingException e) {
			// None of the visit methods of this class throw a VisitException.
			throw new IllegalStateException("Caught visiting exception while the interval collector never throws: " + e.getMessage());
		}
	}

	@Override
	public Void visit(CharSetNode node) {
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
