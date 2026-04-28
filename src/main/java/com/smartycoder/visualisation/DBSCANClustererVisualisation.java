package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawMultiPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import org.hipparchus.clustering.CentroidCluster;
import org.hipparchus.clustering.Cluster;
import org.hipparchus.clustering.Clusterable;
import org.hipparchus.clustering.FuzzyKMeansClusterer;
import org.locationtech.jts.coverage.CoverageUnion;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartycoder.ui.VisualisationUtil.saveAsFile;

public class DBSCANClustererVisualisation {

    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();

        int pointCount = 400;
        List<Coordinate> coordinates = new RandomCoordinates(50, 500, 50, 500)
                .generate(pointCount);
        Point[] points = coordinatesToPoints(pointCount, coordinates, geometryFactory);
        MultiPoint multiPoint = geometryFactory.createMultiPoint(points);

        List<CentroidCluster<ClusterableCoordinate>> clusters = new FuzzyKMeansClusterer<ClusterableCoordinate>(
                5, 3)
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
            drawingCommands.add(new DrawPolygon(clusterPolygon, null, Color.GRAY, null));
        }

        // Create Voronoi diagram from ALL points
        VoronoiDiagramBuilder voronoiBuilder = new VoronoiDiagramBuilder();
        voronoiBuilder.setSites(coordinates);
        Geometry voronoiDiagram = voronoiBuilder.getDiagram(geometryFactory);

        // Map each coordinate to its cluster index
        Map<Coordinate, Integer> coordinateToCluster = new HashMap<>();
        for (int clusterIdx = 0; clusterIdx < clusters.size(); clusterIdx++) {
            Cluster<ClusterableCoordinate> cluster = clusters.get(clusterIdx);
            for (ClusterableCoordinate cc : cluster.getPoints()) {
                coordinateToCluster.put(cc.coordinate(), clusterIdx);
            }
        }

        // Group Voronoi cells by cluster and merge them
        @SuppressWarnings("unchecked")
        List<Polygon>[] clusterVoronoiCells = new List[clusters.size()];
        for (int i = 0; i < clusters.size(); i++) {
            clusterVoronoiCells[i] = new ArrayList<>();
        }

        if (voronoiDiagram instanceof GeometryCollection geometryCollection) {
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                Polygon voronoiCell = (Polygon) geometryCollection.getGeometryN(i);
                // Find which cluster this Voronoi cell belongs to
                // Find the contained point in coordinates to determine cluster
                int clusterIdx = 0;
                for (Coordinate coord : coordinates) {
                    if (voronoiCell.contains(geometryFactory.createPoint(coord))) {
                        clusterIdx = coordinateToCluster.getOrDefault(coord, 0);
                        break;
                    }
                }

                clusterVoronoiCells[clusterIdx].add(voronoiCell);
            }
        }

        // Merge Voronoi cells for each cluster
        for (int clusterIdx = 0; clusterIdx < clusters.size(); clusterIdx++) {
            if (!clusterVoronoiCells[clusterIdx].isEmpty()) {
                Geometry g = CoverageUnion.union(clusterVoronoiCells[clusterIdx].toArray(new Polygon[0]));
                if (g instanceof Polygon merged) {
                    drawingCommands.add(new DrawPolygon(merged, Color.BLUE, null, null));
                }
            }
        }

        drawingCommands.add(new DrawMultiPoint(multiPoint, Color.WHITE, null));

        saveAsFile(Path.of("target/cluster.png"), drawingCommands.toArray(new DrawingCommand[0]));

//        show(
//                "JTS Visualisation - Convex Hull",
//                drawingCommands.toArray(new DrawingCommand[0])
//        );
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


}
