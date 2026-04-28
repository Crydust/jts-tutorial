package com.smartycoder.ui;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;

public final class VisualisationUtil {

    static {
        enableHighDpiRendering();
    }

    private VisualisationUtil() {
        // NOOP
    }

    public static void show(String title, DrawingCommand... commands) {
        BufferedImage image = createImage(commands, true);
        show(title, image);
    }

    public static void saveAsFile(Path path, DrawingCommand... commands) {
        BufferedImage image = createImage(commands, false);
        BufferedImage indexed = convertTo256Colors(image);
        try {
            ImageIO.write(indexed, "PNG", path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image to " + path, e);
        }
    }

    private static BufferedImage createImage(DrawingCommand[] commands, boolean drawAxis) {
        int width = 600, height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            enableHighQualityRendering(g);

            // Draw background
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 600, 600);

            if (drawAxis) {
                // Draw x axis
                g.setColor(Color.WHITE);
                g.drawLine(10, 10, 600, 10);
                for (int i = 10; i <= 600; i += 50) {
                    g.drawString(Integer.toString(i), i, 10);
                }

                // Draw y axis
                g.setColor(Color.WHITE);
                g.drawLine(10, 10, 10, 600);
                for (int i = 10; i <= 600; i += 50) {
                    g.drawString(Integer.toString(i), 10, i);
                }
            }

            // Reset default color
            g.setColor(Color.BLACK);

            // Draw geometries
            for (DrawingCommand command : commands) {
                command.doDrawing(g);
            }
        } finally {
            g.dispose();
        }
        return image;
    }

    private static void show(String title, BufferedImage image) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                JDialog dialog = new JDialog();
                dialog.setTitle(title);
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.add(new ImageComponent(image));
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enableHighDpiRendering() {
        System.setProperty("sun.java2d.uiScale", "2");
    }

    private static void enableHighQualityRendering(Graphics g) {
        if (g instanceof Graphics2D g2d) {
            g2d.addRenderingHints(Map.of(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
                    RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,
                    RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY,
                    RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                    RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE,
                    RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE,
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                    RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON
            ));
        }
    }

    public static Color colorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static class ImageComponent extends JComponent {
        private final BufferedImage image;

        public ImageComponent(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
        }
    }

    private static BufferedImage convertTo256Colors(BufferedImage image) {
        // Collect unique colors from the image
        java.util.Set<Integer> colorSet = new java.util.HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                colorSet.add(image.getRGB(x, y));
            }
        }

        int numColors = Math.min(colorSet.size(), 256);
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        byte[] a = new byte[256];

        // If we have 256 or fewer colors, use them directly
        if (numColors <= 256) {
            Integer[] colors = colorSet.toArray(new Integer[0]);
            for (int i = 0; i < numColors; i++) {
                int rgb = colors[i];
                r[i] = (byte) ((rgb >> 16) & 0xFF);
                g[i] = (byte) ((rgb >> 8) & 0xFF);
                b[i] = (byte) (rgb & 0xFF);
                a[i] = (byte) ((rgb >> 24) & 0xFF);
            }
            // Fill remaining slots with black
            for (int i = numColors; i < 256; i++) {
                r[i] = 0;
                g[i] = 0;
                b[i] = 0;
                a[i] = (byte) 255;
            }
        } else {
            // For more than 256 colors, use simple quantization
            // Reduce each color component to 6 bits (64 levels) then combine
            for (int i = 0; i < 256; i++) {
                // Create a palette by distributing colors across RGB space
                int red = (i / 36) * 51;   // 6 levels: 0, 51, 102, 153, 204, 255
                int green = ((i / 6) % 6) * 51;
                int blue = (i % 6) * 51;
                r[i] = (byte) red;
                g[i] = (byte) green;
                b[i] = (byte) blue;
                a[i] = (byte) 255;
            }
        }

        IndexColorModel colorModel = new IndexColorModel(8, 256, r, g, b, a);
        BufferedImage indexed = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        Graphics2D g2d = indexed.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return indexed;
    }
}
