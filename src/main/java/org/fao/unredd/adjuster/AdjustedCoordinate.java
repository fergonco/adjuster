package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.LineSegment;

public class AdjustedCoordinate {

	private OrderedEditableCoordinate adjusted;
	private OrderedEditableCoordinate reference;

	public AdjustedCoordinate(OrderedEditableCoordinate adjusted,
			OrderedEditableCoordinate reference) {
		this.adjusted = adjusted;
		this.reference = reference;
	}

	public void appendNeighbours() {
		reference.appendNeighbors(adjusted, adjusted.previous(),
				new LineSegment(adjusted.getCoordinate(), adjusted.next()
						.getCoordinate()),
				OrderedEditableCoordinate.Direction.PREVIOUS);
		reference.appendNeighbors(adjusted, adjusted.next(), new LineSegment(
				adjusted.getCoordinate(), adjusted.previous().getCoordinate()),
				OrderedEditableCoordinate.Direction.NEXT);
	}
}
