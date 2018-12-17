package io.lateralus.lexergenerator.core.automaton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Minimizes an automaton.
 *<br/>
 * To do this it uses Hopcroft's algorithm (see
 * <a href="https://en.wikipedia.org/wiki/DFA_minimization#Hopcroft's_algorithm">
 *     https://en.wikipedia.org/wiki/DFA_minimization#Hopcroft's_algorithm</a>).
 */
public class AutomatonMinimizer {

	/**
	 * Class that represents the splitter.
	 * <br/>
	 * In Hopcroft's algorithm a splitter is a tuple of a set of states and a character from the alphabet. It is used in
	 * the refinement process to split a set of states into two new sets.
	 */
	private class Splitter {
		private Set<Integer> clazz;
		private int input;

		private Splitter(Set<Integer> clazz, int input) {
			this.clazz = clazz;
			this.input = input;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Splitter splitter = (Splitter) o;

			if (input != splitter.input) return false;
			return clazz != null ? clazz.equals(splitter.clazz) : splitter.clazz == null;
		}

		@Override
		public int hashCode() {
			int result = clazz != null ? clazz.hashCode() : 0;
			result = 31 * result + input;
			return result;
		}
	}

	/**
	 * Interface for partitioning the accepting states.
	 */
	public interface AcceptingStatePartitioner {
		Set<Set<Integer>> partitionAcceptingStates(Set<Integer> acceptingStates);
	}

	private int[] oldStateNewStateMapping;

	private AcceptingStatePartitioner partitioner;

	private Automaton dfa;

	public AutomatonMinimizer(AcceptingStatePartitioner partitioner) {
		this.partitioner = partitioner;
	}

	/**
	 * Minimizes the given automaton.
	 * @param dfa The automaton to minimize. Note that for this to work the automaton should be <em>complete</em>, i.e.
	 *            for every state there should be a transition for every character.
	 * @return The minimized automaton.
	 */
	public Automaton minimize(Automaton dfa) {
		this.dfa = dfa;

		// The algorithm works by refining a partition of states based on their behaviour.

		// Determine the alphabet (capital sigma).
		int alphabetSize = dfa.getAlphabetSize();
		int stateCount = dfa.getStateCount();

		// Determine all accepting states.
		Set<Integer> acceptingStates = dfa.getAcceptingStates();

		// Since the behaviour of an accepting state is defined by its token type, we must split the set of accepting
		// states into more states. One set of accepting states per token type. (F)
		Set<Set<Integer>> acceptingStatePartitions = partitioner.partitionAcceptingStates(acceptingStates);

		// Determine all states that are not an accepting state. (Q \ F)
		Set<Integer> nonAcceptingStates = complement(determineAllStates(stateCount), acceptingStates);

		// The initial partitioning is the sets of accepting states and the rest. (P)
		Set<Set<Integer>> partitions = new HashSet<>(acceptingStatePartitions);
		partitions.add(nonAcceptingStates);

		LinkedList<Splitter> work = new LinkedList<>();

		for (Set<Integer> acceptingState : acceptingStatePartitions) {
			for (int i = 0; i < alphabetSize; i++) {
				work.add(new Splitter(acceptingState, i));
			}
		}

		// Add all partitions for the accepting states to the work supply (W).
		Splitter splitter;

		// Check to see if there is some work left to do.
		while(!work.isEmpty()) {

			// Get (and remove) the first item from the work supply.
			splitter = work.removeFirst();

			// Find all states that, given the splitter input, transition to the splitter state.
			Set<Integer> o = determineOriginStates(splitter, dfa.getTransitions());

			// If x is empty, all intersections of x with y will be empty as well. So the loop below will never
			// partition the set any further. Skip it altogether.
			if (o.isEmpty()) {
				continue;
			}

			// Loop over all (current) partitions.
			List<Set<Integer>> currentPartitions = new ArrayList<>(partitions);
			for (Set<Integer> c : currentPartitions) {

				// Find the intersection (C') between o and c.
				Set<Integer> intersection = intersect(o, c);
				if (intersection.isEmpty()) {
					continue;
				}

				// Find the complement (C'') of c on o.
				Set<Integer> complement = complement(c, o);
				if (complement.isEmpty()) {
					continue;
				}

				// Refine the partitions; replace y with the intersection and the complement.
				partitions.remove(c);
				partitions.add(intersection);
				partitions.add(complement);

				refineWork(work, c, intersection, complement);

			}
		}

		// Create new states based on the partitions; every partition represents a new state.
		return buildMinimizedDfa(partitions, dfa);
	}

