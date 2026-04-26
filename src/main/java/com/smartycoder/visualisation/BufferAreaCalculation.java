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
public class BufferAreaCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] bluePolygonCoordinates = {
                new Coordinate(150, 60),
                new Coordinate(325, 180),
                new Coordinate(90, 150),
                new Coordinate(150, 60),
        };
        Coordinate[] yellowPolygonCoordinates = {
                new Coordinate(210, 120),
                new Coordinate(350, 120),
                new Coordinate(400, 210),
                new Coordinate(250, 300),
                new Coordinate(180, 150),
                new Coordinate(210, 120),
        };
        Polygon bluePolygon = geometryFactory.createPolygon(bluePolygonCoordinates);
        Polygon yellowPolygon = geometryFactory.createPolygon(yellowPolygonCoordinates);
        Polygon bufferedYellowPolygon = (Polygon) yellowPolygon.buffer(30);
        Polygon bufferedBluePolygon = (Polygon) bluePolygon.buffer(30);

        show(
                "JTS Visualisation - Buffer Area Calculation",
                new DrawPolygon(bufferedYellowPolygon, null, colorWithAlpha(Color.CYAN, 220), null),
                new DrawPolygon(yellowPolygon, null, colorWithAlpha(Color.YELLOW, 170), Color.WHITE),
                new DrawPolygon(bufferedBluePolygon, null, colorWithAlpha(Color.RED, 130), null),
                new DrawPolygon(bluePolygon, null, colorWithAlpha(Color.BLUE, 160), Color.WHITE),
                new DrawMultilineText("30 unit buffered polygons of yellow and blue polygons", 60, 350, Color.WHITE)
        );
    }

}
