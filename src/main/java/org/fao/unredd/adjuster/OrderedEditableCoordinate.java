package org.fao.unredd.adjuster;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class OrderedEditableCoordinate {

	public enum Direction {
		NEXT, PREVIOUS
	}

	private OrderedEditableCoordinate previous;
	private OrderedEditableCoordinate next;
	private Coordinate coordinate;

	public OrderedEditableCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public OrderedEditableCoordinate next() {
		return next;
	}

	public OrderedEditableCoordinate previous() {
		return previous;
	}

	public void linkNext(OrderedEditableCoordinate next) {
		this.next = next;
	}

	public void linkPrevious(OrderedEditableCoordinate previous) {
		this.previous = previous;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public OrderedEditableCoordinate sibling(Direction direction) {
		if (direction == Direction.NEXT) {
			return next();
		} else {
			return previous();
		}
	}

	public void update(Coordinate closest) {
		Coordinate old = this.coordinate;
		this.coordinate = closest;
		try {
			buildPolygon();
		} catch (InvalidGeometryException e) {
			this.coordinate = old;
		}

	}

	public OrderedEditableCoordinate insert(Direction direction,
			Coordinate point) {
		OrderedEditableCoordinate previousToInsertion;
		if (direction == Direction.PREVIOUS) {
			previousToInsertion = previous();
		} else {
			previousToInsertion = this;
		}
		OrderedEditableCoordinate ret = new OrderedEditableCoordinate(point);
		OrderedEditableCoordinate formerNext = previousToInsertion.next();
		previousToInsertion.linkNext(ret);
		formerNext.linkPrevious(ret);
		ret.linkPrevious(previousToInsertion);
		ret.linkNext(formerNext);

		try {
			buildPolygon();
			return ret;
		} catch (InvalidGeometryException e) {
			previousToInsertion.linkNext(formerNext);
			formerNext.linkPrevious(previousToInsertion);
			return null;
		}
	}

	public Polygon buildPolygon() throws InvalidGeometryException {
		ArrayList<Coordinate> ring = new ArrayList<Coordinate>();
		OrderedEditableCoordinate currentCoordinate = this;
		do {
			ring.add(currentCoordinate.getCoordinate());
			currentCoordinate = currentCoordinate.next();
		} while (currentCoordinate != this);
		ring.add(ring.get(0));

		GeometryFactory gf = new GeometryFactory();
		LinearRing linearRing = gf.createLinearRing(ring
				.toArray(new Coordinate[0]));
		Polygon polygon = gf.createPolygon(linearRing);
		if (!polygon.isValid()) {
			throw new InvalidGeometryException(polygon);
		}
		return polygon;
	}

	public void appendNeighbors(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2, LineSegment otherSegmentToBeAdjusted,
			Direction insertionDirectionFromC1) {

		Direction direction = calculateDirection(c1, c2);
		if (direction != null) {
			appendNeighborsInDirection(c1, c2, otherSegmentToBeAdjusted,
					insertionDirectionFromC1, direction);
		}
	}

	private Direction calculateDirection(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2) {
		double xAxisAngle = new LineSegment(c1.getCoordinate(),
				c2.getCoordinate()).angle();
		double forwardAngle = new LineSegment(getCoordinate(), next()
				.getCoordinate()).angle();
		double backwardAngle = new LineSegment(getCoordinate(), previous()
				.getCoordinate()).angle();

		double forwardDistance = angleDistance(
				Math.min(xAxisAngle, forwardAngle),
				Math.max(xAxisAngle, forwardAngle));
		double backwardDistance = angleDistance(
				Math.min(xAxisAngle, backwardAngle),
				Math.max(xAxisAngle, backwardAngle));

		if (forwardDistance < backwardDistance
				&& forwardDistance < Adjuster.ANGLE_TOLERANCE) {
			return OrderedEditableCoordinate.Direction.NEXT;
		} else if (backwardDistance < forwardDistance
				&& backwardDistance < Adjuster.ANGLE_TOLERANCE) {
			return OrderedEditableCoordinate.Direction.PREVIOUS;
		} else {
			return null;
		}
	}

	private void appendNeighborsInDirection(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2, LineSegment otherSegmentToBeAdjusted,
			Direction insertionDirectionFromC1, Direction direction) {
		OrderedEditableCoordinate reference = this;
		OrderedEditableCoordinate segmentStart = c1;
		do {
			reference = reference.sibling(direction);
		} while ((segmentStart = insert(segmentStart, c2, reference,
				otherSegmentToBeAdjusted, insertionDirectionFromC1)) != null);
	}

	public static double angleDistance(double min, double max) {
		return Math.min(max - min, Math.PI * 2 + min - max);
	}

	private OrderedEditableCoordinate insert(OrderedEditableCoordinate c1,
			OrderedEditableCoordinate c2,
			OrderedEditableCoordinate referencePoint,
			LineSegment otherSegmentToBeAdjusted,
			Direction insertionDirectionFromC1) {

		if (repeated(c1, OrderedEditableCoordinate.Direction.PREVIOUS,
				referencePoint.getCoordinate())
				|| repeated(c2, OrderedEditableCoordinate.Direction.NEXT,
						referencePoint.getCoordinate())) {
			return null;
		}

		LineSegment segment = new LineSegment(c1.getCoordinate(),
				c2.getCoordinate());

		// Check the closest point is not on the extremes of the segment
		if (segment.closestPoint(referencePoint.getCoordinate()).distance(
				c1.getCoordinate()) < 0.0001
				|| segment.closestPoint(referencePoint.getCoordinate())
						.distance(c2.getCoordinate()) < 0.0001) {
			return null;
		}

		double distance = segment.distance(referencePoint.getCoordinate());

		// Check the other segment is further
		if (otherSegmentToBeAdjusted.distance(referencePoint.getCoordinate()) < distance) {
			return null;
		}

		if (distance < Adjuster.TOLERANCE) {
			return c1.insert(insertionDirectionFromC1,
					referencePoint.getCoordinate());
		} else {
			return null;
		}
	}

	private boolean repeated(OrderedEditableCoordinate reference,
			OrderedEditableCoordinate.Direction direction,
			Coordinate newCoordinate) {
		OrderedEditableCoordinate coordinate = reference;
		while (coordinate.getCoordinate().distance(reference.getCoordinate()) < Adjuster.TOLERANCE
				&& coordinate.next() != reference) {
			if (coordinate.getCoordinate().distance(newCoordinate) < 0.001) {
				return true;
			}

			coordinate = coordinate.sibling(direction);
		}

		return false;
	}

}
