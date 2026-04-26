package com.smartycoder.visualisation;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record RandomCoordinates(int minX, int maxX, int minY, int maxY) {
    private static final Random RANDOM = new Random();

    public List<Coordinate> generate(int count) {
        List<Coordinate> coords = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            coords.add(new Coordinate(RANDOM.nextInt(minX, maxX), RANDOM.nextInt(minY, maxY)));
        }
        return coords;
    }
}
