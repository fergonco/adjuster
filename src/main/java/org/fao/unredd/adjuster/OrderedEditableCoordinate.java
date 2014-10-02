package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.Coordinate;

public class OrderedEditableCoordinate {

    private OrderedEditableCoordinate next;
    private Coordinate coordinate;

    public OrderedEditableCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void update(CoordinateInLine closest) {
        this.coordinate = closest.getCoordinate();
    }

    public OrderedEditableCoordinate append(Coordinate... coordinates) {
        OrderedEditableCoordinate last = this;
        OrderedEditableCoordinate appendNext = this.next();
        for (Coordinate coordinate : coordinates) {
            OrderedEditableCoordinate linkedCoordinate = new OrderedEditableCoordinate(
                    coordinate);
            last.linkNext(linkedCoordinate);
            last = linkedCoordinate;
        }
        last.linkNext(appendNext);
        return last;
    }

    public OrderedEditableCoordinate next() {
        return next;
    }

    public void linkNext(OrderedEditableCoordinate next) {
        this.next = next;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

}
