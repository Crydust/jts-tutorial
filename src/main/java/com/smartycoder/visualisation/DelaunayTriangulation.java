package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.show;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class DelaunayTriangulation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        int pointCount = 100;
        List<Coordinate> coords = new RandomCoordinates(25, 430, 20, 425)
                .generate(pointCount);
        DelaunayTriangulationBuilder triangleBuilder = new DelaunayTriangulationBuilder();
        triangleBuilder.setSites(coords);
        Geometry triangles = triangleBuilder.getTriangles(geometryFactory);
        List<Polygon> trianglesProduced = new ArrayList<>();
        if (triangles instanceof GeometryCollection geometryCollection) {
            System.out.println("Produced triangles count: " + geometryCollection.getNumGeometries());
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                Polygon triangle = (Polygon) geometryCollection.getGeometryN(i);
                trianglesProduced.add(triangle);
            }
        }
        DrawingCommand[] commands = new DrawingCommand[trianglesProduced.size()];
        for (int i = 0; i < trianglesProduced.size(); i++) {
            commands[i] = new DrawPolygon(trianglesProduced.get(i), Color.RED, null, null);
        }

        show("JTS Visualisation - Delaunay Triangulation", commands);
    }

}
