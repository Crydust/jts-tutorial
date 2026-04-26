package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class SymmetricDifferenceAreaCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(150, 60),
                new Coordinate(325, 180),
                new Coordinate(90, 150),
                new Coordinate(150, 60),
        };
        Coordinate[] coordinates2 = {
                new Coordinate(210, 120),
                new Coordinate(350, 120),
                new Coordinate(400, 210),
                new Coordinate(250, 300),
                new Coordinate(180, 150),
                new Coordinate(210, 120),
        };
        Polygon bluePolygon = geometryFactory.createPolygon(coordinates);
        Polygon yellowPolygon = geometryFactory.createPolygon(coordinates2);
        MultiPolygon symmetricDifferencePolygon = (MultiPolygon) yellowPolygon.symDifference(bluePolygon);
        DrawingCommand[] commands = new DrawingCommand[symmetricDifferencePolygon.getNumGeometries() + 1];
        for (int i = 0; i < symmetricDifferencePolygon.getNumGeometries(); i++) {
            Polygon poly = (Polygon) symmetricDifferencePolygon.getGeometryN(i);
            commands[i] = new DrawPolygon(poly, null, VisualisationUtil.colorWithAlpha(Color.ORANGE, 200), null);
        }
        commands[symmetricDifferencePolygon.getNumGeometries()] = new DrawMultilineText("The union of two differences of polygons from each other:\nSymmetric Difference", 60, 330, Color.WHITE);

        VisualisationUtil.show("JTS Visualisation - Symmetric Difference Area Calculation", commands);
    }

}
