package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultilineText;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class GeometrySimplification {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(50, 20),
                new Coordinate(70, 70),
                new Coordinate(90, 50),
                new Coordinate(110, 80),
                new Coordinate(120, 60),
                new Coordinate(150, 60),
                new Coordinate(175, 120),
                new Coordinate(200, 150),
                new Coordinate(220, 100),
                new Coordinate(230, 130),
                new Coordinate(260, 80),
                new Coordinate(310, 110),
        };
        LineString lineString = geometryFactory.createLineString(coordinates);
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(lineString);
        simplifier.setDistanceTolerance(30);
        LineString simplerLineString = (LineString) simplifier.getResultGeometry();
        System.out.println("original line coordinate size : " + coordinates.length);
        System.out.println("simplified line coordinate size " + simplerLineString.getCoordinates().length);

        show(
                "JTS Visualisation - Douglas Peucker Geometry Simplification",
                new DrawLineString(lineString, Color.BLUE, null),
                new DrawLineString(simplerLineString, Color.RED, null),
                new DrawMultilineText("original line coordinate size " + lineString.getNumPoints() + "\nsimplified line coordinate size " + simplerLineString.getNumPoints(), 60, 300, Color.WHITE)
        );
    }

}
