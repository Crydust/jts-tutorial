package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.colorWithAlpha;
import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class IntersectionAreaCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] blueCoordinates = {
                new Coordinate(150, 60),
                new Coordinate(325, 180),
                new Coordinate(90, 150),
                new Coordinate(150, 60),
        };
        Coordinate[] yellowCoordinates = {
                new Coordinate(210, 120),
                new Coordinate(350, 120),
                new Coordinate(400, 210),
                new Coordinate(250, 300),
                new Coordinate(180, 150),
                new Coordinate(210, 120),
        };
        Polygon bluePolygon = geometryFactory.createPolygon(blueCoordinates);
        Polygon yellowPolygon = geometryFactory.createPolygon(yellowCoordinates);
        Polygon intersectionArea = (Polygon) yellowPolygon.intersection(bluePolygon);

        show(
                "JTS Visualisation - Intersection Area Calculation",
                new DrawPolygon(bluePolygon, null, colorWithAlpha(Color.BLUE, 127), null),
                new DrawPolygon(yellowPolygon, null, colorWithAlpha(Color.YELLOW, 127), null),
                new DrawPolygon(intersectionArea, null, colorWithAlpha(Color.RED, 127), null),
                new DrawMultilineText("The intersection area of Blue and Yellow polygons\nis painted in RED color.", 60, 330, Color.WHITE)
        );

    }

}
