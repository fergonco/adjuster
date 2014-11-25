package org.fao.unredd.adjuster;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class EditablePolygon extends TopologicalPolygon {

	private SimpleFeature feature;
	private FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter;

	public EditablePolygon(SimpleFeature feature,
			FeatureWriter<SimpleFeatureType, SimpleFeature> outputFeatureWriter)
			throws EmptyGeometryException {
		super((Geometry) feature.getDefaultGeometry());
		this.feature = feature;
		this.featureWriter = outputFeatureWriter;
	}

	public void save() throws IOException {
		SimpleFeature feature = featureWriter.next();
		feature.setAttributes(this.feature.getAttributes());
		feature.setDefaultGeometry(buildPolygon());
		featureWriter.write();
	}
}