	/**
	 * Refines the sets of states in the work stack.
	 * @param work The stack of splitters to process.
	 * @param original The original partition we are investigating.
	 * @param c The first part of the partition we split.
	 * @param d The second part of the partition we split.
	 */
	private void refineWork(LinkedList<Splitter> work, Set<Integer> original, Set<Integer> c, Set<Integer> d) {
		int alphabetSize = dfa.getAlphabetSize();
		Set<Integer> min = c.size() < d.size() ? c : d;

		for (int x = 0; x < alphabetSize; x++) {
			Splitter splitter = new Splitter(original, x);
			while (work.remove(splitter)) {
				work.add(new Splitter(c, x));
				work.add(new Splitter(d, x));
			}
			work.add(new Splitter(min, x));
		}
	}

	/**
	 * Builds a new DFA. Every partition represents a new states, and all (old) states in a partition are merged
	 * together.
	 * @param partitions The refined partitions.
	 * @param dfa The original {@link Automaton}.
	 * @return A minimized version of the original Automaton.
	 */
	private Automaton buildMinimizedDfa(Set<Set<Integer>> partitions, Automaton dfa) {
		Set<Integer> acceptingStates = dfa.getAcceptingStates();
		int startState = dfa.getStartState();

		Automaton minimizedDfa = new Automaton();
		oldStateNewStateMapping = new int[dfa.getStateCount()];

		// Create new states based on the partitions. Every partition represents a new state.
		for (Set<Integer> partition : partitions) {
			int newState = minimizedDfa.addState();

			for (Integer oldState : partition) {
				oldStateNewStateMapping[oldState] = newState;
				if (acceptingStates.contains(oldState)) {
					minimizedDfa.addAcceptingState(newState);
				}
				if (oldState == startState) {
					minimizedDfa.setStartState(newState);
				}
			}
		}

		// For every transition create a new transition.
		for (Automaton.Transition t : dfa.getTransitions()) {
			int newFromState = oldStateNewStateMapping[t.fromState];
			int newToState = oldStateNewStateMapping[t.toState];

			minimizedDfa.addTransition(newFromState, newToState, t.input);
		}

		minimizedDfa.setAlphabetSize(dfa.getAlphabetSize());

		return minimizedDfa;
	}

	/**
	 * Builds a set of states for all states from {@code 0} up until {@code stateCount}.
	 * @param stateCount The number of states that should be in the set.
	 * @return The set containing the states.
	 */
	private static Set<Integer> determineAllStates(int stateCount) {
		Set<Integer> set = new HashSet<>();
		for (int i = 0; i < stateCount; i++) {
			set.add(i);
		}
		return set;
	}

	/**
	 * Calculates the intersection ({@code x ∩ y}).
	 * @param x x
	 * @param y y
	 * @return A new set that represents the intersection.
	 */
	private static <T> Set<T> intersect(Set<T> x, Set<T> y) {
		Set<T> small, large;

		if (x.size() < y.size()) {
			small = x;
			large = y;
		} else {
			small = y;
			large = x;
		}

		Set<T> intersection = new HashSet<>();

		for (T element : small) {
			if (large.contains(element)) {
				intersection.add(element);
			}
		}

		return intersection;
	}

	/**
	 * Calculates the complement of y with respect to x ({@code x \ y}).
	 * @param x x
	 * @param y y
	 * @return A new set that represents the complement.
	 */
	private static <T> Set<T> complement(Set<T> x, Set<T> y) {
		Set<T> result = new HashSet<>(x);
		result.removeAll(y);

		return result;
	}

	/**
	 * Determines all states that given the input from the splitter lead to a state in the splitter.
	 * @param splitter The splitter for which to find the origin states.
	 * @param transitions A set containing all transitions in the DFA.
	 * @return The set of origin states.
	 */
	private static Set<Integer> determineOriginStates(Splitter splitter, Set<Automaton.Transition> transitions) {
		Set<Integer> result = new HashSet<>();

		for (Automaton.Transition t : transitions) {
			if (t.input == splitter.input && splitter.clazz.contains(t.toState)) {
				result.add(t.fromState);
			}
		}
		return result;
	}

	public int[] getOldStateNewStateMapping() {
		return oldStateNewStateMapping;
	}
}
