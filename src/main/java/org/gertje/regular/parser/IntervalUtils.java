package org.gertje.regular.parser;

import java.util.Arrays;

public class IntervalUtils {

	/**
	 * Splits intervals in a list of intervals into smaller non overlapping intervals such that every original interval
	 * can be created from a set of new intervals.
	 * <p>
	 * e.g. given the following intervals: [5, 70], [20, 30], [15, 50], [50, 60], [80, 90] <br/>
	 * the method will return the following intervals: [5, 14], [15, 19], [20, 30], [31, 49], [50, 50], [51, 60],
	 * [61, 70], [80, 90]
	 * <p>
	 * The argument and return arrays (must) have the following structure:
	 * <ul><li>the ints at the even indices are the start points,
	 * </li><li>the ints at the odd indices are the end points.
	 * </li></ul>
	 *
	 * @param intervalArray The array with intervals to be split
	 * @return An array that holds the new intervals
	 */
	public static int[] splitIntervals(int[] intervalArray) {
		// To encode the difference between the start and the end of an interval, we multiply the integer by 2 and add 1
		// if it is the end of an interval (thus when the index is odd).
		for (int i = 0; i < intervalArray.length; i++) {
			intervalArray[i] = (intervalArray[i] << 1) + (i & 1);
		}

		// Sort the array.
		Arrays.sort(intervalArray);

		// The maximal number of splitted intervals is 2n-1.
		// Create an array that can hold that much intervals.
		int n = intervalArray.length >> 1;
		int[] newIntervalArray = new int[2*(2*n-1)];

		// The current interval (overlapping) depth
		int depth = 0;

		// The index.
		int i = 0;

		// The (hypothetical) previous interval ended one lower then the start of the first interval.
		int prevEnd = (intervalArray[0] >> 1) - 1;
		int prevStart = 0;

		// For every int in the set, we perform one or more actions.
		for (int item : intervalArray) {
//			System.out.println(item + " - " + (item >> 1));

			int point = item >> 1;
			if ((item & 1) == 0) {
				// This item represents the start of an interval.

				if ((i & 1) == 1 && prevStart == point) {
					// There is already an interval with this starting point.
					// Update the depth.
					depth++;
					// Continue with the next item
					continue;

				} else if ((i & 1) == 1) {
					// We need to fill the end of the previous interval first.
					newIntervalArray[i++] = point - 1;

				} else {
					// We can start the new interval, but need to do some checks first.

					// Check that it leaves no gaps.
					if (depth > 0 && prevEnd + 1 != point) {
						// Add the missing interval.
						newIntervalArray[i] = newIntervalArray[i - 1] + 1;
						i++;
						newIntervalArray[i++] = point - 1;
					}
				}

				// Start the interval
				newIntervalArray[i++] = point;
				prevStart = point;
				depth++;

			} else {
				// This item represents the end of an interval.

				if (prevEnd == point) {
					// There is already an interval with this ending point.
					// Update the depth.
					depth--;
					// Continue with the next item
					continue;
				}

				if ((i & 1) == 0) {
					// There is no open interval. Open the interval first.
					newIntervalArray[i++] = prevEnd + 1;
				}

				// End the interval.
				newIntervalArray[i++] = point;
				prevEnd = point;
				depth--;
			}
		}

		// Return an array that has the exact length.
		return Arrays.copyOf(newIntervalArray, i);
	}


	public static int[] findSubIntervals(int start, int end, int[] intervalArray) {
		int[] subIntervalArray = new int[intervalArray.length / 2];

		int i = 0;

		// Find the index of the start of the interval. In the case that the start and end of an interval are the same
		// number, the binary search method might find the index of the end of that interval instead the start index. To
		// fix this we set the lowest bit to 0 (since the start index is always even).
		int subIntervalStartIndex = Arrays.binarySearch(intervalArray, start) & ~1;

		subIntervalArray[i++] = subIntervalStartIndex >> 1;

		while (intervalArray[subIntervalStartIndex + 1] < end) {
			subIntervalStartIndex += 2;
			subIntervalArray[i++] = subIntervalStartIndex >> 1;
		}
		return Arrays.copyOf(subIntervalArray, i);
	}
}
