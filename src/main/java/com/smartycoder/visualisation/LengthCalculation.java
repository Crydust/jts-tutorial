package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class LengthCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(150, 60),
                new Coordinate(200, 150),
                new Coordinate(80, 80),
                new Coordinate(40, 160),
                new Coordinate(50, 60),
        };
        LineString lineString = geometryFactory.createLineString(coordinates);
        double length = lineString.getLength();
        Coordinate[] polygonCoordinates = {
                new Coordinate(60, 250),
                new Coordinate(160, 270),
                new Coordinate(170, 330),
                new Coordinate(100, 350),
                new Coordinate(50, 280),
                new Coordinate(60, 250),
        };
        Polygon polygon = geometryFactory.createPolygon(polygonCoordinates);
        double polygonPerimeter = polygon.getLength();

        show(
                "JTS Visualisation - Length Calculation",
                new DrawLineString(lineString, Color.BLUE, null),
                new DrawPolygon(polygon, null, Color.BLUE, null),
                new DrawMultilineText("Calculated Length\n " + length, 70, 190, Color.WHITE),
                new DrawMultilineText("Calculated Perimeter\n " + polygonPerimeter, 70, 380, Color.WHITE)
        );
    }

}
