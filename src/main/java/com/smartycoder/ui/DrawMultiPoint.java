package com.smartycoder.ui;

import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;

import java.awt.Color;
import java.awt.Graphics;

public record DrawMultiPoint(MultiPoint multiPoint, Color fillColor, Color textColor) implements DrawingCommand {

    @Override
    public void doDrawing(Graphics g) {
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            Point point = (Point) multiPoint.getGeometryN(i);
            int x = (int) point.getX();
            int y = (int) point.getY();
            g.setColor(fillColor);
            g.fillOval(x - 2, y - 2, 4, 4);
            if (textColor != null) {
                g.setColor(textColor);
                g.drawString("(%d, %d)".formatted(x, y), x - 20, y - 10);
            }
        }
    }
}
