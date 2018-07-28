package org.gertje.regular.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Responsible for determinizing an {@link Automaton}.
 * </br><br/>
 * A generic automaton can be either deterministic or non-deterministic depending on the presence of so called
 * &epsilon;-transitions (transitions that allow for state-change without input). Any non-deterministic finite automaton
 * (NFA) can be expressed as a deterministic finite automaton (DFA).
 * <br/>
 * This class translates a NFA into a DFA using the powerset construction.
 */
public class AutomatonDeterminizer {

	/**
	 * Inner class that represents a DFA state, which is an element of the powerset of the states of the NFA.
	 */
	private static class DFAState {
		private int[] states;
		private int hash;

		private DFAState(int[] states) {
			this.states = states;
		}

		@Override
		public boolean equals(Object o) {
			DFAState dfaState = (DFAState) o;

			if (this == dfaState) {
				return true;
			}

			if (this.states.length != dfaState.states.length) {
				return false;
			}

			if (this.hashCode() != o.hashCode()) {
				return false;
			}

			return Arrays.equals(states, dfaState.states);
		}

		@Override
		public int hashCode() {
			int h = hash;
			if (h == 0 && states.length > 0) {
				h = Arrays.hashCode(states);
				hash = h;
			}
			return h;
		}
	}

	/**
	 * The NFA.
	 */
	private Automaton nfa;


	private int[] stateStack;
	private int[] stateSet;
	private boolean[] stateSetPresent;

	private Map<Integer, Set<Integer>> newStateOldStateMapping;

	public Automaton determinize(Automaton nfa) {
		// Get the starting state.
		this.nfa = nfa;

		int alphabetSize = nfa.getAlphabetSize();

		initializeForEpsilonClosureCalculation(nfa.getStateCount());

		// Create the list and the map that we will use to locally store the DFA states. The map will be used to check
		// that the DFA states are unique. The list will be needed to check that the DFA states will only be processed
		// once (i.e. they will be marked). Also note that since the Automaton class simply starts to count at 0 when
		// adding new states, the index of the state list also is the new DFA state number. Underlying algorithm relies
		// on this 'feature'.
		Map<DFAState, Integer> dfaStateMap = new HashMap<>();
		List<DFAState> stateList = new ArrayList<>();

		// The mark index indicates the highest processed item in the list with dfa states.
		int mark = 0;

		List<Automaton.Transition> transitionList = new ArrayList<>();

		// Get
		int startState = nfa.getStartState();
		int[] states = calculateEpsilonClosure(new int[]{ startState });
		DFAState dfaStartState = new DFAState(states);

		// Add a new state that will represent the new (DFA) start state.
		stateList.add(dfaStartState);

		//
		dfaStateMap.put(dfaStartState, stateList.size() - 1);

		// While there are unmarked items left.
		while (mark < stateList.size()) {
			// Note that mark is also the state number. (See the explanation above.)
			DFAState dfaState = stateList.get(mark);

			for (int input = 0; input < alphabetSize; input++) {
				// Calculate the states that can be reached from the states in the current DFA state on input and
				// allowing for epsilon transitions.
				states = calculateEpsilonClosure(calculateMove(dfaState.states, input));

				// If no states were found, continue with the next input.
				if (states.length == 0) {
					continue;
				}

				DFAState newDFAState = new DFAState(states);

				int state;
				// If the DFA state does not yet exist in our DFA add it, otherwise get its state number.
				if (!dfaStateMap.containsKey(newDFAState)) {
					stateList.add(newDFAState);
					state = stateList.size() - 1;
					dfaStateMap.put(newDFAState, state);

				} else {
					state = dfaStateMap.get(newDFAState);
				}

				// Add the transition.
				transitionList.add(new Automaton.Transition(mark, state, input));
			}

			// Mark the state as processed.
			mark++;
		}

		// Create the resulting automaton.
		Automaton dfa = new Automaton();

		// By definition the start state is state 0. (See the explanation above.)
		dfa.setStartState(0);

		newStateOldStateMapping = new HashMap<>();

		for (DFAState dfaState : stateList) {
			int i = dfa.addState();

			// Add all states to the
			newStateOldStateMapping.put(i, Arrays.stream(dfaState.states).boxed().collect(Collectors.toSet()));

			if (containsState(dfaState.states, nfa.getAcceptingStates())) {
				dfa.addAcceptingState(i);
			}
		}

		for (Automaton.Transition transition : transitionList) {
			dfa.addTransition(transition.fromState, transition.toState, transition.input);
		}

		dfa.setAlphabetSize(nfa.getAlphabetSize());

		return dfa;
	}

