package com.smartycoder.visualisation;

import com.smartycoder.ui.DrawPoint;
import com.smartycoder.ui.DrawPolygon;
import com.smartycoder.ui.DrawingCommand;
import com.smartycoder.ui.VisualisationUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.quadtree.Quadtree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @see https://www.smartycoder.com
 *
 */
public class QuadtreeVisualisation {

	public static void main(String[] args) {
		GeometryFactory geometryFactory = new GeometryFactory();

		Random randomX = new Random();
		Random randomY = new Random();
		int pointCount = 100;
		Point[] points = new Point[pointCount];
		Quadtree quadTree = new Quadtree();
		for (int i = 1; i <= pointCount; i++) {
			int xRandomlySelected = randomX.nextInt(250) + 65;
			int yRandomlySelected = randomY.nextInt(250) + 50;
			Coordinate coordinate = new Coordinate(xRandomlySelected, yRandomlySelected);
			points[i - 1] = geometryFactory.createPoint(coordinate);
			quadTree.insert(points[i - 1].getEnvelopeInternal(), points[i - 1]);
		}
		Coordinate[] coordinates = {new Coordinate(150, 60), new Coordinate(250, 60), new Coordinate(250, 250), new Coordinate(150, 250), new Coordinate(150, 60)};
		Polygon searchPolygon = geometryFactory.createPolygon(coordinates);
		List<Object> searchPossibleCoveredPoints = quadTree.query(searchPolygon.getEnvelopeInternal());
		List<Point> falsePositives = new ArrayList<>();
		for (Object c : searchPossibleCoveredPoints) {
			Point p = (Point) c;
			System.out.println(p + " intersects polygon: " + searchPolygon.intersects(p) + " envlope " + p.getEnvelopeInternal());
			if (!searchPolygon.intersects(p)) {
				falsePositives.add(p);
			}
		}
		searchPossibleCoveredPoints.removeAll(falsePositives);
		DrawingCommand[] commands = new DrawingCommand[pointCount + 1];
		commands[0] = new DrawPolygon(searchPolygon, Color.RED, null, null);
		for (int i = 0; i < pointCount; i++) {
			Point point = points[i];
			Color color = searchPossibleCoveredPoints.contains(point) ? Color.RED : Color.BLUE;
			commands[i + 1] = new DrawPoint(point, color, null);
		}

		VisualisationUtil.show("JTS Visualisation - QuadTree", commands);
	}

}
