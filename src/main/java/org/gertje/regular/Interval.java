package org.gertje.regular;

public class Interval {

	private int start;
	private int end;

	public Interval(int start) {
		this(start, start);
	}

	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
}
