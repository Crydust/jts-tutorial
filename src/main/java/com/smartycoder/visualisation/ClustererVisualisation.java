package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.hipparchus.clustering.CentroidCluster;
import org.hipparchus.clustering.Cluster;
import org.hipparchus.clustering.Clusterable;
import org.hipparchus.clustering.FuzzyKMeansClusterer;
import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.saveAsFile;

public class ClustererVisualisation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        int pointCount = 400;
        List<Coordinate> coordinates = new RandomCoordinates(50, 500, 50, 500)
                .generate(pointCount);
        Point[] points = coordinatesToPoints(pointCount, coordinates, geometryFactory);
        MultiPoint multiPoint = geometryFactory.createMultiPoint(points);

        List<CentroidCluster<ClusterableCoordinate>> clusters = new FuzzyKMeansClusterer<ClusterableCoordinate>(
                8, 3)
                .cluster(coordinates.stream()
                        .map(ClusterableCoordinate::new)
                        .toList());
        List<Polygon> clusterPolygons = new ArrayList<>();
        for (Cluster<ClusterableCoordinate> cluster : clusters) {
            Coordinate[] clusterCoordinates = cluster.getPoints().stream()
                    .map(ClusterableCoordinate::coordinate)
                    .toArray(Coordinate[]::new);
            MultiPoint clusterMultiPoint = geometryFactory.createMultiPoint(new CoordinateArraySequence(clusterCoordinates));
            Polygon clusterPolygon = (Polygon) ConcaveHull.concaveHullByLengthRatio(clusterMultiPoint, 0.65);
            clusterPolygons.add(clusterPolygon);
        }

        List<DrawingCommand> drawingCommands = new ArrayList<>();
        for (Polygon clusterPolygon : clusterPolygons) {
            drawingCommands.add(new DrawPolygon(clusterPolygon, Color.RED, null, null));
        }

        // This line can be completely changed:
        List<Polygon> expandedPolygons = bufferPolygons(clusterPolygons, geometryFactory);

        for (Polygon expandedPolygon : expandedPolygons) {
            drawingCommands.add(new DrawPolygon(expandedPolygon, Color.BLUE, null, null));
        }

        drawingCommands.add(new DrawMultiPoint(multiPoint, Color.WHITE, null));

//        show("cluster", drawingCommands.toArray(new DrawingCommand[0]));
        saveAsFile(Path.of("cluster.png"), drawingCommands.toArray(new DrawingCommand[0]));
    }

    private static Point[] coordinatesToPoints(int pointCount, List<Coordinate> coordinates, GeometryFactory geometryFactory) {
        Point[] points = new Point[pointCount];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = geometryFactory.createPoint(coordinates.get(i));
        }
        return points;
    }

    private static List<Polygon> bufferPolygons(List<Polygon> polygons, GeometryFactory geometryFactory) {
        double bufferDistance = 10.0; // Fixed buffer distance for simplicity; adjust as needed

        List<Polygon> buffered = new ArrayList<>();
        for (Polygon p : polygons) {
            Geometry buf = p.buffer(bufferDistance);
            if (buf instanceof Polygon bp) {
                buffered.add(bp);
            } else {
                buffered.add(p); // Fallback if buffer is not a polygon
            }
        }

        List<Polygon> result = new ArrayList<>();
        for (int i = 0; i < buffered.size(); i++) {
            Polygon current = buffered.get(i);

            // Difference with all other buffered polygons to prevent overlap
            for (int j = 0; j < buffered.size(); j++) {
                if (i != j) {
                    Geometry diff = current.difference(buffered.get(j));
                    if (diff instanceof Polygon dp) {
                        current = dp;
                    }
                }
            }

            // Smooth the polygon
            TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(current);
            simplifier.setDistanceTolerance(5.0);
            Geometry simplified = simplifier.getResultGeometry();
            if (simplified instanceof Polygon sp) {
                current = sp;
            }

            // Union with original polygon to ensure all cluster points are contained
            Geometry unioned = current.union(polygons.get(i));
            if (unioned instanceof Polygon up) {
                result.add(up);
            } else {
                result.add(polygons.get(i)); // Fallback
            }
        }

        return result;
    }

    private record ClusterableCoordinate(Coordinate coordinate) implements Clusterable {
        @Override
        public double[] getPoint() {
            return new double[]{coordinate.x, coordinate.y};
        }
    }
}
