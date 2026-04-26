package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPoint;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.awt.Color;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class IntersectsCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {new Coordinate(50, 50), new Coordinate(150, 150)};
        LineString line1 = geometryFactory.createLineString(coordinates);
        LineString line2 = geometryFactory.createLineString(new Coordinate[]{new Coordinate(80, 40), new Coordinate(30, 250)});
        boolean line1IntersectsLine2 = line1.intersects(line2);
        Point intersectionPoint = (Point) line1.intersection(line2);
        LineString line3 = geometryFactory.createLineString(new Coordinate[]{new Coordinate(210, 75), new Coordinate(350, 170)});
        boolean line3DisjointLine1 = line3.disjoint(line1);
        boolean line3IntersectsLine1 = line3.intersects(line1);

        VisualisationUtil.show(
                "JTS Visualisation - Intersects Calculation",
                new DrawLineString(line1, Color.WHITE, null),
                new DrawLineString(line2, Color.WHITE, null),
                new DrawLineString(line3, Color.WHITE, null),
                new DrawPoint(intersectionPoint, Color.RED, null),
                new DrawMultilineText("Line 1 and Line 2 intersects? " + line1IntersectsLine2 + "\n" +
                        "Line 3 and Line 1 intersects? " + line3IntersectsLine1 + "\n" +
                        "Line 3 and Line 1 disjoint? " + line3DisjointLine1, 60, 250, Color.WHITE)
        );
    }

}
