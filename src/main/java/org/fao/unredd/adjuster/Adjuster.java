package org.fao.unredd.adjuster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class Adjuster {
	public static final double TOLERANCE = 100;
	public static final double ANGLE_TOLERANCE = 0.5;

	public static void main(String[] args) throws MalformedURLException,
			IOException, InvalidGeometryException {

		Reference reference = new Reference(
				"coberturas/2006/ch06_wgs84_gkf5.shp");
		// Adjusting adjusting = new Adjusting(
		// "coberturas/2006_2011/single-testcase.shp", "single-result.shp");
		// Adjusting adjusting = new Adjusting(
		// "coberturas/2006_2011/testcase.shp", "result.shp");
		Adjusting adjusting = new Adjusting(
				"coberturas/2006_2011/ch_defo_otf_a_ot_2006_2011_f5wgs84_final.shp",
				"result_100.shp");

		while (!adjusting.eof()) {
			EditablePolygon geom = adjusting.next();
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
