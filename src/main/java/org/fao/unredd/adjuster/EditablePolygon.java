package org.fao.unredd.adjuster;

import java.io.IOException;
import java.util.ArrayList;

import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class EditablePolygon {

	private OrderedEditableCoordinate firstCoordinate;
	private SimpleFeature feature;
	private FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter;

	public EditablePolygon(SimpleFeature feature,
			FeatureWriter<SimpleFeatureType, SimpleFeature> outputFeatureWriter) {
		this.feature = feature;
		this.featureWriter = outputFeatureWriter;
		Coordinate[] coordinates = ((Geometry) feature.getDefaultGeometry())
				.getCoordinates();
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

	public void save() throws IOException {
		SimpleFeature feature = featureWriter.next();
		feature.setAttributes(this.feature.getAttributes());
		feature.setDefaultGeometry(buildPolygon());
		featureWriter.write();
	}

	private Object buildPolygon() {
		ArrayList<Coordinate> ring = new ArrayList<Coordinate>();
		OrderedEditableCoordinate currentCoordinate = firstCoordinate;
		do {
			ring.add(currentCoordinate.getCoordinate());
			currentCoordinate = currentCoordinate.next();
		} while (currentCoordinate != firstCoordinate);
		ring.add(ring.get(0));

		GeometryFactory gf = new GeometryFactory();
		LinearRing linearRing = gf.createLinearRing(ring
				.toArray(new Coordinate[0]));
		return gf.createPolygon(linearRing);
	}
}
