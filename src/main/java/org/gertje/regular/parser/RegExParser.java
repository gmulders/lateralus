package org.gertje.regular.parser;

import org.gertje.regular.parser.nodes.AbstractRegExNode;
import org.gertje.regular.parser.nodes.CharSetNode;
import org.gertje.regular.parser.nodes.ConcatNode;
import org.gertje.regular.parser.nodes.OptionalNode;
import org.gertje.regular.parser.nodes.PlusNode;
import org.gertje.regular.parser.nodes.StarNode;
import org.gertje.regular.parser.nodes.UnionNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RegExParser {

	private static final int MIN_UNICODE = 0;
	private static final int MAX_UNICODE = 0x10ffff;

	private String input;
	private int index;

	public RegExParser(String input) {
		this.input = input;
	}

	public AbstractRegExNode parse () throws RegExException {
		return expression();
	}

	private int peek() throws RegExException {
		if (!hasMore()) {
			throw new RegExException("End of input reached.");
		}

		return input.codePointAt(index);
	}

	private void expect(int codePoint) throws RegExException {
		if (!hasMore() || next() != codePoint) {
			throw new RegExException("Unexpected code point, expected '" + Character.getName(codePoint) + "' at "
					+ index + ".");
		}
	}

	private int next() throws RegExException {
		if (!hasMore()) {
			throw new RegExException("End of input reached.");
		}

		int codePoint = input.codePointAt(index);
		index += Character.charCount(codePoint);
		return codePoint;
	}

	private boolean hasMore() {
		return index < input.length();
	}

	private AbstractRegExNode expression() throws RegExException {
		// expression = term
		//              term | expression

		AbstractRegExNode lhs = term();

		if (!hasMore() || peek() != '|') {
			return lhs;
		}

		expect('|');
		AbstractRegExNode rhs = expression();

		return new UnionNode(lhs, rhs);
	}

	private AbstractRegExNode term() throws RegExException {
		// term = factor
		//        factor term

		AbstractRegExNode lhs = factor();

		if (!hasMore() || peek() == '|' || peek() == ')') {
			return lhs;
		}

		AbstractRegExNode rhs = term();

		return new ConcatNode(lhs, rhs);
	}

	private AbstractRegExNode factor() throws RegExException {
		// factor = atom
		//          atom metacharacter

		AbstractRegExNode argument = atom();

		if (!hasMore()) {
			return argument;
		}

		switch (peek()) {
			case '*':
				next();
				return new StarNode(argument);
			case '+':
				next();
				return new PlusNode(argument);
			case '?':
				next();
				return new OptionalNode(argument);
			default:
				return argument;
		}
	}

	private AbstractRegExNode atom() throws RegExException {
		// atom = .
		//        ( expression )
		//        [ characterclass ]
		//        [ ^ characterclass ]
		//        character

		int i = peek();

		switch (i) {
			case '.':
				next();
				return new CharSetNode(new Interval(MIN_UNICODE, MAX_UNICODE));
			case '(':
				next();
				AbstractRegExNode expression = expression();
				expect(')');
				return expression;
			case '[':
				next();
				boolean negate = false;
				if (peek() == '^') {
					next();
					negate = true;
				}
				List<Interval> intervalList = characterClass();
				expect(']');

				// Merge overlapping ranges.
				intervalList = merge(intervalList);

				// Negate the ranges if needed.
				if (negate) {
					intervalList = negate(intervalList);
				}

				return new CharSetNode(intervalList);
			default:
				return new CharSetNode(new Interval(character()));
		}
	}

	private List<Interval> characterClass() throws RegExException {
		// characterclass = epsilon
		//                  characterrange
		//                  characterrange characterclass

		List<Interval> intervalList = new ArrayList<>();

		while (peek() != ']') {
			intervalList.add(characterRange());
		}

		return intervalList;
	}

	private Interval characterRange() throws RegExException {
		// characterrange = begincharacter
		//                  begincharacter - endcharacter

		int start = beginCharacter();
		int end = start;

		if (peek() == '-') {
			next();
			end = endCharacter();
		}

		if (start <= end) {
			return new Interval(start, end);
		} else {
			return new Interval(end, start);
		}
	}

	private int beginCharacter() throws RegExException {
		// begincharacter = character
		return character();
	}

	private int endCharacter() throws RegExException {
		// endcharacter = character
		return character();
	}

	private int character() throws RegExException {
		// character = anycharacterexceptmetacharacters
		//             \ anycharacterexceptspecialcharacters
		int i = next();

		if (i != '\\') {
			return i;
		}

		i = next();

		switch (i) {
			case 't':
				return '\t';
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 'U':
				return codePoint();
			default:
				return i;
		}
	}

	private int codePoint() throws RegExException {
		// codepoint = hexnumber
		int codePoint = hexNumber();

		// The code point should not be bigger then MAX_UNICODE. From our definition, it follows that it cannot be
		// smaller then MIN_UNICODE.
		return Math.min(codePoint, MAX_UNICODE);
	}

	private int hexNumber() throws RegExException {
		// hexnumber = hexdigit
		//           = hexdigit hexnumber

		int number = 0;

		for (int i = 0; i < 4; i++) {
			number = number << 4;
			number += hexDigit();
		}

		return number;
	}

	private int hexDigit() throws RegExException {
		// hexdigit = 0
		//            1
		//            ...
		//            F

		int i = next();

		if (i >= '0' && i <= '9') {
			return i - '0';
		} else if (i >= 'a' && i <= 'f') {
			return i - 'a' + 10;
		} else if (i >= 'A' && i <= 'F') {
			return i - 'A' + 10;
		} else {
			throw new RegExException("Expected hexadecimal digit.");
		}
	}

	private List<Interval> merge(List<Interval> intervalList) {
		List<Interval> newIntervalList = new ArrayList<>();
		if(intervalList == null || intervalList.isEmpty()) {
			return newIntervalList;
		}

		// Sort the array according to start values.
		intervalList.sort(Comparator.comparingInt(Interval::getStart));

		Interval interval = intervalList.get(0);
		for (int i = 1; i < intervalList.size(); i++) {
			Interval currentInterval = intervalList.get(i);
			if (interval.getEnd() < currentInterval.getStart()) {
				newIntervalList.add(interval);
				interval = currentInterval;
			} else {
				interval.setEnd(Math.max(interval.getEnd(), currentInterval.getEnd()));
			}
		}
		newIntervalList.add(interval);
		return newIntervalList;
	}

	private List<Interval> negate(List<Interval> intervalList) {
		List<Interval> newIntervalList = new ArrayList<>();
		if(intervalList == null || intervalList.isEmpty()) {
			newIntervalList.add(new Interval(MIN_UNICODE, MAX_UNICODE));
			return newIntervalList;
		}

		Interval interval = intervalList.get(0);
		if (interval.getStart() == MIN_UNICODE) {
			if (interval.getEnd() == MAX_UNICODE) {
				return newIntervalList;
			}
		} else {
			newIntervalList.add(new Interval(MIN_UNICODE, interval.getStart() - 1));
		}

		for (int i = 1; i < intervalList.size(); i++) {
			Interval currentInterval = intervalList.get(i);
			newIntervalList.add(new Interval(interval.getEnd() + 1, currentInterval.getStart() - 1));
			interval = currentInterval;
		}

		if (interval.getEnd() < MAX_UNICODE) {
			newIntervalList.add(new Interval(interval.getEnd() + 1, MAX_UNICODE));
		}

		return newIntervalList;
	}
}
