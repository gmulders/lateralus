package io.lateralus.lexergenerator.core.parser.nodes;

import io.lateralus.lexergenerator.core.parser.Interval;
import io.lateralus.lexergenerator.core.parser.visitors.RegExNodeVisitor;
import io.lateralus.lexergenerator.core.parser.visitors.VisitingException;

import java.util.Arrays;
import java.util.List;

public class CharSetNode extends AbstractRegExNode {

	private List<Interval> intervalList;

	public CharSetNode(Interval interval) {
		this(Arrays.asList(interval));
	}

	public CharSetNode(List<Interval> intervalList) {
		this.intervalList = intervalList;
	}

	public List<Interval> getIntervalList() {
		return intervalList;
	}

	public void setIntervalList(List<Interval> intervalList) {
		this.intervalList = intervalList;
	}

	@Override
	public <R, X extends VisitingException> R accept(RegExNodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}
}
