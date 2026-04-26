package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;
import java.util.Random;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class ConvexHullVisualisation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Random randomX = new Random();
        Random randomY = new Random();
        int pointCount = 100;
        Point[] points = new Point[pointCount];
        for (int i = 1; i <= pointCount; i++) {
            int xRandomlySelected = randomX.nextInt(250) + 65;
            int yRandomlySelected = randomY.nextInt(250) + 50;
            System.out.println(i + ". randomly selected point " + xRandomlySelected + ", " + yRandomlySelected);
            Coordinate coordinate = new Coordinate(xRandomlySelected, yRandomlySelected);
            points[i - 1] = geometryFactory.createPoint(coordinate);
        }
        MultiPoint multiPoint = geometryFactory.createMultiPoint(points);
        Polygon convexHull = (Polygon) multiPoint.convexHull();

        VisualisationUtil.show(
                "JTS Visualisation - Convex Hull",
                new DrawPolygon(convexHull, Color.RED, null, null),
                new DrawMultiPoint(multiPoint, Color.WHITE, null)
        );
    }

}
