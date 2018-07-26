package org.gertje.regular;

import org.gertje.regular.parser.RegExException;
import org.gertje.regular.parser.RegExParser;
import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.LexerClassNode;
import org.gertje.regular.parser.nodes.LexerDefinitionNode;
import org.gertje.regular.parser.nodes.LexerTokenNode;
import org.gertje.regular.parser.visitors.BuildLexerVisitor;
import org.gertje.regular.parser.visitors.IntervalCollectorVisitor;

import java.util.Arrays;

public class Main {

	public static void main(String[] args) throws RegExException {

		// Parse the regular expression.
//		AbstractRegExNode node = new RegExParser("ab?c+.|a(ab)*|[.][\\U0000-\\U000A][\\g\\\\\\t\\r\\n][^d-\\U0fff\\U00ff-^]").parse();
		AbstractRegExNode node = new RegExParser("ab+c").parse();
//		AbstractRegExNode node = new RegExParser("[a-a]|[a-b]").parse();
//		AbstractRegExNode node = new RegExParser(
//				"[\\U0000-\\U007f]" +
//				"|[\\U00c2-\\U00df][\\U0080-\\U00bf]" +
//				"|[\\U00e0-\\U00e0][\\U00a0-\\U00bf][\\U0080-\\U00bf]" +
//				"|[\\U00e1-\\U00ec][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
//				"|[\\U00ed-\\U00ed][\\U0080-\\U009f][\\U0080-\\U00bf]" +
//				"|[\\U00ee-\\U00ef][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
//				"|[\\U00f0-\\U00f0][\\U0090-\\U00bf][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
//				"|[\\U00f1-\\U00f3][\\U0080-\\U00bf][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
//				"|[\\U00f4-\\U00f4][\\U0080-\\U008f][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
//				"").parse();

//		AbstractRegExNode node = new RegExParser(
//				"[\\U0000-\\U007f]" +
////				"|" +
////				"[\\U00c2-\\U00df][\\U0080-\\U00bf]" +
////				"|" +
////				"[\\U00e0-\\U00e0][\\U00a0-\\U00bf][\\U0080-\\U00bf]" +
////				"|[\\U00e1-\\U00ec][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
////				"|[\\U00ed-\\U00ed][\\U0080-\\U009f][\\U0080-\\U00bf]" +
////				"|[\\U00ee-\\U00ef][\\U0080-\\U00bf][\\U0080-\\U00bf]" +
//				"|[\\U00f0-\\U00f0][\\U0090-\\U00bf][\\U0080-\\U008f][\\U0080-\\U008f]" +
//				"|[\\U00f1-\\U00f3][\\U0080-\\U00bf][\\U0080-\\U008f][\\U0080-\\U008f]" +
//				"|[\\U00f4-\\U00f4][\\U0080-\\U008f][\\U0080-\\U008f][\\U0080-\\U008f]" +
//				"").parse();
//		AbstractRegExNode node2 = new RegExParser("ab|cd?").parse();

		LexerTokenNode lexerTokenNode = new LexerTokenNode();
		lexerTokenNode.setName("ABC");
		lexerTokenNode.setRegEx(node);
		lexerTokenNode.setResultClassName("DEFAULT");

//		LexerTokenNode lexerTokenNode2 = new LexerTokenNode();
//		lexerTokenNode2.setName("ABCD");
//		lexerTokenNode2.setRegEx(node2);
//		lexerTokenNode2.setResultClassName("DEFAULT");

		LexerClassNode lexerClassNode = new LexerClassNode();
		lexerClassNode.setLexerTokenList(Arrays.asList(
				lexerTokenNode
//				,
//				lexerTokenNode2
			));
		lexerClassNode.setName("DEFAULT");

		LexerDefinitionNode lexerDefinitionNode = new LexerDefinitionNode(Arrays.asList(lexerClassNode), "DEFAULT");

		// Determine the array with the intervals that define the alphabet.
		int[] alphabetIntervals = IntervalUtils.splitIntervals(new IntervalCollectorVisitor().collectIntervals(lexerClassNode));
//		int alphabetSize = alphabetIntervals.length / 2;

		// Create the Lexer from the regex parse tree and the interval array.
		BuildLexerVisitor visitor = new BuildLexerVisitor();
		LexerDefinition lexerDefinition = visitor.convert(lexerDefinitionNode, alphabetIntervals);




//		// Translates a unicode codepoint to the correct input element from the alphabet.
//		int[] unicode = new int[0x100];
//
//		// Fill the array that translates unicode codepoints.
//		for (int i = 0; i < alphabetIntervals.length; i+=2) {
//			// Get the start and end from the ranges array.
//			int start = alphabetIntervals[i];
//			// Add one to the end, to make next loop prettier.
//			int end = alphabetIntervals[i + 1] + 1;
//
//			// The input is the number of the current range plus one (0 input will lead to the 0 (error) state).
//			int input = (i >> 1) + 1;
//
//			// Fill the range.
//			Arrays.fill(unicode, start, end, input);
//		}
//
//		printArray(alphabetIntervals, 2);
//		printArray(unicode, 1);
//
//		int[][] table = createTable(lexerDefinition.getDfa());
//
//		for (int[] a : table) {
//			printArray(a, 1);
//		}



		System.out.println("The end.");
	}

