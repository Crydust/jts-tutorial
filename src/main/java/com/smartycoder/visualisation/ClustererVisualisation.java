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
import org.locationtech.jts.simplify.PolygonHullSimplifier;
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
        double hullDistance = 10.0; // Distance for outer hull expansion and smoothing

        List<Polygon> result = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon original = polygons.get(i);

            // Generate outer hull using PolygonHullSimplifier
            // This expands and smooths the polygon in one operation
            Geometry hullGeometry = PolygonHullSimplifier.hull(original, true, hullDistance);
            Polygon current = (hullGeometry instanceof Polygon p) ? p : original;

            // Difference with all other outer hulls to prevent overlap
            for (int j = 0; j < polygons.size(); j++) {
                if (i != j) {
                    Geometry otherHullGeometry = PolygonHullSimplifier.hull(polygons.get(j), true, hullDistance);
                    Polygon otherHull = (otherHullGeometry instanceof Polygon p) ? p : polygons.get(j);
                    Geometry diff = current.difference(otherHull);
                    if (diff instanceof Polygon dp) {
                        current = dp;
                    }
                }
            }

            // Union with original polygon to ensure all cluster points are contained
            Geometry unioned = current.union(original);
            if (unioned instanceof Polygon up) {
                result.add(up);
            } else {
                result.add(original); // Fallback
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
