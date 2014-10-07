package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * double linked list of coordinates with no duplicated points.
 * 
 * @author fergonco
 */
public class TopologicalPolygon {

	private OrderedEditableCoordinate firstCoordinate;

	public TopologicalPolygon(Geometry geometry) {
		Coordinate[] coordinates = geometry.getCoordinates();
		OrderedEditableCoordinate last = null;
		// We jump the repeated last coordinate
		for (int i = 0; i < coordinates.length - 1; i++) {
			Coordinate coordinate = coordinates[i];
			OrderedEditableCoordinate linkedCoordinate = new OrderedEditableCoordinate(
					coordinate);
			if (last == null) {
				firstCoordinate = linkedCoordinate;
				last = linkedCoordinate;
			} else {
				if (coordinate.distance(last.getCoordinate()) < 0.0001) {
					continue;
				}
				last.linkNext(linkedCoordinate);
				linkedCoordinate.linkPrevious(last);
				last = linkedCoordinate;
			}
		}
		last.linkNext(firstCoordinate);
		firstCoordinate.linkPrevious(last);
	}

	public OrderedEditableCoordinate getFirstCoordinate() {
		return firstCoordinate;
	}

	protected Polygon buildPolygon() {
		try {
			return firstCoordinate.buildPolygon();
		} catch (InvalidGeometryException e) {
			// Probably was already wrong at the begining
			return e.getPolygon();
		}
	}

}