package org.gertje.regular.definition;

import org.gertje.regular.DebugUtils;
import org.gertje.regular.automaton.Automaton;
import org.gertje.regular.automaton.AutomatonDeterminizer;
import org.gertje.regular.automaton.AutomatonMinimizer;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.gertje.regular.parser.visitors.IntervalCollector;
import org.gertje.regular.parser.visitors.ThompsonConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds a lexer definition ({@link LexerDefinition}) given a lexer definition node ({@link LexerDefinitionNode}).
 *
 * TODO: Find more suitable name.
 */
public class LexerDefinitionBuilder {

	/**
	 * The error state for the lexer. Note that the concept of an error state is something that is required for a lexer,
	 * but does not exist in the definition of automata. Therefor we store this state here.
	 */
	private int lexerErrorState;

	/**
	 * Mapping from a lexer class name -> lexer class index
	 */
	private Map<String, Integer> lexerClassIndexMap = new HashMap<>();

	/**
	 * Mapping from lexer class index -> lexer class start state
	 *
	 * Note that this lexer state is not an automaton state, but an input that brings the automaton from the start state
	 * to the start state of the lexer.
	 */
	private Map<Integer, Integer> lexerClassStartStateMap = new HashMap<>();

	/**
	 * The list with all token types.
	 */
	private List<LexerDefinition.TokenType> tokenTypeList = new ArrayList<>();

	/**
	 * Mapping from an accepting state -> token type.
	 */
	private Map<Integer, LexerDefinition.TokenType> acceptingStateTokenTypeMap = new HashMap<>();

	/**
	 * The number of classes in the lexer.
	 */
	private int nrOfClasses;

	public LexerDefinitionBuilder() {
	}

	/**
	 * Builds a lexer definition.
	 * @param node The lexer definition node to build the lexer definition from.
	 * @return The lexer definition.
	 */
	public LexerDefinition build(LexerDefinitionNode node) {

		// Determine the alphabet.
		int[] alphabetIntervals = IntervalCollector.collectIntervals(node);

		Automaton nfa = new Automaton();

		// Determine the size of the alphabet.
		int alphabetSize = Math.max(alphabetIntervals.length / 2, node.getLexerClassNodeList().size());
		nfa.setAlphabetSize(alphabetSize);

		// Create a start state.
		nfa.setStartState(nfa.addState());

		// Add an NFA for every lexer class node in the definition.
		for (LexerClassNode lexerClassNode : node.getLexerClassNodeList()) {
			processLexerClass(lexerClassNode, nfa, alphabetIntervals);
		}

		// Determinize the NFA to get a DFA.
		Automaton dfa = determinize(nfa);

		// Complete the DFA.
		dfa = complete(dfa);

		// Minimize the DFA.
		dfa = minimize(dfa);

		DebugUtils.printAutomaton(dfa, alphabetIntervals, lexerErrorState, true);

		Map<LexerDefinition.TokenType, Set<Integer>> newMap = new HashMap<>();
		acceptingStateTokenTypeMap.forEach((i, t) -> newMap.computeIfAbsent(t, t2 -> new HashSet<>()).add(i));

		newMap.forEach((t, s) -> System.out.println(t.getName() + " " + Arrays.toString(s.toArray())));

		// Create the lexer definition.
		return createLexerDefinition(dfa, alphabetIntervals, node.getStartLexerStateName());
	}

	/**
	 * Creates the lexer definition.
	 *
	 * @param dfa The DFA to create the definition from.
	 * @param alphabetIntervals The alphabet.
	 * @param startLexerStateName The name of the lexer state to start in.
	 * @return The lexer definition.
	 */
	private LexerDefinition createLexerDefinition(Automaton dfa, int[] alphabetIntervals, String startLexerStateName) {
		String[] lexerClassNames = new String[lexerClassIndexMap.size()];

		for (Map.Entry<String, Integer> entry : lexerClassIndexMap.entrySet()) {
			lexerClassNames[entry.getValue()] = entry.getKey();
		}

		LexerDefinition lexerDefinition = new LexerDefinition();

		lexerDefinition.setLexerClassNames(lexerClassNames);
		lexerDefinition.setDfa(dfa);
		lexerDefinition.setTokenTypeList(tokenTypeList);
		lexerDefinition.setAcceptingStateTokenTypes(acceptingStateTokenTypeMap);
		lexerDefinition.setAlphabetIntervals(alphabetIntervals);
		lexerDefinition.setStartLexerState(lexerClassIndexMap.get(startLexerStateName));
		lexerDefinition.setErrorState(lexerErrorState);

		return lexerDefinition;
	}

	/**
	 * Processes the given lexer class node:
	 * <ol>
	 *     <li>determines the start state for the lexer class,</li>
	 *     <li>adds a transition from the lexer start state to the lexer class start state over the index of the lexer
	 *     class (i.e. the lexer class index is the input) and</li>
	 *     <li>processes every token that is part of this class.</li>
	 * </ol>
	 *
	 * @param node The node to process.
	 * @param nfa The {@link Automaton} to add the NFA to.
	 * @param alphabetIntervals The alphabet.
	 */
	private void processLexerClass(LexerClassNode node, Automaton nfa, int[] alphabetIntervals) {
		int lexerClassIndex = determineLexerClassIndex(node.getName());
		int lexerClassStartState = determineLexerClassStartState(nfa, lexerClassIndex);

		nfa.addTransition(nfa.getStartState(), lexerClassStartState, lexerClassIndex);

		for (LexerTokenNode lexerToken : node.getLexerTokenList()) {
			int localStartState = nfa.addState();
			processLexerTokenNode(lexerToken, nfa, localStartState, alphabetIntervals);
			// end en start zijn gezet.
			nfa.addEpsilonTransition(lexerClassStartState, localStartState);
		}
	}

