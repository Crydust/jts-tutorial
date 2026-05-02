package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.show;
import static com.smartycoder.visualisation.ClustererVisualisation.bufferPolygons;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;

class ClustererVisualisationTest {

    @Test
    void shouldBbufferPolygons() throws ParseException {
        // TODO test that the buffered Polygons:
        //  * don't overlap
        //  * fully contain the original polygon
        //  * have no sharp edges
        GeometryFactory geometryFactory = new GeometryFactory();
        WKTReader reader = new WKTReader();
        Polygon polygonA = (Polygon) reader.read("""
                POLYGON ((
                    100 100, 400 100,
                    400 250, 350 250,
                    350 150, 150 150,
                    150 550, 350 550,
                    350 500, 400 500,
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
                    450 450, 300 450,
                    300 500, 200 500,
                    200 200
                ))
                """);
        List<Polygon> expandedPolygons = bufferPolygons(List.of(polygonA, polygonB), geometryFactory);

        DrawingCommand[] drawingCommands = {
                new DrawPolygon(polygonA, RED, null, null),
                new DrawPolygon(polygonB, RED, null, null),
                new DrawPolygon(expandedPolygons.get(0), BLUE, null, null),
                new DrawPolygon(expandedPolygons.get(1), BLUE, null, null),
        };
        show(
                "test",
                drawingCommands);

    }
}
