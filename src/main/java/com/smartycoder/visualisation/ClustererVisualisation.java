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
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
            // Using length ratio (scale-free parameter)
            Polygon clusterPolygon = (Polygon) ConcaveHull.concaveHullByLengthRatio(clusterMultiPoint, 0.65);
            // SEE PolygonHullSimplifier https://lin-ear-th-inking.blogspot.com/2022/04/outer-and-inner-concave-polygon-hulls.html
//            Polygon clusterPolygon = (Polygon) clusterMultiPoint.convexHull();
            clusterPolygons.add(clusterPolygon);
        }

        List<DrawingCommand> drawingCommands = new ArrayList<>();
        for (Polygon clusterPolygon : clusterPolygons) {
            drawingCommands.add(new DrawPolygon(clusterPolygon, Color.RED, null, null));
        }

        // Expand polygons to midpoints between neighbors
        List<Polygon> expandedPolygons = expandPolygons(clusterPolygons, geometryFactory);
        // repeat for better coverage (this feels like a hack)
//        expandedPolygons = expandPolygonsToMidpoints(expandedPolygons, geometryFactory);
//        expandedPolygons = expandPolygonsToMidpoints(expandedPolygons, geometryFactory);
//        expandedPolygons = expandPolygonsToMidpoints(expandedPolygons, geometryFactory);

        for (Polygon expandedPolygon : expandedPolygons) {
            drawingCommands.add(new DrawPolygon(expandedPolygon, Color.BLUE, null, null));
        }

        drawingCommands.add(new DrawMultiPoint(multiPoint, Color.WHITE, null));

        show("cluster", drawingCommands.toArray(new DrawingCommand[0]));
        saveAsFile(Path.of("cluster.png"), drawingCommands.toArray(new DrawingCommand[0]));
    }

    private static List<Polygon> expandPolygons(List<Polygon> polygons, GeometryFactory geometryFactory) {
        List<Polygon> expandedPolygons = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon currentPolygon = polygons.get(i);
            double desiredSegmentLength = currentPolygon.getLength() / 12;
            Coordinate[] coords = currentPolygon.getExteriorRing().getCoordinates();

            // Split each edge into smaller segments
            List<Coordinate> newCoords = new ArrayList<>();
            for (int j = 0; j < coords.length - 1; j++) {
                Coordinate start = coords[j];
                Coordinate end = coords[j + 1];

                newCoords.add(start);

                int midpointCount = (int) Math.ceil(start.distance(end) / desiredSegmentLength);

                // Add midpoints along the edge
                for (int k = 1; k < midpointCount; k++) {
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
        List<Double> expansionDistances = new ArrayList<>();

        // Get the center of the current polygon
        Coordinate centerCoord = currentPolygon.getCentroid().getCoordinate();

        for (Coordinate coord : coords) {
            Point point = geometryFactory.createPoint(coord);

            // Vector from center to this coordinate (outward direction)
            double outwardX = coord.x - centerCoord.x;
            double outwardY = coord.y - centerCoord.y;
            double outwardLength = coord.distance(centerCoord);

            if (outwardLength == 0) {
                expandedCoords.add(coord);
                expansionDistances.add(0.0);
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
            double bestExpansionDistance = 0;

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
                                bestExpansionDistance = toNeighborLength / 2.0;
                            }
                        }
                    }
                }
            }

            // Use the best expansion found, or keep original if none worked
            if (bestExpandedCoord != null) {
                expandedCoords.add(bestExpandedCoord);
                expansionDistances.add(bestExpansionDistance);
            } else {
                expandedCoords.add(coord);
                expansionDistances.add(0.0);
            }
        }

        // Calculate average expansion distance for points that didn't expand
        double averageExpansion = expansionDistances.stream()
                .filter(d -> d > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Apply average expansion to points that couldn't find a neighbor
        List<Coordinate> finalCoords = new ArrayList<>();
        for (int i = 0; i < expandedCoords.size(); i++) {
            Coordinate coord = expandedCoords.get(i);
            double expansion = expansionDistances.get(i);

            if (expansion == 0.0 && averageExpansion > 0) {
                // This point didn't expand, use average expansion in outward direction
                Coordinate centerToCoord = new Coordinate(
                        coord.x - centerCoord.x,
                        coord.y - centerCoord.y
                );
                double distance = Math.sqrt(centerToCoord.x * centerToCoord.x + centerToCoord.y * centerToCoord.y);

                if (distance > 0) {
                    // Normalize and expand by average distance
                    centerToCoord.x /= distance;
                    centerToCoord.y /= distance;

                    Coordinate expandedCoord = new Coordinate(
                            coord.x + centerToCoord.x * averageExpansion,
                            coord.y + centerToCoord.y * averageExpansion
                    );
                    finalCoords.add(expandedCoord);
                } else {
                    finalCoords.add(coord);
                }
            } else {
                finalCoords.add(coord);
            }
        }

        // Close the ring
        if (!finalCoords.get(0).equals2D(finalCoords.get(finalCoords.size() - 1))) {
            finalCoords.add(finalCoords.get(0));
        }

        LinearRing ring = geometryFactory.createLinearRing(finalCoords.toArray(new Coordinate[0]));
        Polygon polygon = geometryFactory.createPolygon(ring);

        polygon = simplifyPolygon(polygon, currentPolygon, allPolygons, currentIndex);

        return polygon;
    }

    private static Polygon simplifyPolygon(Polygon polygon, Polygon originalPolygon,
                                           List<Polygon> allPolygons, int currentIndex) {
        // Simplify with a reasonable tolerance
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(polygon);
        simplifier.setDistanceTolerance(5.0); // Adjust for more/less smoothing
        Geometry simplified = simplifier.getResultGeometry();

        if (!(simplified instanceof Polygon simplifiedPolygon)) {
            return polygon;
        }

        // Union with original to ensure it's at least as large
        Geometry unioned = simplifiedPolygon.union(originalPolygon);
        if (!(unioned instanceof Polygon unionedPolygon)) {
            return polygon;
        }

        // Difference with all neighbor polygons to ensure no overlap
        Polygon result = unionedPolygon;
        for (int i = 0; i < allPolygons.size(); i++) {
            if (i == currentIndex) continue;

            Polygon neighbor = allPolygons.get(i);
            Geometry differenced = result.difference(neighbor);

            if (differenced instanceof Polygon diffPolygon) {
                result = diffPolygon;
            }
        }

        return result;
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