	/**
	 * Initializes the arrays that are needed to determine the epsilon closure.
	 * @param totalNrOfStates The total number of states in the NFA.
	 */
	private void initializeForEpsilonClosureCalculation(int totalNrOfStates) {
		stateStack = new int[totalNrOfStates];
		stateSet = new int[totalNrOfStates];
		stateSetPresent = new boolean[totalNrOfStates];
	}

	/**
	 * Calculates the epsilon closure over the given set of states.
	 * @param states A set of states to compute the epsilon closure over.
	 * @return the epsilon closure
	 */
	private int[] calculateEpsilonClosure(int[] states) {

		int[] stateStack = this.stateStack;
		int[] stateSet = this.stateSet;
		boolean[] present = this.stateSetPresent;

		// Reset the present array.
		Arrays.fill(present, false);

		// Index of the state stack.
		int i = 0;
		// Index of the state set.
		int j = 0;

		for (; i < states.length; i++) {
			int state = states[i];
			stateStack[i] = state;
			stateSet[j++] = state;
			present[state] = true;
		}

		// While the stack contains states.
		while (i > 0) {
			// Pop the top state from the stack.
			int state = stateStack[--i];
			// Get all transitions for the state.
			Set<Automaton.Transition> transitions = nfa.getTransitionSetList().get(state);

			// Loop over all transitions.
			for (Automaton.Transition transition : transitions) {
				// If the transition is not an epsilon transition, skip it.
				if (transition.input != -1) {
					continue;
				}

				// Get the state to transition to.
				int newState = transition.toState;

				// If the new state is already present in the
				if (present[newState]) {
					continue;
				}

				stateStack[i++] = newState;
				stateSet[j++] = newState;
				present[newState] = true;
			}
		}

		// Return the set of states.
		return Arrays.copyOf(stateSet, j);
	}

	/**
	 * Calculates the set of NFA states to which there is a transition from the given states on the given input symbol.
	 * @param states The states to find the transitions from.
	 * @param input The input symbol.
	 * @return the set of NFA states to which there is a transition from the given states on the given input symbol.
	 */
	private int[] calculateMove(int[] states, int input) {
		int[] stateSet = this.stateSet;
		boolean[] present = this.stateSetPresent;

		// Reset the present array.
		Arrays.fill(present, false);

		// Index of the state set.
		int i = 0;

		// Loop over all states.
		for (int state : states) {
			// Get the possible transitions from the current state.
			Set<Automaton.Transition> transitions = nfa.getTransitionSetList().get(state);

			// Loop over all transitions.
			for (Automaton.Transition transition : transitions) {
				// Ignore the transition if the input is not the given input.
				if (transition.input != input) {
					continue;
				}

				// Get the new state.
				int newState = transition.toState;

				// Ignore if the new state is already present in the state set.
				if (present[newState]) {
					continue;
				}

				// Add the state to the state set.
				stateSet[i++] = newState;
				present[newState] = true;
			}
		}

		return Arrays.copyOf(stateSet, i);
	}

	private boolean containsState(int[] left, Set<Integer> right) {

		for (int i : left) {
			if (right.contains(i)) {
				return true;
			}
		}

		return false;
	}


	public Map<Integer, Set<Integer>> getNewStateOldStateMapping() {
		return newStateOldStateMapping;
	}
}
