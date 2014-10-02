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

public class Reference {

    private int tolerance;
    private ArrayList<Geometry> geometries = new ArrayList<Geometry>();

    public Reference(String shapePath, int tolerance)
            throws MalformedURLException, IOException {
        this.tolerance = tolerance;

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        FileDataStore dataStore = dataStoreFactory.createDataStore(new File(
                shapePath).toURI().toURL());
        SimpleFeatureIterator features = dataStore.getFeatureSource()
                .getFeatures().features();
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            geometries.add(geometry);
        }
        features.close();
        dataStore.dispose();
    }

    public CoordinateInLine getClosestPointInTolerance(
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

        if (min < tolerance) {
            return closestPoint(coordinate, argMin);
        } else {
            return null;
        }
    }

    private CoordinateInLine closestPoint(OrderedEditableCoordinate coordinate,
            Geometry geometry) {
        double min = Double.MAX_VALUE;
        CoordinateInLine argMin = null;
        Coordinate[] ring = geometry.getCoordinates();
        for (int i = 0; i < ring.length - 1; i++) {
            LineSegment segment = new LineSegment(ring[i], ring[i + 1]);
            Coordinate point = segment.closestPoint(coordinate.getCoordinate());
            double distance = point.distance(coordinate.getCoordinate());
            if (distance < min) {
                min = distance;
                argMin = new CoordinateInLine(geometry, point, ring, i,
                        !ring[i].equals(point));
            }
        }

        if (argMin != null) {
            return argMin;
        } else {
            return null;
        }
    }

}
