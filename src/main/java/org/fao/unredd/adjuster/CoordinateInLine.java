package org.fao.unredd.adjuster;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class CoordinateInLine {

    private Coordinate point;
    private Coordinate[] ring;
    private int index;
    private boolean after;
    private Geometry geometry;

    public CoordinateInLine(Geometry geometry, Coordinate point,
            Coordinate[] ring, int index, boolean after) {
        this.geometry = geometry;
        this.point = point;
        this.ring = ring;
        this.index = index;
        this.after = after;
    }

    public Coordinate[] getUntil(CoordinateInLine coordinate) {
        if (this.geometry != coordinate.geometry) {
            return new Coordinate[0];
        }
        ArrayList<Coordinate> oneway = new ArrayList<Coordinate>();
        int i = index;
        do {
            i++;
            if (i >= ring.length) {
                i = 0;
            }
            oneway.add(ring[i]);
        } while (i != coordinate.index);

        ArrayList<Coordinate> otherway = new ArrayList<Coordinate>();
        if (after) {
            i = index + 1;
        } else {
            i = index;
        }
        do {
            i--;
            if (i < 0) {
                i = ring.length - 1;
            }
            otherway.add(ring[i]);

        } while (i != coordinate.index);

        if (oneway.size() < otherway.size()) {
            return oneway.toArray(new Coordinate[0]);
        } else {
            return otherway.toArray(new Coordinate[0]);
        }
    }

    public Coordinate getCoordinate() {
        return point;
    }

}
