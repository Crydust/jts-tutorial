package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultilineText;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class CoversCalculation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] triangleCoordinates = {
                new Coordinate(250, 110),
                new Coordinate(300, 200),
                new Coordinate(190, 200),
                new Coordinate(250, 110),
        };
        Polygon triangle = geometryFactory.createPolygon(triangleCoordinates);

        Coordinate[] quadrilateralCoordinates = {
                new Coordinate(110, 80),
                new Coordinate(370, 90),
                new Coordinate(370, 250),
                new Coordinate(100, 250),
                new Coordinate(110, 80),
        };
        Polygon quadrilateral = geometryFactory.createPolygon(quadrilateralCoordinates);

        boolean quadrilateralCoversPolygon = quadrilateral.covers(triangle);
        boolean polygonCoveredByQuadrilateral = triangle.coveredBy(quadrilateral);

        VisualisationUtil.show(
                "JTS Visualisation - Covers Calculation",
                new DrawPolygon(quadrilateral, null, Color.GREEN, Color.WHITE),
                new DrawPolygon(triangle, null, Color.BLUE, Color.BLACK),
                new DrawMultilineText("Quadrilateral covers triangle? " + quadrilateralCoversPolygon + "\n" +
                        "Triangle covered by quadrilateral? " + polygonCoveredByQuadrilateral,
                        40, 285, Color.WHITE)
        );
    }

}
