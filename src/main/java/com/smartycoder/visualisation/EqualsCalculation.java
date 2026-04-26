package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultilineText;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class EqualsCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates1 = {new Coordinate(60, 60), new Coordinate(150, 60), new Coordinate(350, 60)};
        Coordinate[] coordinates2 = {new Coordinate(350, 60), new Coordinate(60, 60)};
        LineString line1 = geometryFactory.createLineString(coordinates1);
        LineString line2 = geometryFactory.createLineString(coordinates2);
        System.out.println("(line1.equals((Object)line2)) " + (line1.equals((Object) line2)));
        System.out.println("(line1.equals(line2)) " + (line1.equals(line2)));
        System.out.println("(line1.equals((Geometry)line2)) " + (line1.equals(line2)));
        System.out.println("(line1.equalsExact(line2)) " + (line1.equalsExact(line2)));

        show(
                "JTS Visualisation - Equals Calculation",
                new DrawLineString(line1, Color.RED, null),
                new DrawLineString(line2, Color.BLUE, null),
                new DrawMultilineText("line1.equals((Object)line2) " + (line1.equals((Object) line2)) + "\n" +
                        "line1.equals(line2) " + (line1.equals(line2)) + "\n" +
                        "line1.equals((Geometry)line2) " + (line1.equals(line2)) + "\n" +
                        "line1.equalsExact(line2) " + line1.equalsExact(line2), 70, 90, Color.WHITE)
        );
    }

}
