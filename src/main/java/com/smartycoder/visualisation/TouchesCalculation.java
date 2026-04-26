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
public class TouchesCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates1 = {new Coordinate(60, 60), new Coordinate(110, 110), new Coordinate(155, 60),
                new Coordinate(60, 60)};
        Coordinate[] coordinates2 = {new Coordinate(155, 60), new Coordinate(250, 150), new Coordinate(310, 40),
                new Coordinate(155, 60)};
        Coordinate[] coordinates3 = {new Coordinate(150, 120), new Coordinate(300, 120),
                new Coordinate(300, 180), new Coordinate(150, 180), new Coordinate(150, 120)};
        Polygon poly1 = geometryFactory.createPolygon(coordinates1);
        Polygon poly2 = geometryFactory.createPolygon(coordinates2);
        Polygon poly3 = geometryFactory.createPolygon(coordinates3);
        System.out.println("poly1.touches(poly2)  " + (poly1.touches(poly2)));
        System.out.println("poly1.intersects(poly2)  " + (poly1.intersects(poly2)));
        System.out.println("poly1.overlaps(poly2)  " + (poly1.overlaps(poly2)));
        System.out.println("-----------------");
        System.out.println("poly1.touches(poly3)  " + (poly1.touches(poly3)));
        System.out.println("poly1.intersects(poly3)  " + (poly1.intersects(poly3)));
        System.out.println("poly1.overlaps(poly3)  " + (poly1.overlaps(poly3)));
        System.out.println("-----------------");
        System.out.println("poly3.touches(poly1) " + (poly3.touches(poly1)));
        System.out.println("poly3.intersects(poly1)  " + (poly3.intersects(poly1)));
        System.out.println("poly3.overlaps(poly1)  " + (poly3.overlaps(poly1)));
        System.out.println("-----------------");
        System.out.println("poly3.touches(poly2)  " + (poly3.touches(poly2)));
        System.out.println("poly3.intersects(poly2)  " + (poly3.intersects(poly2)));
        System.out.println("poly3.overlaps(poly2)  " + (poly3.overlaps(poly2)));
        System.out.println("-----------------");
        System.out.println("poly2.touches(poly3)  " + (poly2.touches(poly3)));
        System.out.println("poly2.intersects(poly3)  " + (poly2.intersects(poly3)));
        System.out.println("poly2.overlaps(poly3)  " + (poly2.overlaps(poly3)));

        show(
                "JTS Visualisation - Touches Calculation",
                new DrawPolygon(poly1, null, Color.BLUE, null),
                new DrawPolygon(poly2, null, Color.RED, null),
                new DrawPolygon(poly3, null, colorWithAlpha(Color.YELLOW, 150), null),
                new DrawMultilineText("poly1", 100, 80, Color.WHITE),
                new DrawMultilineText("poly2", 220, 80, Color.WHITE),
                new DrawMultilineText("poly3", 160, 160, Color.WHITE),
                new DrawMultilineText("poly1.touches(poly2)  " + (poly1.touches(poly2)) + "\n" +
                        "poly1.intersects(poly2)  " + (poly1.intersects(poly2)) + "\n" +
                        "poly1.overlaps(poly2)  " + (poly1.overlaps(poly2)) + "\n" +
                        "poly1.touches(poly3)  " + (poly1.touches(poly3)) + "\n" +
                        "poly1.intersects(poly3)  " + (poly1.intersects(poly3)) + "\n" +
                        "poly1.overlaps(poly3)  " + (poly1.overlaps(poly3)), 40, 200, Color.WHITE),
                new DrawMultilineText("poly3.touches(poly1) " + (poly3.touches(poly1)) + "\n" +
                        "poly3.intersects(poly1)  " + (poly3.intersects(poly1)) + "\n" +
                        "poly3.overlaps(poly1)  " + (poly3.overlaps(poly1)) + "\n" +
                        "poly3.touches(poly2)  " + (poly3.touches(poly2)) + "\n" +
                        "poly3.intersects(poly2)  " + (poly3.intersects(poly2)) + "\n" +
                        "poly3.overlaps(poly2)  " + (poly3.overlaps(poly2)), 230, 200, Color.WHITE),
                new DrawMultilineText("poly2.touches(poly3)  " + (poly2.touches(poly3)) + "\n" +
                        "poly2.intersects(poly3)  " + (poly2.intersects(poly3)) + "\n" +
                        "poly2.overlaps(poly3)  " + (poly2.overlaps(poly3)), 140, 340, Color.WHITE)
        );
    }

}