	/**
	 * Determines the start state for the Automaton whenever the lexer is in the state with the given index.
	 * @param automaton The automaton.
	 * @param lexerClassIndex The index of the lexer class.
	 * @return The start state.
	 */
	private Integer determineLexerClassStartState(Automaton automaton, int lexerClassIndex) {
		return lexerClassStartStateMap.computeIfAbsent(lexerClassIndex, i -> automaton.addState());
	}

	/**
	 * Processes the given lexer token node:
	 * <ol>
	 *     <li>constructs the NFA for the token's regular expression,</li>
	 *     <li>registers the end state of the regular expression as an accepting state and</li>
	 *     <li>creates and registers the result token type</li>
	 * </ol>
	 * @param node The node to process.
	 * @param nfa The {@link Automaton} to add the NFA to.
	 * @param startState The start state for the added NFA.
	 * @param alphabetIntervals The alphabet.
	 */
	private void processLexerTokenNode(LexerTokenNode node, Automaton nfa, int startState, int[] alphabetIntervals) {

		// Create the ThompsonConstructor and have it add the NFA for the regular expression to the automaton.
		ThompsonConstructor constructor = new ThompsonConstructor(nfa, startState, alphabetIntervals);
		constructor.construct(node.getRegEx());

		// Get the end state of the regex we just visited.
		int endState = constructor.getEndState();

		// Register the accepting state.
		nfa.addAcceptingState(endState);

		// Determine the lexer class index of the token, add the class if it does not exist.
		int lexerClassIndex = determineLexerClassIndex(node.getResultClassName());
		LexerDefinition.TokenType currentTokenType = new LexerDefinition.TokenType(tokenTypeList.size(), node.getName(), lexerClassIndex);

		tokenTypeList.add(currentTokenType);

		acceptingStateTokenTypeMap.put(endState, currentTokenType);
	}

	/**
	 * Determines the index of the class with the given name.
	 * @param className The name of the class.
	 * @return the index of the class.
	 */
	private int determineLexerClassIndex(String className) {
		return lexerClassIndexMap.computeIfAbsent(className, s -> nrOfClasses++);
	}

	/**
	 * Completes the given DFA ({@link Automaton}) i.e. makes sure that the automaton has a transition for every state
	 * for every element in the alphabet. Missing transitions are regarded as a transition to the error state.
	 *
	 * @param dfa The DFA to complete.
	 * @return The completed DFA.
	 */
	private Automaton complete(Automaton dfa) {

		// We make use of the fact that a int[][] is initialized with 0's. We create a table with an extra state (0)
		// that will represent the error state.
		int[][] table = new int[dfa.getStateCount() + 1][dfa.getAlphabetSize()];

		// The error state is 0.
		lexerErrorState = 0;

		// Fill the table with all transitions. All transitions (from state / input combinations) that are not filled
		// now will automatically be a transition to the error state.
		for (Automaton.Transition t : dfa.getTransitions()) {
			table[t.fromState + 1][t.input] = t.toState + 1;
		}

		// Create the automaton that represents the new DFA.
		Automaton newDfa = new Automaton();

		// Add a state for every state in the old DFA.
		for (int i = 0; i < dfa.getStateCount() + 1; i++) {
			newDfa.addState();
		}

		// Add a transition for every element in the table.
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				newDfa.addTransition(i, table[i][j], j);
			}
		}

		// The start state is the previous start state plus 1.
		newDfa.setStartState(dfa.getStartState() + 1);

		// All the accepting states are the previous accepting states plus 1.
		for (int i : dfa.getAcceptingStates()) {
			newDfa.addAcceptingState(i + 1);
		}

		// Alphabet size of the new DFA remains the same.
		newDfa.setAlphabetSize(dfa.getAlphabetSize());

		Map<Integer, LexerDefinition.TokenType> newAcceptingStateTokenTypeMap = new HashMap<>();

		// Loop over all current accepting states with a token type.
		for (Map.Entry<Integer, LexerDefinition.TokenType> oldEntry : acceptingStateTokenTypeMap.entrySet()) {
			newAcceptingStateTokenTypeMap.put(oldEntry.getKey() + 1, oldEntry.getValue());
		}

		acceptingStateTokenTypeMap = newAcceptingStateTokenTypeMap;

		return newDfa;
	}

	/**
	 * Determinizes the given NFA using the powerset construction.
	 * @param nfa The non deterministic finite automaton to determinize.
	 * @return The DFA.
	 */
	private Automaton determinize(Automaton nfa) {
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

	/**
	 * Minimizes the given (complete) DFA using Hopcroft's algorithm.
	 * @param dfa The deterministic finite automaton to minimize.
	 * @return The minimized DFA.
	 */
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
		lexerErrorState = mapping[lexerErrorState];

		return dfa;
	}
}