	private static int[][] createTable(Automaton automaton) {
		int[][] table = new int[automaton.getStateCount() + 1][automaton.getAlphabetSize() + 1];

		for (Automaton.Transition transition : automaton.getTransitions()) {
			table[transition.fromState + 1][transition.input + 1] = transition.toState + 1;
		}

		return table;
	}

	private static void printArray(int[] bla, int nr) {
		System.out.print("[");
		for (int i = 0; i < bla.length; i++) {
			if (i % 16 == 0) {
				System.out.println();
			}
			System.out.print(String.format("0x%0" + nr + "x", bla[i]));
			System.out.print(", ");
		}
		System.out.println("]");
	}


//	private static boolean match(DFATable dfaTable, String s) {
//
//		int[] ranges = new int[10];
//
//
//		// Translates a unicode codepoint to the correct input element from the alphabet.
//		int[] unicode = new int[0x110000];
//
//		// Fill the array that translates unicode codepoints.
//		for (int i = 0; i < ranges.length; i+=2) {
//			// Get the start and end from the ranges array.
//			int start = ranges[i];
//			// Add one to the end, to make next loop prettier.
//			int end = ranges[i + 1] + 1;
//
//			// The input is the number of the current range plus one (0 input will lead to the 0 (error) state).
//			int input = (i >> 1) + 1;
//
//			// Fill the range.
//			Arrays.fill(unicode, start, end, input);
//		}
//
//
//
//
//		final int length = s.length();
//
//		int start = 0;
//		int end = 0;
//
//		int[][] table = dfaTable.getTable();
//		int state = dfaTable.getStartState();
//		int[] acceptingStates = dfaTable.getAcceptingStates();
//		int[] alphabet = dfaTable.getAlphabet();
//
//
//		boolean match = false;
//
//		for (int offset = start; offset < length; ) {
//			int codepoint = s.codePointAt(offset);
//
//			int i = 0;
//			for (; i < alphabet.length; i++) {
//				if (alphabet[i] == codepoint) {
//					break;
//				}
//			}
//
//			state = table[state][i];
//
//			int finalState = state;
//			match = match || IntStream.of(acceptingStates).anyMatch(x -> x == finalState);
//
//			if (state == 0) {
//				end = offset;
//				break;
//			}
//
//			// do something with the codepoint
//			offset += Character.charCount(codepoint);
//		}
//
//		if (match) {
//			System.out.println("Match found: " + s.substring(start, end));
//		}
//
//		return match;
//	}



}
