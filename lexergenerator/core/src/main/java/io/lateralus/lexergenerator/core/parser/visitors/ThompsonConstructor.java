package io.lateralus.lexergenerator.core.parser.visitors;

import io.lateralus.lexergenerator.core.automaton.Automaton;
import io.lateralus.lexergenerator.core.parser.Interval;
import io.lateralus.lexergenerator.core.parser.IntervalUtils;
import io.lateralus.lexergenerator.core.parser.nodes.AbstractRegExNode;
import io.lateralus.lexergenerator.core.parser.nodes.AbstractUnaryNode;
import io.lateralus.lexergenerator.core.parser.nodes.CharSetNode;
import io.lateralus.lexergenerator.core.parser.nodes.ConcatNode;
import io.lateralus.lexergenerator.core.parser.nodes.OptionalNode;
import io.lateralus.lexergenerator.core.parser.nodes.PlusNode;
import io.lateralus.lexergenerator.core.parser.nodes.StarNode;
import io.lateralus.lexergenerator.core.parser.nodes.UnionNode;

/**
 * Constructs an NFA from a given regular expression, using Thompson's construction.
 * @see <a href="https://en.wikipedia.org/wiki/Thompson%27s_construction">Thompson's construction on Wikipedia</a>
 */
public class ThompsonConstructor implements RegExNodeVisitor<Void, VisitingException> {

	/**
	 * The start state for the current regex node.
	 */
	private int startState;

	/**
	 * The end state of the previous regex node.
	 */
	private int endState;

	/**
	 * The automaton where the construction is being added to.
	 */
	private Automaton nfa;

	/**
	 * Array containing the alphabet intervals.
	 */
	private int[] alphabetIntervals;

	public ThompsonConstructor(Automaton nfa, int startState, int[] alphabetIntervals) {
		this.nfa = nfa;
		this.startState = startState;
		this.alphabetIntervals = alphabetIntervals;
	}

	/**
	 * Constructs the NFA.
	 * @param node The node that represents the regular expression to construct the NFA from.
	 */
	public void construct(AbstractRegExNode node) {
		try {
			node.accept(this);
		} catch (VisitingException e) {
			// The methods in this class never throw an exception, thus we should never reach this catch.
			throw new IllegalStateException("Caught visiting exception while the Thompson constructor never throws: " + e.getMessage());
		}
	}

	@Override
	public Void visit(CharSetNode node) {

		// Create the end state for this node.
		endState = nfa.addState();

		// Loop over all ranges.
		for (Interval interval : node.getIntervalList()) {
			// Get all intervals from the interval array that make up this interval.
			int[] subIntervalArray = IntervalUtils.findSubIntervals(interval.getStart(), interval.getEnd(), alphabetIntervals);

			for (int input : subIntervalArray) {
				nfa.addTransition(startState, endState, input);
			}
		}

		return null;
	}

	@Override
	public Void visit(ConcatNode node) throws VisitingException {
		// Add the regex for the lhs to the NFA, use the current start state.
		node.getLhs().accept(this);

		// The start state for the rhs, is the end state of the lhs.
		startState = endState;

		// Add the regex for the rhs to the NFA.
		node.getRhs().accept(this);

		// Keep the current end state.

		return null;
	}

	@Override
	public Void visit(OptionalNode node) throws VisitingException {
		// Get the start state for this node.
		int localStartState = startState;

		// Add the regex for the operand to the NFA, use the current start state.
		node.getOperand().accept(this);

		// Add an epsilon transition from the local start state to the end state.
		nfa.addEpsilonTransition(localStartState, endState);

		return null;
	}

	@Override
	public Void visit(PlusNode node) throws VisitingException {

		// Create the states and transitions for the plus node.
		addPlus(node);

		return null;
	}

	@Override
	public Void visit(StarNode node) throws VisitingException {
		// Create the states and transitions for the plus node.
		int localStartState = addPlus(node);

		// Add epsilon transition from the local start state to the end state. (This is the only difference between the
		// plus and star node.)
		nfa.addEpsilonTransition(localStartState, endState);

		return null;
	}

	@Override
	public Void visit(UnionNode node) throws VisitingException {
		// Get the start state for this node.
		int localStartState = startState;

		// Create a new start state for the lhs operand of this node.
		startState = nfa.addState();

		// Wire the states up.
		nfa.addEpsilonTransition(localStartState, startState);

		// Add the regex for the lhs to the NFA.
		node.getLhs().accept(this);

		// Get the end state for the lhs.
		int lhsEndState = endState;

		// Create a new start state for the lhs operand of this node.
		startState = nfa.addState();

		// Wire the states up.
		nfa.addEpsilonTransition(localStartState, startState);

		// Add the regex for the rhs to the NFA.
		node.getRhs().accept(this);

		// Get the end state for the rhs.
		int rhsEndState = endState;

		// Create the end state for this node.
		endState = nfa.addState();

		// Wire the states up.
		nfa.addEpsilonTransition(lhsEndState, endState);
		nfa.addEpsilonTransition(rhsEndState, endState);

		return null;
	}

	private int addPlus(AbstractUnaryNode node) throws VisitingException {

		// Get the start state for this node.
		int localStartState = startState;

		// Create a new start state for the operand of this node.
		int operandStartState = nfa.addState();
		startState = operandStartState;

		// Wire the states up.
		nfa.addEpsilonTransition(localStartState, startState);

		// Add the regex for the operand to the NFA.
		node.getOperand().accept(this);

		int operandEndState = endState;

		// Add epsilon transition from the end state of the operand to the begin state of the operand.
		nfa.addEpsilonTransition(operandEndState, operandStartState);

		// Create the end state for this node.
		endState = nfa.addState();

		// Add epsilon transition from the operand end state to the end state.
		nfa.addEpsilonTransition(operandEndState, endState);

		return localStartState;
	}

	public int getEndState() {
		return endState;
	}
}
