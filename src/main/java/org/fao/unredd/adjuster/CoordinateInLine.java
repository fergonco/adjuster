package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class CoordinateInLine {

	private Coordinate point;
	private Coordinate[] ring;
	private int index;
	private boolean after;

	public CoordinateInLine(Coordinate point, Coordinate[] ring, int index,
			boolean after) {
		this.point = point;
		this.ring = ring;
		this.index = index;
		this.after = after;
	}

	public Coordinate getCoordinate() {
		return point;
	}

	public void appendNeighbors(OrderedEditableCoordinate coordinate) {

		// forwards
		int i = index;
		do {
			i++;
			if (i >= ring.length) {
				i = 1;
			}
		} while (insertOnOneSegment(coordinate, i));

		// backwards
		if (after) {
			i = index + 1;
		} else {
			i = index;
		}
		do {
			i--;
			if (i < 0) {
				i = ring.length - 2;
			}
		} while (insertOnOneSegment(coordinate, i));
	}

	private boolean insertOnOneSegment(OrderedEditableCoordinate coordinate,
			int i) {
		boolean insertion = insert(coordinate.previous(), coordinate, ring[i]);
		insertion = insert(coordinate, coordinate.next(), ring[i]) || insertion;

		return insertion;
	}

	private boolean insert(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2, Coordinate point) {

		if (repeated(c1, OrderedEditableCoordinate.Direction.PREVIOUS, point)
				|| repeated(c2, OrderedEditableCoordinate.Direction.NEXT, point)) {
			return false;
		}

		LineSegment segment = new LineSegment(c1.getCoordinate(),
				c2.getCoordinate());

		double distance = segment.distance(point);
		if (distance < Adjuster.TOLERANCE) {
			OrderedEditableCoordinate newCoordinate = new OrderedEditableCoordinate(
					point);
			newCoordinate.linkPrevious(c1);
			newCoordinate.linkNext(c2);
			c1.linkNext(newCoordinate);
			c2.linkPrevious(newCoordinate);
			return true;
		} else {
			return false;
		}
	}

	private boolean repeated(OrderedEditableCoordinate reference,
			OrderedEditableCoordinate.Direction direction,
			Coordinate newCoordinate) {
		OrderedEditableCoordinate coordinate = reference;
		while (coordinate.getCoordinate().distance(reference.getCoordinate()) < Adjuster.TOLERANCE) {
			if (coordinate.getCoordinate().distance(newCoordinate) < 0.001) {
				return true;
			}

			coordinate = coordinate.sibling(direction);
		}

		return false;
	}

}
