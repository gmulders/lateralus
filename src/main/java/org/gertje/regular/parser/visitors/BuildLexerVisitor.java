package org.gertje.regular.parser.visitors;

import org.gertje.regular.Automaton;
import org.gertje.regular.AutomatonDeterminizer;
import org.gertje.regular.AutomatonMinimizer;
import org.gertje.regular.IntervalUtils;
import org.gertje.regular.Interval;
import org.gertje.regular.LexerDefinition;
import org.gertje.regular.parser.nodes.AbstractUnaryNode;
import org.gertje.regular.parser.nodes.ConcatNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.gertje.regular.parser.nodes.OptionalNode;
import org.gertje.regular.parser.nodes.PlusNode;
import org.gertje.regular.parser.nodes.CharSetNode;
import org.gertje.regular.parser.nodes.StarNode;
import org.gertje.regular.parser.nodes.UnionNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildLexerVisitor implements RegExNodeVisitor<Void, VisitingException> {

	private int startState;
	private int endState;

	private LexerDefinition lexerDefinition;

	private Automaton nfa;

	private int[] alphabetIntervals;

	private int lexerStartState;

	private Map<String, Integer> lexerClassIndexMap = new HashMap<>();
	private Map<Integer, Integer> lexerClassStartStateMap = new HashMap<>();

	private Map<Integer, LexerDefinition.TokenType> acceptingStateTokenTypeMap = new HashMap<>();

	private int nrOfClasses;

	private int nrOfTokens;

	public LexerDefinition convert(LexerDefinitionNode node, int[] alphabetIntervals) throws VisitingException {
		this.alphabetIntervals = alphabetIntervals;

		visit(node);

		return lexerDefinition;
	}

	private int calculateNextLexerClassIndex(String lexerClassName) {
		return nrOfClasses++;
	}

	private int calculateClassStartState(int lexerClassIndex) {
		int state = nfa.addState();
		lexerClassStartStateMap.put(lexerClassIndex, state);
		return state;
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
	public Void visit(LexerClassNode node) throws VisitingException {
		int lexerClassIndex = lexerClassIndexMap.computeIfAbsent(node.getName(), this::calculateNextLexerClassIndex);
		int lexerClassStartState = lexerClassStartStateMap.computeIfAbsent(lexerClassIndex, this::calculateClassStartState);

		nfa.addTransition(lexerStartState, lexerClassStartState, lexerClassIndex);

		for (LexerTokenNode lexerToken : node.getLexerTokenList()) {
			int localStartState = startState = nfa.addState();
			lexerToken.accept(this);
			// end en start zijn gezet.
			nfa.addEpsilonTransition(lexerClassStartState, localStartState);
		}

		return null;
	}

	@Override
	public Void visit(LexerDefinitionNode node) throws VisitingException {
		// Create a new Automaton
		nfa = new Automaton();

		// Determine the size of the alphabet.
		int alphabetSize = Math.max(alphabetIntervals.length / 2, node.getLexerClassNodeList().size());
		nfa.setAlphabetSize(alphabetSize);

		// Create a start state for the NFA.
		lexerStartState = nfa.addState();
		nfa.setStartState(lexerStartState);

		for (LexerClassNode lexerClassNode : node.getLexerClassNodeList()) {
			lexerClassNode.accept(this);
		}

//		printAutomaton(nfa, alphabetIntervals, false);

		Automaton dfa = determinize();

//		printAutomaton(dfa, alphabetIntervals, false);

//		List<Set<Automaton.Transition>> list = dfa.getTransitionSetList();
//		for (int i = 0; i < list.size(); i++) {
//			Set<Automaton.Transition> set = dfa.getTransitionSetList().get(i);
//
//			System.out.print("'" + determineChar(i) + "'");
//			System.out.print(": {");
//			for (Automaton.Transition t : set) {
//				System.out.print("'" + t.input + "': '" + determineChar(t.toState) + "',");
//			}
//			System.out.println("},");
//		}

//		String acceptingStates = dfa.getAcceptingStates().stream().map(this::determineChar).map(Object::toString)
//				.collect(Collectors.joining(" "));
//
//		System.out.println(acceptingStates);


		dfa = complete(dfa);

		dfa = minimize(dfa);

//		printAutomaton(dfa, alphabetIntervals, true);




		String[] lexerClassNames = new String[lexerClassIndexMap.size()];

		for (Map.Entry<String, Integer> entry : lexerClassIndexMap.entrySet()) {
			lexerClassNames[entry.getValue()] = entry.getKey();
		}


		lexerDefinition = new LexerDefinition();

		lexerDefinition.setLexerClassNames(lexerClassNames);
		lexerDefinition.setDfa(dfa);
		lexerDefinition.setAcceptingStateTokenTypes(acceptingStateTokenTypeMap);
		lexerDefinition.setAlphabetIntervals(alphabetIntervals);
		lexerDefinition.setLexerStartState(lexerClassIndexMap.get(node.getLexerStartStateName()));
		return null;
	}

	private Automaton complete(Automaton oldDfa) {
		Automaton newDfa = new Automaton();

		// Maak array ding aan
		int[][] table = new int[oldDfa.getStateCount() + 1][oldDfa.getAlphabetSize()];

		for (Automaton.Transition t : oldDfa.getTransitions()) {
			table[t.fromState + 1][t.input] = t.toState + 1;
		}

		for (int i = 0; i < oldDfa.getStateCount() + 1; i++) {
			newDfa.addState();
		}

		// Loop over alle dingen
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				newDfa.addTransition(i, table[i][j], j);
			}
		}

		newDfa.setStartState(oldDfa.getStartState() + 1);

		for (int i : oldDfa.getAcceptingStates()) {
			newDfa.addAcceptingState(i + 1);
		}

		newDfa.setAlphabetSize(oldDfa.getAlphabetSize());

		Map<Integer, LexerDefinition.TokenType> newAcceptingStateTokenTypeMap = new HashMap<>();

		// Loop over all current accepting states with a token type.
		for (Map.Entry<Integer, LexerDefinition.TokenType> oldEntry : acceptingStateTokenTypeMap.entrySet()) {
			newAcceptingStateTokenTypeMap.put(oldEntry.getKey() + 1, oldEntry.getValue());
		}

		acceptingStateTokenTypeMap = newAcceptingStateTokenTypeMap;

		return newDfa;
	}

	private char determineChar(int i) {
		if (i >= 0 && i <= 25) {
			return (char)('a' + i);
		}
		return (char)('A' + i - 26);
	}

	private Automaton determinize() {
		AutomatonDeterminizer determinizer = new AutomatonDeterminizer();
		Automaton dfa = determinizer.determinize(nfa);

		// After determinization we need to update our accepting states.
		Map<Integer, Set<Integer>> mapping = determinizer.getNewStateOldStateMapping();

		Map<Integer, LexerDefinition.TokenType> newAcceptingStateTokenTypeMap = new HashMap<>();

		// Loop over all current accepting states with a token type.
		for (Map.Entry<Integer, LexerDefinition.TokenType> oldEntry : acceptingStateTokenTypeMap.entrySet()) {
			int oldState = oldEntry.getKey();
			for (Map.Entry<Integer, Set<Integer>> mappingEntry : mapping.entrySet()) {
				if (mappingEntry.getValue().contains(oldState)) {
					Integer newState = mappingEntry.getKey();
					LexerDefinition.TokenType tokenType = newAcceptingStateTokenTypeMap.get(newState);
					if (tokenType == null || tokenType.ordinal() > oldEntry.getValue().ordinal()) {
						newAcceptingStateTokenTypeMap.put(newState, oldEntry.getValue());
					}
				}
			}
		}

		acceptingStateTokenTypeMap = newAcceptingStateTokenTypeMap;
		return dfa;
	}

	private Automaton minimize(Automaton dfa) {

		AutomatonMinimizer minimizer = new AutomatonMinimizer(acceptingStates -> {
			// We will create a new partition of states per token type.
			Map<LexerDefinition.TokenType, Set<Integer>> tokenTypeAcceptingStates = new HashMap<>();

			for (Integer state : acceptingStates) {
				// Determine the token type for the accepting state.
				LexerDefinition.TokenType tokenType = acceptingStateTokenTypeMap.get(state);

				// Determine the set of integers for the token type and add the state.
				tokenTypeAcceptingStates.computeIfAbsent(tokenType, x -> new HashSet<>()).add(state);
			}
			return new HashSet<>(tokenTypeAcceptingStates.values());
		});

		dfa = minimizer.minimize(dfa);

		int[] mapping = minimizer.getOldStateNewStateMapping();
		Map<Integer, LexerDefinition.TokenType> newAcceptingStateTokenTypeMap = new HashMap<>();

		// Loop over all current accepting states with a token type.
		for (Map.Entry<Integer, LexerDefinition.TokenType> e : acceptingStateTokenTypeMap.entrySet()) {
			newAcceptingStateTokenTypeMap.put(mapping[e.getKey()], e.getValue());
		}

		acceptingStateTokenTypeMap = newAcceptingStateTokenTypeMap;

		return dfa;
	}

	@Override
	public Void visit(LexerTokenNode node) throws VisitingException {
		int lexerClassIndex = lexerClassIndexMap.computeIfAbsent(node.getResultClassName(), this::calculateNextLexerClassIndex);
		LexerDefinition.TokenType currentTokenType = new LexerDefinition.TokenType(nrOfTokens++, node.getName(), lexerClassIndex);


		node.getRegEx().accept(this);

		// End and start start states are set.

		// Register the accepting state.
		nfa.addAcceptingState(endState);

		acceptingStateTokenTypeMap.put(endState, currentTokenType);


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

	private static void printAutomaton(Automaton automaton, int[] alphabetIntervals, boolean skipErrorState) {

		String acceptingStates = automaton.getAcceptingStates().stream().map(i -> i+1).map(Object::toString)
				.collect(Collectors.joining(" "));

		System.out.println("digraph finite_state_machine {\n" +
				"\trankdir=LR;\n" +
				"\tsize=\"8,5\"\n" +
				"\tnode [shape = doublecircle]; " + acceptingStates + "\n" +
				"\tnode [shape = circle];");

		for (Automaton.Transition transition : automaton.getTransitions()) {
			if (skipErrorState && transition.toState == 0) {
				continue;
			}
			String input = determineInput(transition.input, alphabetIntervals);
			System.out.println("\t" + (transition.fromState + 1) + " -> " + (transition.toState + 1) + " [ label = \"" + input + "\" ];");
		}

		System.out.println("}");
	}

	private static String determineInput(int input, int[] alphabetIntervals) {
		if (input < 0) {
			return "epsilon";
		}

		int start = alphabetIntervals[input * 2];
		int end = alphabetIntervals[input * 2 + 1];

		return String.format("%02x", start) + ".." + String.format("%02x", end);
	}
}
