package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * double linked list of coordinates with no duplicated points. the input
 * geometry is expected to be a LinearRing, a polygon with only one ring or a
 * multipolygon of one polygon with only one ring.
 * 
 * @author fergonco
 */
public class TopologicalPolygon {

	private OrderedEditableCoordinate firstCoordinate;

	public TopologicalPolygon(Geometry geometry) throws EmptyGeometryException {
		if (geometry.isEmpty()) {
			throw new EmptyGeometryException();
		}
		// Get the external ring of the unique polygon
		if (geometry instanceof MultiPolygon) {
			geometry = geometry.getGeometryN(0);
		}
		if (geometry instanceof Polygon) {
			geometry = ((Polygon) geometry).getExteriorRing();
		}

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
			// Probably was already wrong at the beginning
			return e.getPolygon();
		}
	}

}
