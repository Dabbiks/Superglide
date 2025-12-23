package com.dabbiks.superglide.game.world.pathfinder;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathSmoother {

    public static HashMap<Integer, Location> smoothPath(HashMap<Integer, Location> originalPath, int pointsPerSegment) {
        if (originalPath == null || originalPath.isEmpty()) return new HashMap<>();

        List<Location> rawPoints = new ArrayList<>();
        for (int i = 0; i < originalPath.size(); i++) {
            if (originalPath.containsKey(i)) rawPoints.add(originalPath.get(i));
        }

        if (rawPoints.size() < 4) return originalPath;

        List<Location> controlPoints = new ArrayList<>(rawPoints);

        controlPoints.add(0, rawPoints.get(rawPoints.size() - 1));

        controlPoints.add(rawPoints.get(0));
        controlPoints.add(rawPoints.get(1));

        HashMap<Integer, Location> smoothMap = new HashMap<>();
        int globalIndex = 0;

        for (int i = 0; i < controlPoints.size() - 3; i++) {
            Location p0 = controlPoints.get(i);
            Location p1 = controlPoints.get(i + 1);
            Location p2 = controlPoints.get(i + 2);
            Location p3 = controlPoints.get(i + 3);

            for (int t = 0; t < pointsPerSegment; t++) {
                double time = t / (double) pointsPerSegment;

                Location splinedLoc = calculateCatmullRom(time, p0, p1, p2, p3);
                smoothMap.put(globalIndex++, splinedLoc);
            }
        }

        smoothMap.put(globalIndex++, controlPoints.get(controlPoints.size() - 2));

        int lookAhead = pointsPerSegment;
        int totalPoints = smoothMap.size();

        for (int i = 0; i < totalPoints; i++) {
            Location current = smoothMap.get(i);
            Location target = smoothMap.get((i + lookAhead) % totalPoints);

            Vector dir = target.toVector().subtract(current.toVector());

            if (dir.lengthSquared() > 0.001) {
                current.setDirection(dir.normalize());
            }
        }

        return smoothMap;
    }

    private static Location calculateCatmullRom(double t, Location p0, Location p1, Location p2, Location p3) {
        double t2 = t * t;
        double t3 = t2 * t;

        double x = 0.5 * ((2 * p1.getX()) + (-p0.getX() + p2.getX()) * t +
                (2 * p0.getX() - 5 * p1.getX() + 4 * p2.getX() - p3.getX()) * t2 +
                (-p0.getX() + 3 * p1.getX() - 3 * p2.getX() + p3.getX()) * t3);

        double y = 0.5 * ((2 * p1.getY()) + (-p0.getY() + p2.getY()) * t +
                (2 * p0.getY() - 5 * p1.getY() + 4 * p2.getY() - p3.getY()) * t2 +
                (-p0.getY() + 3 * p1.getY() - 3 * p2.getY() + p3.getY()) * t3);

        double z = 0.5 * ((2 * p1.getZ()) + (-p0.getZ() + p2.getZ()) * t +
                (2 * p0.getZ() - 5 * p1.getZ() + 4 * p2.getZ() - p3.getZ()) * t2 +
                (-p0.getZ() + 3 * p1.getZ() - 3 * p2.getZ() + p3.getZ()) * t3);

        return new Location(p1.getWorld(), x, y, z);
    }
}