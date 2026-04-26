package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPoint;
import com.smartycoder.ui.DrawPolygon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class DistanceCalculation {

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
        double distance = polygon.distance(point);
        DistanceOp op = new DistanceOp(polygon, point);
        Coordinate[] nearestPoints = op.nearestPoints();
        boolean isWithinDistance103 = point.isWithinDistance(polygon, 103);
        boolean isWithinDistance102 = point.isWithinDistance(polygon, 102);
        LineString distanceLine = geometryFactory.createLineString(nearestPoints);

        show(
                "JTS Visualisation - Distance Calculation",
                new DrawPolygon(polygon, null, Color.BLUE, null),
                new DrawPoint(point, Color.WHITE, null),
                new DrawLineString(distanceLine, Color.WHITE, null),
                new DrawMultilineText("Calculated Distance: " + distance + "\n" +
                        "Is Within Distance 103? " + isWithinDistance103 + "\n" +
                        "Is Within Distance 102? " + isWithinDistance102, 70, 200, Color.WHITE)
        );
    }

}
