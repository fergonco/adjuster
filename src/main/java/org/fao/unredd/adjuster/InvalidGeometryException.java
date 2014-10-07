package org.fao.unredd.adjuster;

import com.vividsolutions.jts.geom.Polygon;

public class InvalidGeometryException extends Exception {
	private static final long serialVersionUID = 1L;

	private Polygon polygon;

	public InvalidGeometryException(Polygon polygon) {
		this.polygon = polygon;
	}

	public Polygon getPolygon() {
		return polygon;
	}

}
