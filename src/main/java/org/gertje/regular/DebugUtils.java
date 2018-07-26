package org.gertje.regular;

import java.util.Set;

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


}
