package org.fao.unredd.adjuster;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Hello world!
 * 
 */
public class Adjuster {
    public static void main(String[] args) throws MalformedURLException,
            IOException {

        Reference reference = new Reference(
                "coberturas/2006/ch06_wgs84_gkf5.shp", 100);
        Adjusting adjusting = new Adjusting(
                "coberturas/2006_2011/testcase.shp", // ch_defo_otf_a_ot_2006_2011_f5wgs84_final.shp",
                "result.shp");

        while (!adjusting.eof()) {
            EditablePolygon geom = adjusting.next();
            OrderedEditableCoordinate firstCoordinate = geom
                    .getFirstCoordinate();
            OrderedEditableCoordinate coordinate = firstCoordinate;
            CoordinateInLine lastAdjusted = null;
            do {
                CoordinateInLine closest = reference
                        .getClosestPointInTolerance(coordinate);
                if (closest != null) {
                    coordinate.update(closest);
                    if (lastAdjusted != null) {
                        coordinate = coordinate.append(lastAdjusted
                                .getUntil(closest));
                    }
                    lastAdjusted = closest;
                } else {
                    lastAdjusted = null;
                }
                coordinate = coordinate.next();
            } while (coordinate != firstCoordinate);
            geom.save();
        }

        adjusting.write();
    }
}
