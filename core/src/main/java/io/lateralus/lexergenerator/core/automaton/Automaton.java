package io.lateralus.lexergenerator.core.automaton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Finite State Automaton (FSA).
 * <p>
 * An Automaton consists of states and transitions between those states. An input symbol (that is part of the alphabet of the
 * Automaton) triggers the transition between states.
 * <p>
 * In this implementation every state is represented by an integer.
 * To transition between states we use a positive integer as input or {@code -1} for the epsilon transition.
 */
public class Automaton {

	/**
	 * Object representing a transition between states.
	 * <p>
	 * A transition is triggered by an input symbol in the interval of code points.
	 */
	public static class Transition {

		/**
		 * The state to transition from.
		 */
		public final int fromState;

		/**
		 * The state to transition to.
		 */
		public final int toState;

		/**
		 * The input for this transition. The input -1 is the epsilon.
		 */
		public final int input;

		/**
		 * Constructor.
		 * @param toState The state to transition to.
		 * @param input The input for this transition.
		 */
		public Transition(final int fromState, final int toState, final int input) {
			this.fromState = fromState;
			this.toState = toState;
			this.input = input;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Transition that = (Transition) o;

			if (fromState != that.fromState) return false;
			if (toState != that.toState) return false;
			return input == that.input;
		}

		@Override
		public int hashCode() {
			int result = fromState;
			result = 31 * result + toState;
			result = 31 * result + input;
			return result;
		}
	}

	/**
	 * The number of states.
	 */
	private int stateCount;

	/**
	 * Per state a set of possible transitions from that state.
	 */
	private List<Set<Transition>> transitionSetList;

	/**
	 * List of all transitions.
	 */
	private Set<Transition> transitions;

	/**
	 * The (total) number of transitions.
	 */
	private int transitionCount;

	/**
	 * The start state.
	 */
	private int startState;

	/**
	 * The set of accepting states.
	 */
	private Set<Integer> acceptingStates;

	/**
	 * The size of the alphabet for this automaton. Since the alphabet is just 0, 1, 2, ... n-1, this number also
	 * defines the alphabet.
	 */
	private int alphabetSize;

	/**
	 * Constructor.
	 */
	public Automaton() {
		transitionSetList = new ArrayList<>();
		transitions = new HashSet<>();
		acceptingStates = new HashSet<>();
	}

	/**
	 * Adds a state to the list with states.
	 * @return The integer representing the state.
	 */
	public int addState() {
		transitionSetList.add(new HashSet<>());
		return stateCount++;
	}

	public void addAcceptingState(int state) {
		acceptingStates.add(state);
	}

	/**
	 * Adds the given state to the set of start states.
	 * @param state The state to add.
	 */
	public void setStartState(int state) {
		startState = state;
	}

	/**
	 * Adds a transition from {@code fromState} to {@code toState} triggered by one of the code points in the range
	 * {@code fromCodePoint} to {@code toCodePoint}.
	 * @param fromState The state to start from.
	 * @param toState The state to go to.
	 * @param input The input for this transition.
	 */
	public void addTransition(int fromState, int toState, int input) {
		Transition transition = new Transition(fromState, toState, input);
		transitionSetList.get(fromState).add(transition);
		transitions.add(transition);
		transitionCount++;
	}

	/**
	 * Adds an epsilon transition from {@code fromState} to {@code toState}.
	 * @param fromState The state to start from.
	 * @param toState The state to go to.
	 */
	public void addEpsilonTransition(int fromState, int toState) {
		addTransition(fromState, toState, -1);
	}

	public int getStateCount() {
		return stateCount;
	}

	public Set<Transition> getTransitions() {
		return transitions;
	}

	public int getTransitionCount() {
		return transitionCount;
	}

	public List<Set<Transition>> getTransitionSetList() {
		return transitionSetList;
	}

	public int getStartState() {
		return startState;
	}

	public Set<Integer> getAcceptingStates() {
		return acceptingStates;
	}

	public void setAlphabetSize(int alphabetSize) {
		this.alphabetSize = alphabetSize;
	}

	public int getAlphabetSize() {
		return alphabetSize;
	}
}
