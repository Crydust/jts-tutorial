package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class IntersectionLineString {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {new Coordinate(60, 60), new Coordinate(150, 150)};
        LineString lineString1 = geometryFactory.createLineString(coordinates);
        LineString lineString2 = geometryFactory.createLineString(new Coordinate[]{new Coordinate(110, 20), new Coordinate(30, 250)});
        Point intersectionPoint = (Point) lineString1.intersection(lineString2);

        show(
                "JTS Visualisation - Intersection LineString",
                new DrawLineString(lineString1, Color.WHITE, null),
                new DrawLineString(lineString2, Color.WHITE, null),
                new DrawPoint(intersectionPoint, Color.RED, null)
        );
    }

}
