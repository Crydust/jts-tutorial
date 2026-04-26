package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class ContainsCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(250, 60),
                new Coordinate(300, 150),
                new Coordinate(190, 150),
                new Coordinate(250, 60),
        };
        Polygon polygon = geometryFactory.createPolygon(coordinates);
        Point point = geometryFactory.createPoint(new Coordinate(100, 100));
        Point point2 = geometryFactory.createPoint(new Coordinate(250, 85));
        boolean polygonContainsPoint1 = polygon.contains(point);
        boolean polygonContainsPoint2 = polygon.contains(point2);
        boolean point1WithinPolygon = point.within(polygon);
        boolean point2WithinPolygon = point2.within(polygon);

        VisualisationUtil.show(
                "JTS Visualisation - Contains Calculation",
                new DrawPolygon(polygon, null, Color.BLUE, null),
                new DrawPoint(point, Color.WHITE, null),
                new DrawPoint(point2, Color.YELLOW, null),
                new DrawMultilineText("Polygon contains point 1? " + polygonContainsPoint1 + "\nPoint 1 within polygon? " + point1WithinPolygon, 30, 40, Color.WHITE),
                new DrawMultilineText("Polygon contains point 2? " + polygonContainsPoint2 + "\nPoint 2 within polygon? " + point2WithinPolygon, 200, 200, Color.WHITE)
        );
    }

}
