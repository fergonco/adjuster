package org.fao.unredd.adjuster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class Adjuster {
	public static final double TOLERANCE = 10;
	public static final double ANGLE_TOLERANCE = 0.5;

	public static void main(String[] args) throws MalformedURLException,
			IOException, InvalidGeometryException {

		String referencePath = "coberturas/test-25-nov/merge.shp";
		String adjustingPath = "coberturas/test-25-nov/tucuman/defo_otf_a_ot_2007_2011_f3wgs84_final.shp";
		String resultPath = "result.shp";

		adjust(referencePath, adjustingPath, resultPath);
	}

	public static void adjust(String referencePath, String adjustingPath,
			String resultPath) throws MalformedURLException, IOException {
		Reference reference = new Reference(referencePath);
		// Adjusting adjusting = new Adjusting(
		// "coberturas/2006_2011/single-testcase.shp", "single-result.shp");
		// Adjusting adjusting = new Adjusting(
		// "coberturas/2006_2011/testcase.shp", "result.shp");
		Adjusting adjusting = new Adjusting(adjustingPath, resultPath);

		int i = 0;
		while (!adjusting.eof()) {
			System.out.println("Processing geometry: " + i);
			i++;
			EditablePolygon geom;
			try {
				geom = adjusting.next();
			} catch (EmptyGeometryException e) {
				System.out.println("JUMPED!!!!");
				continue;
			}
			OrderedEditableCoordinate firstCoordinate = geom
					.getFirstCoordinate();
			OrderedEditableCoordinate coordinate = firstCoordinate;
			printPolygon(coordinate);
			ArrayList<AdjustedCoordinate> adjusted = new ArrayList<AdjustedCoordinate>();
			do {
				OrderedEditableCoordinate closest = reference
						.getClosestPointInTolerance(coordinate);
				// Save next because it may change
				OrderedEditableCoordinate nextCoordinate = coordinate.next();
				if (closest != null) {
					coordinate.update(closest.getCoordinate());
					adjusted.add(new AdjustedCoordinate(coordinate, closest));
					printPolygon(coordinate);
				}
				coordinate = nextCoordinate;
			} while (coordinate != firstCoordinate);

			System.out.println("Adding neighbours");
			for (AdjustedCoordinate adjustedCoordinate : adjusted) {
				adjustedCoordinate.appendNeighbours();
				printPolygon(coordinate);
			}
			geom.save();
		}

		adjusting.write();
	}

	private static void printPolygon(OrderedEditableCoordinate coordinate) {
		try {
			System.out.println(coordinate.buildPolygon());
		} catch (InvalidGeometryException e) {
			System.out.println("FAIL!!!");
			System.out.println(e.getPolygon());
		}
	}
}
