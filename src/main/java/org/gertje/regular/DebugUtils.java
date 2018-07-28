package org.gertje.regular;

import org.gertje.regular.automaton.Automaton;

import java.util.Set;
import java.util.stream.Collectors;

public class DebugUtils {


	public static void printSetSet(String x, Set<Set<Integer>> v) {
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

	public static void printSet(String x, Set<Integer> v) {
		System.out.print(x + ": {");
		for (Integer i : v) {
			System.out.print(i + ", ");
		}
		System.out.println("}");
	}

	public static void printAutomaton(Automaton automaton, int[] alphabetIntervals, boolean skipErrorState) {

		String acceptingStates = automaton.getAcceptingStates().stream().map(i -> i - 1).map(Object::toString)
				.collect(Collectors.joining(" "));

		System.out.println("digraph finite_state_machine {\n" +
				"\trankdir=LR;\n" +
				"\tsize=\"8,5\"\n" +
				"\tnode [shape = point]; start;\n" +
				"\tnode [shape = doublecircle]; " + acceptingStates + "\n" +
				"\tnode [shape = circle];\n" +
				"\tstart -> 1");

		for (Automaton.Transition transition : automaton.getTransitions()) {
			if (skipErrorState && transition.toState == 0) {
				continue;
			}
			String input = determineInput(transition.input, alphabetIntervals);
			System.out.println("\t" + (transition.fromState - 1) + " -> " + (transition.toState - 1) + " [ label = \"" + input + "\" ];");
		}

		System.out.println("}");
	}

	private static String determineInput(int input, int[] alphabetIntervals) {
		if (input < 0) {
			return "&epsilon;";
		}

		int start = alphabetIntervals[input * 2];
		int end = alphabetIntervals[input * 2 + 1];

		return String.format("%02x", start) + ".." + String.format("%02x", end);
	}

}
