package com.smartycoder.ui;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

public record DrawLineString(LineString lineString, Color strokeColor, Color textColor) implements DrawingCommand {

    @Override
    public void doDrawing(Graphics g) {
        Coordinate[] coords = lineString.getCoordinates();
        g.setColor(strokeColor);
        for (int i = 0; i < coords.length - 1; i++) {
            int x1 = (int) coords[i].getX();
            int y1 = (int) coords[i].getY();
            int x2 = (int) coords[i + 1].getX();
            int y2 = (int) coords[i + 1].getY();
            g.drawLine(x1, y1, x2, y2);
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
