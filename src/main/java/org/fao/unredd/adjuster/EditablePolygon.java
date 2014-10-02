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
        for (Coordinate coordinate : coordinates) {
            OrderedEditableCoordinate linkedCoordinate = new OrderedEditableCoordinate(
                    coordinate);
            if (last == null) {
                firstCoordinate = linkedCoordinate;
                last = linkedCoordinate;
            } else {
                last.linkNext(linkedCoordinate);
                last = linkedCoordinate;
            }
        }
        last.linkNext(firstCoordinate);
    }

    public OrderedEditableCoordinate getFirstCoordinate() {
        return firstCoordinate;
    }

    public void save() throws IOException {
        SimpleFeature boh = featureWriter.next();
        boh.setAttributes(feature.getAttributes());
        boh.setDefaultGeometry(buildPolygon());
    }

    private Object buildPolygon() {
        ArrayList<Coordinate> ring = new ArrayList<Coordinate>();
        OrderedEditableCoordinate currentCoordinate = firstCoordinate;
        do {
            ring.add(currentCoordinate.getCoordinate());
            currentCoordinate = currentCoordinate.next();
        } while (currentCoordinate != firstCoordinate);

        GeometryFactory gf = new GeometryFactory();
        LinearRing linearRing = gf.createLinearRing(ring
                .toArray(new Coordinate[0]));
        return gf.createPolygon(linearRing);
    }
}
