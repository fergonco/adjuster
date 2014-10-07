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

	public void appendNeighbors(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2) {
		OrderedEditableCoordinate segmentStart = c1;

		if (forward(c1, c2)) {
			// forwards
			int i = index;
			do {
				i = nextIndex(i);
			} while ((segmentStart = insert(segmentStart, c2, ring[i])) != null);
		} else {
			// backwards
			int i;
			if (after) {
				i = index + 1;
			} else {
				i = index;
			}
			do {
				i = previousIndex(i);
			} while ((segmentStart = insert(segmentStart, c2, ring[i])) != null);
		}
	}

	private boolean forward(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2) {
		double xAxisAngle = new LineSegment(c1.getCoordinate(),
				c2.getCoordinate()).angle();
		double forwardAngle = new LineSegment(ring[index],
				ring[nextIndex(index)]).angle();
		double backwardAngle = new LineSegment(ring[index],
				ring[previousIndex(index)]).angle();

		double forwardDistance = angleDistance(
				Math.min(xAxisAngle, forwardAngle),
				Math.max(xAxisAngle, forwardAngle));
		double backwardDistance = angleDistance(
				Math.min(xAxisAngle, backwardAngle),
				Math.max(xAxisAngle, backwardAngle));
		return forwardDistance < backwardDistance;
	}

	public static double angleDistance(double min, double max) {
		return Math.min(max - min, Math.PI * 2 + min - max);
	}

	private int previousIndex(int i) {
		i--;
		if (i < 0) {
			i = ring.length - 2;
		}
		return i;
	}

	private int nextIndex(int i) {
		i++;
		if (i >= ring.length) {
			i = 1;
		}
		return i;
	}

	private OrderedEditableCoordinate insert(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2, Coordinate point) {

		if (repeated(c1, OrderedEditableCoordinate.Direction.PREVIOUS, point)
				|| repeated(c2, OrderedEditableCoordinate.Direction.NEXT, point)) {
			return null;
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
			return newCoordinate;
		} else {
			return null;
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
