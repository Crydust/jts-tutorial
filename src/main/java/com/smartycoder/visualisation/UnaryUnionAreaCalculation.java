package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.colorWithAlpha;
import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class UnaryUnionAreaCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {new Coordinate(150, 60),
                new Coordinate(325, 180), new Coordinate(90, 150), new Coordinate(150, 60)};
        Coordinate[] coordinates2 = {new Coordinate(210, 120), new Coordinate(350, 120),
                new Coordinate(400, 210), new Coordinate(250, 300), new Coordinate(180, 150), new Coordinate(210, 120)};
        Polygon bluePolygon = geometryFactory.createPolygon(coordinates);
        Polygon yellowPolygon = geometryFactory.createPolygon(coordinates2);
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[]{yellowPolygon, bluePolygon});
        Polygon unionPolygon = (Polygon) multiPolygon.union();

        show(
                "JTS Visualisation - Unary Union Area Calculation",
                new DrawPolygon(unionPolygon, null, colorWithAlpha(Color.CYAN, 220), null),
                new DrawMultilineText("Unary union operation on MultiPolygon:\nUnary union area.", 60, 330, Color.WHITE)
        );
    }

}
