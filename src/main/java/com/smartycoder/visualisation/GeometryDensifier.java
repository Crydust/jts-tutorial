package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;

import java.awt.Color;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class GeometryDensifier {

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
        Densifier densifier = new Densifier(lineString);
        densifier.setDistanceTolerance(15);
        LineString denserLineString = (LineString) densifier.getResultGeometry();
        System.out.println("original line coordinate size : " + coordinates.length);
        System.out.println("densified line coordinate size " + denserLineString.getCoordinates().length);
        MultiPoint originalPoints = geometryFactory.createMultiPointFromCoords(coordinates);
        Coordinate[] denserCoords = denserLineString.getCoordinates();
        Coordinate[] shiftedCoords = new Coordinate[denserCoords.length];
        for (int i = 0; i < denserCoords.length; i++) {
            shiftedCoords[i] = new Coordinate(denserCoords[i].getX(), denserCoords[i].getY() + 100);
        }
        LineString shiftedDenserLineString = geometryFactory.createLineString(shiftedCoords);
        MultiPoint denserPoints = geometryFactory.createMultiPointFromCoords(shiftedCoords);

        VisualisationUtil.show(
                "JTS Visualisation - Geometry Densifier",
                new DrawLineString(lineString, Color.BLUE, null),
                new DrawMultiPoint(originalPoints, Color.BLUE, null),
                new DrawLineString(shiftedDenserLineString, Color.RED, null),
                new DrawMultiPoint(denserPoints, Color.RED, null),
                new DrawMultilineText("original line coordinate size " + lineString.getNumPoints() + "\n" +
                        "densified line coordinate size " + denserLineString.getNumPoints(), 60, 300, Color.WHITE)
        );
    }

}
