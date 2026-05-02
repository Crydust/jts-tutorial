package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.awt.Color;
import java.util.List;

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
                    new DrawPolygon(expandedA, Color.BLUE, null, null),
                    new DrawPolygon(expandedB, Color.GREEN, null, null),
            };
            show("test", drawingCommands);
        }

        // Then
        assertFalse(expandedA.overlaps(expandedB), "The buffered polygons overlap");
        assertTrue(expandedA.contains(polygonA), "The buffered polygon doesn't fully contain the original polygon");
        assertTrue(expandedB.contains(polygonB), "The buffered polygon doesn't fully contain the original polygon");
        // TODO check expandedA has no "sharp edges" aka angles less than 90 degrees
    }
}
