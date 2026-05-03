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
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;
import org.locationtech.jts.simplify.PolygonHullSimplifier;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.colorWithAlpha;
import static com.smartycoder.ui.VisualisationUtil.saveAsFile;
import static com.smartycoder.ui.VisualisationUtil.show;

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
        List<Polygon> expandedPolygons = bufferPolygons(clusterPolygons);

        for (Polygon expandedPolygon : expandedPolygons) {
            drawingCommands.add(new DrawPolygon(expandedPolygon, Color.BLUE, colorWithAlpha(Color.BLUE, 20), null));
        }

        drawingCommands.add(new DrawMultiPoint(multiPoint, Color.WHITE, null));

        show("cluster", drawingCommands.toArray(new DrawingCommand[0]));
        saveAsFile(Path.of("cluster.png"), drawingCommands.toArray(new DrawingCommand[0]));
    }

    private static Point[] coordinatesToPoints(int pointCount, List<Coordinate> coordinates, GeometryFactory geometryFactory) {
        Point[] points = new Point[pointCount];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = geometryFactory.createPoint(coordinates.get(i));
        }
        return points;
    }

    static List<Polygon> bufferPolygons(List<Polygon> polygons) {
        double desiredufferDistance = 15.0; // Distance for expansion

        // First pass: buffer all polygons to expand them
        List<Polygon> buffered = new ArrayList<>();
        for (Polygon p : polygons) {
            // Buffer by less than the distance to the nearest other polygon
            double actualBufferDistance = desiredufferDistance;
            for (Polygon other : polygons) {
                if (other == p) continue;
                actualBufferDistance = Math.min(p.distance(other) / 1.3, actualBufferDistance);
            }

            Geometry buf = p.buffer(actualBufferDistance);
            if (buf instanceof Polygon bp) {
                buffered.add(bp);
            } else {
                buffered.add(p);
            }
        }

        // Second pass: difference and simplify
        for (int i = 0; i < buffered.size(); i++) {
            Polygon current = buffered.get(i);

            // Apply PolygonHullSimplifier for smoothing
            Geometry smoothed = PolygonHullSimplifier.hull(current, true, 0.5 /* between 0 and 1 */);
            if (smoothed instanceof Polygon sp) {
                current = sp;
            }

            // Difference with all other buffered polygons to prevent overlap
            for (int j = 0; j < buffered.size(); j++) {
                if (i != j) {
                    Geometry diff = current.difference(buffered.get(j));
                    if (diff instanceof Polygon dp) {
                        current = dp;
                    }
                }
            }

            buffered.set(i, current);
        }

        // Third pass: Remove small gaps by snapping
        // This replaces "inward bulges" with segments that touch the other polygon
        for (int i = 0; i < buffered.size(); i++) {
            for (int j = 0; j < buffered.size(); j++) {
                if (i != j) {
                    // Snap vertices of buffered(i) to buffered(j) if they are within 5.0 units
                    // This creates a shared boundary instead of a small gap
                    Geometry[] snapped = GeometrySnapper.snap(buffered.get(i), buffered.get(j), 5.0);
                    if (!(snapped[0] instanceof MultiPolygon) && snapped[0] instanceof Polygon spi
                            && !(snapped[1] instanceof MultiPolygon) && snapped[1] instanceof Polygon spj) {
                        buffered.set(i, spi);
                        buffered.set(j, spj);
                    }
                }
            }
        }

        List<Polygon> result = new ArrayList<>();
        for (int i = 0; i < buffered.size(); i++) {
            // Union with original polygon to ensure all cluster points are contained
            Geometry unioned = buffered.get(i).union(polygons.get(i));
            if (unioned instanceof Polygon up) {
                result.add(up);
            } else {
                result.add(polygons.get(i));
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
