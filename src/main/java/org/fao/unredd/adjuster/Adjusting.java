package org.fao.unredd.adjuster;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class Adjusting {

    private FileDataStore dataStore;
    private SimpleFeatureIterator features;
    private DataStore outputDataStore;
    private FeatureWriter<SimpleFeatureType, SimpleFeature> outputFeatureWriter;
    private DefaultTransaction transaction;

    public Adjusting(String shapePath, String resultPath)
            throws MalformedURLException, IOException {
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        dataStore = dataStoreFactory.createDataStore(new File(shapePath)
                .toURI().toURL());
        features = dataStore.getFeatureSource().getFeatures().features();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", new File(resultPath).toURI().toURL());
        outputDataStore = dataStoreFactory.createNewDataStore(params);
        SimpleFeatureType readSchema = dataStore.getSchema(dataStore
                .getTypeNames()[0]);
        outputDataStore.createSchema(readSchema);
        transaction = new DefaultTransaction();
        outputFeatureWriter = outputDataStore.getFeatureWriter(
                outputDataStore.getTypeNames()[0], transaction);
    }

    public boolean eof() {
        return !features.hasNext();
    }

    public EditablePolygon next() {
        SimpleFeature feature = features.next();
        return new EditablePolygon(feature, outputFeatureWriter);
    }

    public void write() throws IOException {
        transaction.commit();
        dataStore.dispose();
        outputDataStore.dispose();
    }

}
