package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.Coordinate;

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

    public void update(CoordinateInLine closest) {
        this.coordinate = closest.getCoordinate();
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

}
