package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.colorWithAlpha;
import static com.smartycoder.ui.VisualisationUtil.saveAsFile;
import static com.smartycoder.ui.VisualisationUtil.show;
import static com.smartycoder.visualisation.ClustererVisualisation.bufferPolygons;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClustererVisualisationTest {

    @Test
    void shouldBufferPolygons() throws ParseException {
        // Given
        WKTReader reader = new WKTReader();
        Polygon polygonA = (Polygon) reader.read("""
                POLYGON ((
                    100 100, 400 100,
                    425 275, 320 250,
                    350 150, 150 150,
                    150 550, 350 550,
                    325 425, 425 500,
                    400 600, 100 600,
                    100 100
                ))
                """);
        Polygon polygonB = (Polygon) reader.read("""
                POLYGON ((
                    200 200, 300 200,
                    300 300, 450 300,
                    450 100, 600 100,
                    600 600, 450 600,
                    450 450, 250 350,
                    300 500, 200 500,
                    200 200
                ))
                """);

        // When
        List<Polygon> expandedPolygons = bufferPolygons(List.of(polygonA, polygonB));
        Polygon expandedA = expandedPolygons.get(0);
        Polygon expandedB = expandedPolygons.get(1);

        // Debug
        boolean debug = true;
        if (debug) {
            DrawingCommand[] drawingCommands = {
                    new DrawPolygon(polygonA, Color.RED, null, null),
                    new DrawPolygon(polygonB, Color.RED, null, null),
                    new DrawPolygon(expandedA, Color.BLUE, colorWithAlpha(Color.BLUE, 20), null),
                    new DrawPolygon(expandedB, Color.GREEN, colorWithAlpha(Color.GREEN, 20), null),
            };
            //show("test", drawingCommands);
            saveAsFile(Path.of("test.png"), drawingCommands);
        }

        // Then
        assertFalse(expandedA.overlaps(expandedB), "The buffered polygons overlap");
        assertTrue(expandedA.contains(polygonA), "The buffered polygon doesn't fully contain the original polygon");
        assertTrue(expandedB.contains(polygonB), "The buffered polygon doesn't fully contain the original polygon");

        assertNoSmallGaps(expandedA, expandedB);
        // We'll look into rounding the sharp edges at a later time
//        assertTrue(hasNoSharpEdges(expandedA, 90), "expandedA has sharp edges (angles less than n degrees)");
//        assertTrue(hasNoSharpEdges(expandedB, 90), "expandedB has sharp edges (angles less than n degrees)");
    }

    private void assertNoSmallGaps(Polygon p1, Polygon p2) {
        Coordinate[] coords1 = p1.getExteriorRing().getCoordinates();
        Coordinate[] coords2 = p2.getExteriorRing().getCoordinates();

        boolean gapFound = false;
        double maxGapDistance = 0;

        // Check if any vertex of p1 is "close but not touching" p2
        for (Coordinate c : coords1) {
            double dist = p2.distance(p1.getFactory().createPoint(c));
            if (dist > 0.001 && dist < 5.0) {
                gapFound = true;
                maxGapDistance = Math.max(maxGapDistance, dist);
            }
        }
        // And vice versa
        for (Coordinate c : coords2) {
            double dist = p1.distance(p2.getFactory().createPoint(c));
            if (dist > 0.001 && dist < 5.0) {
                gapFound = true;
                maxGapDistance = Math.max(maxGapDistance, dist);
            }
        }

        assertFalse(gapFound, "Found a small gap between polygons. Max gap distance: " + maxGapDistance);
    }

    /**
     * Checks if a polygon has no sharp edges by verifying all interior angles are >= threshold degrees.
     * Returns true if all angles are >= threshold degrees, false otherwise.
     */
    private static boolean hasNoSharpEdges(Polygon polygon, double threshold) {
        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        int n = coords.length - 1; // Exclude the repeated closing coordinate

        for (int i = 0; i < n; i++) {
            Coordinate prev = coords[(i - 1 + n) % n];
            Coordinate curr = coords[i];
            Coordinate next = coords[(i + 1) % n];

            // Calculate vectors from current point to previous and next
            double angleRadians = Angle.angleBetween(prev, curr, next);
            double angleDegrees = Math.toDegrees(angleRadians);

            // If angle is less than threshold degrees, it's a sharp edge
            if (angleDegrees < threshold) {
                return false;
            }
        }

        return true;
    }
}
