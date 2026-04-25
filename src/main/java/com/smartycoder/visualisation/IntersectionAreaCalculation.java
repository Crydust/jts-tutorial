package com.smartycoder.visualisation;

import com.smartycoder.ui.JTSVisualisationPanel;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.awt.*;
import java.util.Arrays;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class IntersectionAreaCalculation {

    static {
        System.setProperty("sun.java2d.uiScale", "2");
    }

    static void main() {

        EventQueue.invokeLater(() -> {

            JTSVisualisationPanel panel = new JTSVisualisationPanel();

            String title = "JTS Visualisation - Intersection Area Calculation";
            panel.show(title);

            GeometryFactory geometryFactory = new GeometryFactory();

            Coordinate[] blueCoordinates = {
                    new Coordinate(150, 60),
                    new Coordinate(325, 180),
                    new Coordinate(90, 150),
                    new Coordinate(150, 60)
            };

            Coordinate[] yellowCoordinates = {
                    new Coordinate(210, 120),
                    new Coordinate(350, 120),
                    new Coordinate(400, 210),
                    new Coordinate(250, 300),
                    new Coordinate(180, 150),
                    new Coordinate(210, 120)
            };

            Polygon bluePolygon = geometryFactory.createPolygon(blueCoordinates);
            Polygon yellowPolygon = geometryFactory.createPolygon(yellowCoordinates);
            Polygon intersectionArea = (Polygon) yellowPolygon.intersection(bluePolygon);

            panel.addDrawCommand(g -> {
                fillPolygon(g, Color.BLUE, bluePolygon);
                fillPolygon(g, Color.YELLOW, yellowPolygon);
                fillPolygon(g, Color.RED, intersectionArea);

                g.setColor(Color.white);
                g.drawString("The intersection area of Blue and Yellow polygons", 60, 330);
                g.drawString("is painted in RED color.", 60, 350);
            });

        });

    }

    private static void fillPolygon(Graphics g, Color color, Polygon polygon) {
        Coordinate[] coordinates = polygon.getCoordinates();
        int[] xPoints = Arrays.stream(coordinates).mapToInt(it -> (int) it.getX()).toArray();
        int[] yPoints = Arrays.stream(coordinates).mapToInt(it -> (int) it.getY()).toArray();
        int nPoints = coordinates.length;
        Color translucentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 127);
        g.setColor(translucentColor);
        g.fillPolygon(xPoints, yPoints, nPoints);
    }
}
