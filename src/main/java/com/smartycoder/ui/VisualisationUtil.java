package com.smartycoder.ui;

import org.eclipse.imagen.media.colorindexer.ColorIndexer;
import org.eclipse.imagen.media.colorindexer.ColorUtils;
import org.eclipse.imagen.media.colorindexer.Quantizer;

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
        image = quantizeImage(image, 32);
        try {
            ImageIO.write(image, "PNG", path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image to " + path, e);
        }
    }

    private static BufferedImage quantizeImage(BufferedImage image, int maxColors) {
        if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        ColorIndexer colorIndexer = new Quantizer(maxColors).buildColorIndexer(image);

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        byte[] indexes = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = ColorUtils.red(pixel);
            int g = ColorUtils.green(pixel);
            int b = ColorUtils.blue(pixel);
            int a = ColorUtils.alpha(pixel);
            indexes[i] = (byte) (colorIndexer.getClosestIndex(r, g, b, a) & 0xFF);
        }

        BufferedImage indexedImage = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_BYTE_INDEXED,
                colorIndexer.toIndexColorModel()
        );
        indexedImage.getRaster().setDataElements(0, 0, width, height, indexes);

        return indexedImage;
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

}
