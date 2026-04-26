package com.smartycoder.ui;

import java.awt.Color;
import java.awt.Graphics;

public record DrawMultilineText(String text, int x, int y, Color textColor) implements DrawingCommand {

    private static final int LINE_HEIGHT = 20;

    @Override
    public void doDrawing(Graphics g) {
        g.setColor(textColor);
        int lineNumber = 0;
        for (String line : text.lines().toList()) {
            g.drawString(line, x, y + lineNumber * LINE_HEIGHT);
            lineNumber++;
        }
    }
}
