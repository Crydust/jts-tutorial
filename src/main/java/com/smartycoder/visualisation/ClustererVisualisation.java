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
        List<Polygon> expandedPolygons = bufferPolygons(clusterPolygons);

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

    static List<Polygon> bufferPolygons(List<Polygon> polygons) {
        double bufferDistance = 15.0; // Distance for expansion

        // First pass: buffer all polygons to expand them
        List<Polygon> buffered = new ArrayList<>();
        for (Polygon p : polygons) {
            Geometry buf = p.buffer(bufferDistance);
            if (buf instanceof Polygon bp) {
                buffered.add(bp);
            } else {
                buffered.add(p);
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

            // Apply PolygonHullSimplifier for smoothing after differencing
            // This improves the shape while maintaining the expansion
            Geometry smoothed = PolygonHullSimplifier.hull(current, true, 3.0);
            if (smoothed instanceof Polygon sp) {
                current = sp;
            }

            // Union with original polygon to ensure all cluster points are contained
            Geometry unioned = current.union(polygons.get(i));
            if (unioned instanceof Polygon up) {
                result.add(up);
            } else {
                result.add(polygons.get(i));
            }
        }

        // Fill pockets between adjacent polygons
        result = smoothPockets(polygons, result);

        return result;
    }

    /**
     * Detects and fills small gaps (pockets) between nearby polygon pairs.
     * Pockets below 1% threshold are eradicated only if filling doesn't create overlap.
     */
    static List<Polygon> smoothPockets(List<Polygon> sourcePolygons, List<Polygon> expandedPolygons) {
        double pocketThresholdPercent = 0.01; // 1% threshold
        double proximityThreshold = 2.0; // Distance threshold to consider pockets

        List<Polygon> result = new ArrayList<>(expandedPolygons);

        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                Polygon p1 = result.get(i);
                Polygon p2 = result.get(j);

                // Check if polygons are close enough to have a pocket
                if (p1.distance(p2) < proximityThreshold) {
                    Geometry pocket = findPocket(p1, p2);
                    if (pocket != null && pocket.getArea() > 0) {
                        double sourceArea1 = sourcePolygons.get(i).getArea();
                        double sourceArea2 = sourcePolygons.get(j).getArea();
                        double minSourceArea = Math.min(sourceArea1, sourceArea2);

                        // If pocket is below 1% threshold, try to eradicate it
                        if (pocket.getArea() < minSourceArea * pocketThresholdPercent) {
                            Geometry pocketFill = pocket.convexHull();
                            if (pocketFill instanceof Polygon fp) {
                                // Try filling pocket into p1
                                Polygon testFill1 = (Polygon) p1.union(fp);

                                // Only accept if it doesn't create new overlap
                                if (!testFill1.overlaps(p2)) {
                                    result.set(i, testFill1);
                                } else {
                                    // Try filling into p2 instead
                                    Polygon testFill2 = (Polygon) p2.union(fp);
                                    if (!testFill2.overlaps(p1)) {
                                        result.set(j, testFill2);
                                    }
                                    // If both would create overlap, don't fill (pocket remains)
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Creates a thin bridge polygon connecting the nearest points of two polygons.
     */
    static Geometry createBridge(Polygon p1, Polygon p2) {
        // Find the two closest points between the polygons
        Coordinate[] coords1 = p1.getExteriorRing().getCoordinates();
        Coordinate[] coords2 = p2.getExteriorRing().getCoordinates();

        double minDist = Double.MAX_VALUE;
        Coordinate c1 = null, c2 = null;

        for (Coordinate coord1 : coords1) {
            for (Coordinate coord2 : coords2) {
                double dist = coord1.distance(coord2);
                if (dist < minDist && dist > 0.01) { // Avoid identical points
                    minDist = dist;
                    c1 = coord1;
                    c2 = coord2;
                }
            }
        }

        if (c1 != null && c2 != null) {
            // Create a small line segment buffer as the bridge
            Geometry line = new org.locationtech.jts.geom.GeometryFactory().createLineString(
                    new Coordinate[]{c1, c2});
            return line.buffer(0.5); // Small buffer creates a thin polygon bridge
        }

        return null;
    }

    /**
     * Finds the pocket region between two polygons by computing the area
     * where small buffers around each polygon would intersect.
     */
    static Geometry findPocket(Polygon p1, Polygon p2) {
        // Create very thin buffers to detect the gap region
        Geometry buffer1 = p1.buffer(0.2);
        Geometry buffer2 = p2.buffer(0.2);

        // The pocket is the overlapping region of buffers minus original polygons
        Geometry overlap = buffer1.intersection(buffer2);
        Geometry pocket = overlap.difference(p1).difference(p2);

        return pocket.isEmpty() ? null : pocket;
    }

    private record ClusterableCoordinate(Coordinate coordinate) implements Clusterable {
        @Override
        public double[] getPoint() {
            return new double[]{coordinate.x, coordinate.y};
        }
    }
}
