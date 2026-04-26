package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawLineString;
import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.colorWithAlpha;
import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class CrossesCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates1 = {
                new Coordinate(60, 60),
                new Coordinate(110, 110),
                new Coordinate(155, 60),
                new Coordinate(60, 60),
        };
        Coordinate[] coordinates2 = {
                new Coordinate(150, 120),
                new Coordinate(300, 120),
                new Coordinate(300, 180),
                new Coordinate(150, 180),
                new Coordinate(150, 120),
        };
        Coordinate[] coordinates3 = {new Coordinate(40, 75), new Coordinate(350, 120)};
        Coordinate[] coordinates4 = {new Coordinate(210, 160), new Coordinate(350, 250)};
        Polygon poly1 = geometryFactory.createPolygon(coordinates1);
        Polygon poly2 = geometryFactory.createPolygon(coordinates2);
        LineString line1 = geometryFactory.createLineString(coordinates3);
        LineString line2 = geometryFactory.createLineString(coordinates4);

        System.out.println("poly1.crosses(line1)  " + (poly1.crosses(line1)));
        System.out.println("-----------------");
        System.out.println("line1.crosses(poly2)  " + (line1.crosses(poly2)));
        System.out.println("-----------------");
        System.out.println("line1.crosses(poly1)  " + (line1.crosses(poly1)));
        System.out.println("-----------------");
        System.out.println("poly2.crosses(line2)  " + poly2.crosses(line2));
        System.out.println("-----------------");
        System.out.println("line2.crosses(poly2)  " + line2.crosses(poly2));

        show(
                "JTS Visualisation - Crosses Calculation",
                new DrawPolygon(poly1, null, Color.BLUE, null),
                new DrawPolygon(poly2, null, colorWithAlpha(Color.YELLOW, 150), null),
                new DrawLineString(line1, Color.WHITE, null),
                new DrawLineString(line2, Color.WHITE, null),
                new DrawMultilineText("poly1", 100, 80, Color.WHITE),
                new DrawMultilineText("poly2", 160, 160, Color.WHITE),
                new DrawMultilineText("line1", 310, 100, Color.WHITE),
                new DrawMultilineText("line2", 310, 220, Color.WHITE),
                new DrawMultilineText("poly1.crosses(line1)  " + (poly1.crosses(line1)) + "\n" +
                        "line1.crosses(poly2)  " + (line1.crosses(poly2)) + "\n" +
                        "line1.crosses(poly1)  " + (line1.crosses(poly1)) + "\n" +
                        "poly2.crosses(line2)  " + poly2.crosses(line2) + "\n" +
                        "line2.crosses(poly2)  " + line2.crosses(poly2), 50, 210, Color.WHITE)
        );
    }

}
