package org.fao.unredd.adjuster;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Reference {

	private ArrayList<LineString> geometries = new ArrayList<LineString>();

	public Reference(String shapePath) throws MalformedURLException,
			IOException {

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		FileDataStore dataStore = dataStoreFactory.createDataStore(new File(
				shapePath).toURI().toURL());
		SimpleFeatureIterator features = dataStore.getFeatureSource()
				.getFeatures().features();
		while (features.hasNext()) {
			SimpleFeature feature = features.next();
			MultiPolygon geometry = (MultiPolygon) feature.getDefaultGeometry();
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Polygon polygon = (Polygon) geometry.getGeometryN(i);
				geometries.add(polygon.getExteriorRing());
			}
		}
		features.close();
		dataStore.dispose();
	}

	public OrderedEditableCoordinate getClosestPointInTolerance(
			OrderedEditableCoordinate coordinate) {
		Geometry argMin = null;
		double min = Double.MAX_VALUE;
		for (Geometry geometry : geometries) {
			double distance = geometry.distance(geometry.getFactory()
					.createPoint(coordinate.getCoordinate()));
			if (distance < min
					|| (Math.abs(distance - min) < 0.0001 && geometry
							.getNumPoints() < argMin.getNumPoints())) {
				argMin = geometry;
				min = distance;
			}
		}

		if (min < Adjuster.TOLERANCE) {
			return closestPoint(coordinate, argMin);
		} else {
			return null;
		}
	}

	private OrderedEditableCoordinate closestPoint(
			OrderedEditableCoordinate coordinate, Geometry geometry) {
		TopologicalPolygon polygon = new TopologicalPolygon(geometry);
		double min = Double.MAX_VALUE;
		Coordinate coordinateArgMin = null;
		OrderedEditableCoordinate topologicalArgMin = null;
		OrderedEditableCoordinate currentCoordinate = polygon
				.getFirstCoordinate();
		do {
			LineSegment segment = new LineSegment(
					currentCoordinate.getCoordinate(), currentCoordinate.next()
							.getCoordinate());
			Coordinate point = segment.closestPoint(coordinate.getCoordinate());
			double distance = point.distance(coordinate.getCoordinate());
			if (distance < min) {
				min = distance;
				topologicalArgMin = currentCoordinate;
				coordinateArgMin = point;
			}

			currentCoordinate = currentCoordinate.next();
		} while (currentCoordinate != polygon.getFirstCoordinate());

		if (coordinateArgMin != null) {
			if (topologicalArgMin.getCoordinate().distance(coordinateArgMin) < 0.0001) {
				return topologicalArgMin;
			} else if (topologicalArgMin.next().getCoordinate()
					.distance(coordinateArgMin) < 0.0001) {
				return topologicalArgMin.next();
			} else {
				return topologicalArgMin.insert(
						OrderedEditableCoordinate.Direction.NEXT,
						coordinateArgMin);
			}
		} else {
			return null;
		}
	}

}
