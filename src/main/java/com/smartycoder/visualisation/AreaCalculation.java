package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class AreaCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = {
                new Coordinate(150, 60),
                new Coordinate(200, 150),
                new Coordinate(90, 150),
                new Coordinate(150, 60),
        };
        Polygon polygon = geometryFactory.createPolygon(coordinates);

        double area = polygon.getArea();

        show(
                "JTS Visualisation - Area Calculation",
                new DrawPolygon(polygon, null, Color.BLUE, Color.WHITE),
                new DrawMultilineText("Calculated Area\n " + area, 70, 190, Color.WHITE)
        );
    }

}
