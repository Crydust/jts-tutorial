package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class VoronoiDiagramVisualisation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Random randomX = new Random();
        Random randomY = new Random();
        List<Coordinate> coords = new ArrayList<>();
        int pointCount = 100;
        for (int i = 1; i <= pointCount; i++) {
            int xRandomlySelected = randomX.nextInt(250) + 65;
            int yRandomlySelected = randomY.nextInt(250) + 50;
            System.out.println(i + ". randomly selected point " + xRandomlySelected + ", " + yRandomlySelected);
            coords.add(new Coordinate(xRandomlySelected, yRandomlySelected));
        }
        VoronoiDiagramBuilder diagramBuilder = new VoronoiDiagramBuilder();
        diagramBuilder.setSites(coords);
        Geometry polygonCollection = diagramBuilder.getDiagram(geometryFactory);
        List<Polygon> producedPolygons = new ArrayList<>();
        if (polygonCollection instanceof GeometryCollection geometryCollection) {
            System.out.println("Produced polygon count: " + geometryCollection.getNumGeometries());
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) geometryCollection.getGeometryN(i);
                producedPolygons.add(polygon);
            }
        }
        DrawingCommand[] commands = new DrawingCommand[producedPolygons.size() + 1];
        for (int i = 0; i < producedPolygons.size(); i++) {
            commands[i] = new DrawPolygon(producedPolygons.get(i), Color.RED, null, null);
        }
        MultiPoint multiPoint = geometryFactory.createMultiPointFromCoords(coords.toArray(new Coordinate[0]));
        commands[producedPolygons.size()] = new DrawMultiPoint(multiPoint, Color.WHITE, null);

        VisualisationUtil.show("JTS Visualisation - Voronoi Diagram", commands);
    }

}
