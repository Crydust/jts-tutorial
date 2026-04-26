package com.smartycoder.ui;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

public record DrawPolygon(Polygon polygon, Color drawColor, Color fillColor,
                          Color textColor) implements DrawingCommand {

    @Override
    public void doDrawing(Graphics g) {
        Coordinate[] coords = polygon.getCoordinates();

        int[] xPoints = Arrays.stream(coords).mapToInt(coord -> (int) coord.getX()).toArray();
        int[] yPoints = Arrays.stream(coords).mapToInt(coord -> (int) coord.getY()).toArray();
        int nPoints = coords.length;
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fillPolygon(xPoints, yPoints, nPoints);
        }
        if (drawColor != null) {
            g.setColor(drawColor);
            g.drawPolygon(xPoints, yPoints, nPoints);
        }

        if (textColor != null) {
            double centerY = Arrays.stream(coords).mapToDouble(Coordinate::getY).average().orElse(0);
            g.setColor(textColor);
            for (Coordinate coord : coords) {
                int x = (int) coord.getX();
                int y = (int) coord.getY();
                g.drawString(
                        "(%d, %d)".formatted(x, y),
                        x - 20,
                        y + (y < centerY ? -10 : 15));
            }
        }

    }
}
