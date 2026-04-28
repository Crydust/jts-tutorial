package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.hipparchus.clustering.CentroidCluster;
import org.hipparchus.clustering.Cluster;
import org.hipparchus.clustering.Clusterable;
import org.hipparchus.clustering.FuzzyKMeansClusterer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.smartycoder.ui.VisualisationUtil.saveAsFile;
import static com.smartycoder.ui.VisualisationUtil.show;

public class DBSCANClustererVisualisation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        int pointCount = 400;
        List<Coordinate> coordinates = new RandomCoordinates(50, 500, 50, 500)
                .generate(pointCount);
        Point[] points = coordinatesToPoints(pointCount, coordinates, geometryFactory);
        MultiPoint multiPoint = geometryFactory.createMultiPoint(points);

        List<CentroidCluster<ClusterableCoordinate>> clusters = new FuzzyKMeansClusterer<ClusterableCoordinate>(
                7, 3)
                .cluster(coordinates.stream()
                        .map(ClusterableCoordinate::new)
                        .toList());
        List<Polygon> clusterPolygons = new ArrayList<>();
        for (Cluster<ClusterableCoordinate> cluster : clusters) {
            Coordinate[] clusterCoordinates = cluster.getPoints().stream()
                    .map(ClusterableCoordinate::coordinate)
                    .toArray(Coordinate[]::new);
            MultiPoint clusterMultiPoint = geometryFactory.createMultiPoint(new CoordinateArraySequence(clusterCoordinates));
            Polygon clusterPolygon = (Polygon) clusterMultiPoint.convexHull();
            clusterPolygons.add(clusterPolygon);
        }

        List<DrawingCommand> drawingCommands = new ArrayList<>();
        for (Polygon clusterPolygon : clusterPolygons) {
            drawingCommands.add(new DrawPolygon(clusterPolygon, Color.RED, null, null));
        }

        // Expand polygons to midpoints between neighbors
        List<Polygon> expandedPolygons = expandPolygonsToMidpoints(clusterPolygons, geometryFactory);

        for (Polygon expandedPolygon : expandedPolygons) {
            drawingCommands.add(new DrawPolygon(expandedPolygon, Color.BLUE, null, null));
        }

        drawingCommands.add(new DrawMultiPoint(multiPoint, Color.WHITE, null));

        show("cluster", drawingCommands.toArray(new DrawingCommand[0]));
        saveAsFile(Path.of("cluster.png"), drawingCommands.toArray(new DrawingCommand[0]));
    }

    private static List<Polygon> expandPolygonsToMidpoints(List<Polygon> polygons, GeometryFactory geometryFactory) {
        List<Polygon> expandedPolygons = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon currentPolygon = polygons.get(i);
            Coordinate[] coords = currentPolygon.getExteriorRing().getCoordinates();

            // Split each edge into smaller segments
            List<Coordinate> newCoords = new ArrayList<>();
            for (int j = 0; j < coords.length - 1; j++) {
                Coordinate start = coords[j];
                Coordinate end = coords[j + 1];

                newCoords.add(start);

                // Add midpoints along the edge (segment into 4 parts = 3 midpoints)
                for (int k = 1; k < 4; k++) {
                    double t = k / 4.0;
                    Coordinate midpoint = new Coordinate(
                            start.x + t * (end.x - start.x),
                            start.y + t * (end.y - start.y)
                    );
                    newCoords.add(midpoint);
                }
            }

            // Find nearest neighbor polygon for each node
            Polygon expandedPolygon = expandPolygonOutward(currentPolygon, polygons, i, newCoords, geometryFactory);
            expandedPolygons.add(expandedPolygon);
        }

        return expandedPolygons;
    }

    private static Polygon expandPolygonOutward(Polygon currentPolygon, List<Polygon> allPolygons, int currentIndex,
                                                List<Coordinate> coords, GeometryFactory geometryFactory) {
        List<Coordinate> expandedCoords = new ArrayList<>();

        // Get the center of the current polygon
        Coordinate centerCoord = currentPolygon.getCentroid().getCoordinate();

        for (Coordinate coord : coords) {
            Point point = geometryFactory.createPoint(coord);

            // Vector from center to this coordinate (outward direction)
            double outwardX = coord.x - centerCoord.x;
            double outwardY = coord.y - centerCoord.y;
            double outwardLength = Math.sqrt(outwardX * outwardX + outwardY * outwardY);

            if (outwardLength == 0) {
                expandedCoords.add(coord);
                continue;
            }

            // Normalize the outward direction
            outwardX /= outwardLength;
            outwardY /= outwardLength;

            // Find 3 closest polygons for THIS specific coordinate
            List<PolygonDistance> nearbyPolygons = new ArrayList<>();
            double maxCutoffDistance = 50; // Adjust this based on your coordinate scale

            for (int i = 0; i < allPolygons.size(); i++) {
                if (i == currentIndex) continue;

                Polygon otherPolygon = allPolygons.get(i);
                double distance = point.distance(otherPolygon);

                // Only consider polygons within cutoff distance
                if (distance > maxCutoffDistance) continue;

                nearbyPolygons.add(new PolygonDistance(otherPolygon, distance));
            }

            // Sort by distance and keep top 3
            nearbyPolygons.sort((a, b) -> Double.compare(a.distance, b.distance));
            nearbyPolygons = nearbyPolygons.stream().limit(3).toList();

            // Try to expand towards each candidate, use the best valid one
            Coordinate bestExpandedCoord = null;
            double bestDotProduct = -1;

            for (PolygonDistance pd : nearbyPolygons) {
                Polygon nearestPolygon = pd.polygon;

                // Find closest point on this polygon's boundary
                Coordinate closestPointOnNeighbor = findClosestPointOnPolygon(point, nearestPolygon);

                // Vector from coord to closest point on neighbor
                double toNeighborX = closestPointOnNeighbor.x - coord.x;
                double toNeighborY = closestPointOnNeighbor.y - coord.y;
                double toNeighborLength = Math.sqrt(toNeighborX * toNeighborX + toNeighborY * toNeighborY);

                if (toNeighborLength > 0) {
                    // Normalize
                    toNeighborX /= toNeighborLength;
                    toNeighborY /= toNeighborLength;

                    // Check if neighbor is in outward direction (dot product > 0)
                    double dotProduct = outwardX * toNeighborX + outwardY * toNeighborY;

                    if (dotProduct > 0) {
                        // Calculate midpoint
                        Coordinate expandedCoord = new Coordinate(
                                (coord.x + closestPointOnNeighbor.x) / 2.0,
                                (coord.y + closestPointOnNeighbor.y) / 2.0
                        );

                        // Check if the new point is inside the original polygon
                        Point expandedPoint = geometryFactory.createPoint(expandedCoord);
                        if (!currentPolygon.contains(expandedPoint)) {
                            // Valid expansion found, prefer the one with highest dot product (most aligned with outward direction)
                            if (dotProduct > bestDotProduct) {
                                bestDotProduct = dotProduct;
                                bestExpandedCoord = expandedCoord;
                            }
                        }
                    }
                }
            }

            // Use the best expansion found, or keep original if none worked
            if (bestExpandedCoord != null) {
                expandedCoords.add(bestExpandedCoord);
            } else {
                expandedCoords.add(coord);
            }
        }

        // Close the ring
        if (!expandedCoords.get(0).equals2D(expandedCoords.get(expandedCoords.size() - 1))) {
            expandedCoords.add(expandedCoords.get(0));
        }

        LinearRing ring = geometryFactory.createLinearRing(expandedCoords.toArray(new Coordinate[0]));
        return geometryFactory.createPolygon(ring);
    }

    private static Coordinate findClosestPointOnPolygon(Point point, Polygon polygon) {
        Geometry boundary = polygon.getBoundary();
        Coordinate closestPoint = boundary.getCoordinate();
        double minDistance = point.distance(boundary.getFactory().createPoint(closestPoint));

        for (Coordinate coord : boundary.getCoordinates()) {
            double distance = point.distance(boundary.getFactory().createPoint(coord));
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = coord;
            }
        }

        return closestPoint;
    }

    private static Point[] coordinatesToPoints(int pointCount, List<Coordinate> coordinates, GeometryFactory geometryFactory) {
        Point[] points = new Point[pointCount];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = geometryFactory.createPoint(coordinates.get(i));
        }
        return points;
    }

    private record ClusterableCoordinate(Coordinate coordinate) implements Clusterable {
        @Override
        public double[] getPoint() {
            return new double[]{coordinate.x, coordinate.y};
        }
    }

private static class PolygonDistance {
    Polygon polygon;
    double distance;

    PolygonDistance(Polygon polygon, double distance) {
        this.polygon = polygon;
        this.distance = distance;
    }
}
}
