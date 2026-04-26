package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawPolygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;
import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class ConvexHullVisualisation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        int pointCount = 100;
        List<Coordinate> coordinates = new RandomCoordinates(65, 315, 50, 300)
                .generate(pointCount);
        Point[] points = new Point[pointCount];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = geometryFactory.createPoint(coordinates.get(i));
        }
        MultiPoint multiPoint = geometryFactory.createMultiPoint(points);
        Polygon convexHull = (Polygon) multiPoint.convexHull();

        show(
                "JTS Visualisation - Convex Hull",
                new DrawPolygon(convexHull, Color.RED, null, null),
                new DrawMultiPoint(multiPoint, Color.WHITE, null)
        );
    }

}
