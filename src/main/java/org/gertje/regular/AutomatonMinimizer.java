package org.gertje.regular;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomatonMinimizer {

	private class Splitter {
		private Set<Integer> clazz;
		private int input;

		public Splitter(Set<Integer> clazz, int input) {
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

	public Automaton minimize(Automaton dfa) {
		this.dfa = dfa;

		// Hopcroft's algorithm (see https://en.wikipedia.org/wiki/DFA_minimization#Hopcroft's_algorithm).

		// The algorithm works by refining a partition of states based on their behaviour.

		// Determine the alphabet (capital sigma).
		int alphabetSize = dfa.getAlphabetSize();
		int stateCount = dfa.getStateCount();

//		int inverseTransitions[][] = new int[stateCount + 1][alphabetSize];
//		for (int[] inv : inverseTransitions) {
//			Arrays.fill(inv, stateCount);
//		}
//
//		for (Automaton.Transition t : dfa.getTransitions()) {
//			inverseTransitions[t.toState][t.input] = t.fromState;
//		}

		// Determine all accepting states.
		Set<Integer> acceptingStates = dfa.getAcceptingStates();

		// Since the behaviour of an accepting state is defined by its token type, we must split the set of accepting
		// states into more states. One set of accepting states per token type. (F)
		Set<Set<Integer>> acceptingStatePartitions = partitioner.partitionAcceptingStates(acceptingStates);

		// Determine all states that are not an accepting state. (Q \ F)
		Set<Integer> nonAcceptingStates = complement(determineAllStates(stateCount), acceptingStates);

//		Set<Integer> minAcceptingStates = acceptingStates.size() < nonAcceptingStates.size() ? acceptingStates : nonAcceptingStates;

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
//		work.addAll(acceptingStatePartitions);
//		work.add(nonAcceptingStates);
		Splitter splitter;

		// Check to see if there is some work left to do.
		while(!work.isEmpty()) {

//			printPartitions(partitions);
//			printWork(work);

			// Get (and remove) the first item from the work supply.
			splitter = work.removeFirst();

			// Find all
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

				// Replace y with the intersection and the complement.
				partitions.remove(c);
				partitions.add(intersection);
				partitions.add(complement);

//				System.out.print("refine: ");
//				printPartitions(partitions);

				refine(work, c, intersection, complement);

//				System.out.print("refine: ");
//				printWork(work);
			}


//			// Loop over all (current) partitions.
//			List<Set<Integer>> currentPartitions = new ArrayList<>(partitions);
//			for (Set<Integer> c : currentPartitions) {
//
//				// Check if c is split by the splitter.
//				for (Integer x : c) {
//					for (Automaton.Transition t : dfa.getTransitionSetList().get(x)) {
//						if (t.input == splitter.input && splitter.clazz.contains(t.toState)) {
//
//						}
//					}
//				}
//
//				// If x is empty, all intersections of x with y will be empty as well. So the loop below will never
//				// partition the set any further. Skip it altogether.
//				if (x.isEmpty()) {
//					continue;
//				}
//
//				// Loop over all (current) partitions.
//				List<Set<Integer>> currentPartitions = new ArrayList<>(partitions);
//				for (Set<Integer> y : currentPartitions) {
//
//					// Find the intersection between x and y.
//					Set<Integer> intersection = intersect(x, y);
//
//					// Find the complement of x on y.
//					Set<Integer> complement = complement(y, x);
//
//					// When there is an intersection and a complement, we must partition the set further.
//					if (!intersection.isEmpty() && !complement.isEmpty()) {
//						// Replace y with the intersection and the complement.
//						partitions.remove(y);
//						partitions.add(intersection);
//						partitions.add(complement);
//
//						// Replace Y with the intersection and the complement in the work supply or add the smallest.
//						if (work.remove(y)) {
//							work.add(complement);
//							work.add(intersection);
//						} else {
//							if (intersection.size() <= complement.size()) {
//								work.add(intersection);
//							} else {
//								work.add(complement);
//							}
//						}
//					}
//				}
//			}
		}

		// Create new states based on the partitions; every partition represents a new state.
		return buildMinimizedDfa(partitions, dfa);
	}

	private void printPartitions(Set<Set<Integer>> partitions) {
		System.out.println(partitions.stream().map(x -> {
			return x.stream().map(y -> y + 1).map(Object::toString).collect(Collectors.joining(",", "{", "}"));
		}).collect(Collectors.joining(", ")));

	}

	private void printWork(LinkedList<Splitter> work) {
		System.out.println(work.stream().map(
				s -> s.clazz.stream().map(x -> x + 1).map(Object::toString).collect(Collectors.joining(",")) + ";" + ((char)(s.input + 'a'))).collect(Collectors.joining(" | ")));
	}

	private void refine(LinkedList<Splitter> work, Set<Integer> original, Set<Integer> c, Set<Integer> d) {
		int alphabetSize = dfa.getAlphabetSize();
		Set<Integer> min = c.size() < d.size() ? c : d;

		for (int x = 0; x < alphabetSize; x++) {
			Splitter splitter = new Splitter(original, x);
			while (work.remove(splitter)) {
				work.add(new Splitter(c, x));
				work.add(new Splitter(d, x));
			}
//			work.add(new Splitter(c, x));
//			work.add(new Splitter(d, x));
			work.add(new Splitter(min, x));
		}
	}

	private Automaton buildMinimizedDfa(Set<Set<Integer>> partitions, Automaton dfa) {
		Set<Integer> acceptingStates = dfa.getAcceptingStates();
		int startState = dfa.getStartState();

		Automaton minimizedDfa = new Automaton();
		oldStateNewStateMapping = new int[dfa.getStateCount()];

		// Maak nieuwe states o.b.v. de partities. Elke partitie representeert een nieuwe state.
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

	private static void printSetSet(String x, Set<Set<Integer>> v) {
		System.out.print(x + ": {");
		for (Set<Integer> z : v) {
			System.out.print("{");
			for (Integer i : z) {
				System.out.print(i + ", ");
			}
			System.out.print("}");
		}
		System.out.println("}");
	}

	private static void printSet(String x, Set<Integer> v) {
		System.out.print(x + ": {");
		for (Integer i : v) {
			System.out.print(i + ", ");
		}
		System.out.println("}");
	}

	private static Set<Integer> determineAllStates(int stateCount) {
		Set<Integer> set = new HashSet<>();
		for (int i = 0; i < stateCount; i++) {
			set.add(i);
		}
		return set;
	}


//	private static Set<Set<Integer>> partitionAcceptingStates(Set<Integer> acceptingStates,
//			Automaton dfa) {
//
//		Map<T, Set<Integer>> tokenTypeSets = new HashMap<>();
//		for (Integer i : acceptingStates) {
//			T tokenType = dfa.getStateList().get(i);
//			if (tokenType == null) {
//				continue;
//			}
//			Set<Integer> states = tokenTypeSets.computeIfAbsent(tokenType, k -> new HashSet<>());
//			states.add(i);
//		}
//		return new HashSet<>(tokenTypeSets.values());
//	}

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

	private static <T> Set<T> complement(Set<T> x, Set<T> y) {
		Set<T> result = new HashSet<>(x);
		result.removeAll(y);

		return result;
	}

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
