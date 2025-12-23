package com.dabbiks.superglide.game.world.pathfinder;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Pathfinder {

    private static final int SCAN_MIN_Y = 120;
    private static final int SCAN_MAX_Y = 319;
    private static final int OBSTACLE_PADDING = 3;

    // Ustawienia dynamicznej wysokości
    private static final int MIN_CLEARANCE = 4;
    private static final int SMOOTHING_PASSES = 20;

    public static CompletableFuture<HashMap<Integer, Location>> generatePathAsync(int minR, int maxR, int hAtMin, int hAtMax) {
        ConsoleLogger.info(ConsoleLogger.Type.PATHFINDER, "Pathfinding started for values " + minR + " " + maxR + " " + hAtMin + " " + hAtMax);

        World world = Constants.world;

        Map<Long, ChunkSnapshot> snapshots = new HashMap<>();
        int mapMargin = 64;
        int boundsMinX = -maxR - mapMargin;
        int boundsMaxX = maxR + mapMargin;
        int boundsMinZ = -maxR - mapMargin;
        int boundsMaxZ = maxR + mapMargin;

        try {
            int minChunkX = boundsMinX >> 4;
            int maxChunkX = boundsMaxX >> 4;
            int minChunkZ = boundsMinZ >> 4;
            int maxChunkZ = boundsMaxZ >> 4;

            ConsoleLogger.info(ConsoleLogger.Type.PATHFINDER, "Loading chunk snapshots");
            for (int x = minChunkX; x <= maxChunkX; x++) {
                for (int z = minChunkZ; z <= maxChunkZ; z++) {
                    double centerX = x * 16 + 8;
                    double centerZ = z * 16 + 8;
                    double dist = Math.sqrt(centerX*centerX + centerZ*centerZ);
                    if (dist > maxR + 50 || dist < minR - 50) continue;

                    snapshots.put(asLong(x, z), world.getChunkAt(x, z).getChunkSnapshot(false, false, false));
                }
            }
            ConsoleLogger.info(ConsoleLogger.Type.PATHFINDER, "Chunk snapshots ready (" + snapshots.size() + ")");

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(new HashMap<>());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                int width = boundsMaxX - boundsMinX + 1;
                int height = boundsMaxZ - boundsMinZ + 1;

                boolean[][] rawGrid = new boolean[width][height];
                boolean[][] strictGrid = new boolean[width][height];
                int[][] heightMap = new int[width][height];
                for (int[] row : heightMap) Arrays.fill(row, -100);

                for (int x = boundsMinX; x <= boundsMaxX; x++) {
                    for (int z = boundsMinZ; z <= boundsMaxZ; z++) {

                        double distSq = (double)x*x + (double)z*z;
                        boolean outOfBounds = distSq > (maxR + 15)*(maxR + 15) || distSq < (minR - 15)*(minR - 15);

                        int gx = x - boundsMinX;
                        int gz = z - boundsMinZ;

                        int highestBlockY = getHighestBlockY(x, z, snapshots);
                        heightMap[gx][gz] = highestBlockY;

                        if (outOfBounds) {
                            rawGrid[gx][gz] = true;
                            strictGrid[gx][gz] = true;
                            continue;
                        }

                        if (highestBlockY >= SCAN_MIN_Y) {
                            rawGrid[gx][gz] = true;
                            markInflatedObstacle(strictGrid, gx, gz, width, height, OBSTACLE_PADDING);
                        }
                    }
                }

                boolean[][] lenientGrid = new boolean[width][height];
                for(int i=0; i<width; i++) System.arraycopy(rawGrid[i], 0, lenientGrid[i], 0, height);
                inflateGrid(lenientGrid, rawGrid, width, height, 1);

                ConsoleLogger.info(ConsoleLogger.Type.PATHFINDER, "Maps ready - Initiating A*");

                List<Pathfinder.Point> waypoints = new ArrayList<>();
                double avgR = (minR + maxR) / 2.0;
                int segments = 40;

                for (int i = 0; i < segments; i++) {
                    double angle = Math.toRadians(i * (360.0 / segments));
                    int wx = (int) (avgR * Math.cos(angle));
                    int wz = (int) (avgR * Math.sin(angle));

                    Pathfinder.Point validP = findNearestFreePoint(wx, wz, strictGrid, boundsMinX, boundsMinZ, width, height);
                    if (validP == null) validP = findNearestFreePoint(wx, wz, rawGrid, boundsMinX, boundsMinZ, width, height);

                    if (validP != null) waypoints.add(validP);
                }
                if (!waypoints.isEmpty()) waypoints.add(waypoints.get(0));

                if (waypoints.size() < segments / 2) {
                    ConsoleLogger.warning(ConsoleLogger.Type.PATHFINDER, "Pathfinder could not find the path!");
                    return new HashMap<>();
                }

                List<Pathfinder.PathPoint> rawPath = new ArrayList<>();

                for (int i = 0; i < waypoints.size() - 1; i++) {
                    Pathfinder.Point start = waypoints.get(i);
                    Pathfinder.Point end = waypoints.get(i + 1);
                    List<Pathfinder.PathPoint> segmentPath;

                    segmentPath = findPathAStar2D(start, end, strictGrid, boundsMinX, boundsMinZ, width, height, false);

                    if (segmentPath.isEmpty()) {
                        segmentPath = findPathAStar2D(start, end, lenientGrid, boundsMinX, boundsMinZ, width, height, false);
                    }

                    if (segmentPath.isEmpty()) {
                        ConsoleLogger.warning(ConsoleLogger.Type.PATHFINDER, "Segment " + i + " is too big - trying to fly over it");
                        segmentPath = findPathAStar2D(start, end, rawGrid, boundsMinX, boundsMinZ, width, height, true);
                    }

                    if (segmentPath.isEmpty()) {
                        segmentPath = generateLineForced(start, end);
                    }

                    rawPath.addAll(segmentPath);
                }

                HashMap<Integer, Location> finalMap = applyPlateauSmoothing(rawPath, world, minR, maxR, hAtMin, hAtMax, heightMap, boundsMinX, boundsMinZ);

                ConsoleLogger.warning(ConsoleLogger.Type.PATHFINDER, "Pathfinding completed (" + finalMap.size() + " points)");
                return finalMap;

            } catch (Exception e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        });
    }

    private static HashMap<Integer, Location> applyPlateauSmoothing(
            List<Pathfinder.PathPoint> path, World world, int minR, int maxR, int hMin, int hMax,
            int[][] terrainMap, int minX, int minZ) {

        int size = path.size();
        double[] targetHeights = new double[size];
        int[] pointTerrainY = new int[size];

        for (int i = 0; i < size; i++) {
            Pathfinder.PathPoint p = path.get(i);

            double r = Math.sqrt(p.x * p.x + p.z * p.z);
            targetHeights[i] = calculateIdealHeight(r, minR, maxR, hMin, hMax);

            int gx = p.x - minX;
            int gz = p.z - minZ;
            int ty = -100;
            if (gx >= 0 && gx < terrainMap.length && gz >= 0 && gz < terrainMap[0].length) {
                ty = terrainMap[gx][gz];
            }
            pointTerrainY[i] = ty;
        }

        for (int i = 0; i < size; i++) {
            if (path.get(i).forced) {
                int startIdx = i;
                int endIdx = i;
                while (endIdx < size && path.get(endIdx).forced) endIdx++;

                int maxSegmentTerrainY = -100;
                for (int k = startIdx; k < endIdx; k++) {
                    if (pointTerrainY[k] > maxSegmentTerrainY) maxSegmentTerrainY = pointTerrainY[k];
                }

                double plateauHeight = maxSegmentTerrainY + MIN_CLEARANCE;
                for (int k = startIdx; k < endIdx; k++) {
                    if (plateauHeight > targetHeights[k]) targetHeights[k] = plateauHeight;
                }
                i = endIdx;
            }
        }

        double[] smoothedHeights = targetHeights.clone();
        for (int pass = 0; pass < SMOOTHING_PASSES; pass++) {
            double[] buffer = new double[size];
            for (int i = 0; i < size; i++) {
                int prev = (i - 1 + size) % size;
                int next = (i + 1) % size;
                buffer[i] = (smoothedHeights[prev] + smoothedHeights[i] + smoothedHeights[next]) / 3.0;
            }
            smoothedHeights = buffer;
        }

        for (int i = 0; i < size; i++) {
            double minSafeY = pointTerrainY[i] + MIN_CLEARANCE;
            if (smoothedHeights[i] < minSafeY) {
                smoothedHeights[i] = minSafeY;
            }
        }

        HashMap<Integer, Location> resultMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Pathfinder.PathPoint p = path.get(i);
            resultMap.put(i, new Location(world, p.x, smoothedHeights[i], p.z));
        }

        return resultMap;
    }


    private static int getHighestBlockY(int x, int z, Map<Long, ChunkSnapshot> snapshots) {
        long key = asLong(x >> 4, z >> 4);
        ChunkSnapshot snap = snapshots.get(key);
        if (snap == null) return -100;

        for (int y = SCAN_MAX_Y; y >= SCAN_MIN_Y; y--) {
            Material type = snap.getBlockType(x & 15, y, z & 15);
            if (!type.isAir()) return y;
        }
        return -100;
    }

    private static boolean isColumnBlocked(int x, int z, Map<Long, ChunkSnapshot> snapshots) {
        int highest = getHighestBlockY(x, z, snapshots);
        return highest >= SCAN_MIN_Y;
    }

    private static void markInflatedObstacle(boolean[][] grid, int gx, int gz, int w, int h, int padding) {
        for (int dx = -padding; dx <= padding; dx++) {
            for (int dz = -padding; dz <= padding; dz++) {
                int nx = gx + dx;
                int nz = gz + dz;
                if (nx >= 0 && nx < w && nz >= 0 && nz < h) grid[nx][nz] = true;
            }
        }
    }

    private static void inflateGrid(boolean[][] target, boolean[][] source, int w, int h, int padding) {
        for(int x=0; x<w; x++) {
            for(int z=0; z<h; z++) {
                if(source[x][z]) markInflatedObstacle(target, x, z, w, h, padding);
            }
        }
    }

    private static List<Pathfinder.PathPoint> findPathAStar2D(Pathfinder.Point start, Pathfinder.Point end, boolean[][] grid, int minX, int minZ, int w, int h, boolean forceMode) {
        PriorityQueue<Pathfinder.Node2D> openSet = new PriorityQueue<>();
        boolean[][] visited = new boolean[w][h];

        Pathfinder.Node2D startNode = new Pathfinder.Node2D(start.x, start.z, null, 0, start.dist(end), false);
        openSet.add(startNode);

        int iterations = 0;
        int MAX_ITER = 15000;

        while (!openSet.isEmpty()) {
            if (iterations++ > MAX_ITER) break;

            Pathfinder.Node2D current = openSet.poll();
            int gx = current.x - minX;
            int gz = current.z - minZ;

            if (gx < 0 || gx >= w || gz < 0 || gz >= h) continue;
            if (visited[gx][gz]) continue;
            visited[gx][gz] = true;

            if (current.dist(end) < 2.0) {
                return reconstruct(current);
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    int nx = current.x + dx;
                    int nz = current.z + dz;
                    int ngx = nx - minX;
                    int ngz = nz - minZ;

                    if (ngx < 0 || ngx >= w || ngz < 0 || ngz >= h) continue;

                    boolean isBlocked = grid[ngx][ngz];
                    if (!forceMode && isBlocked) continue;

                    double terrainCost = isBlocked ? 50.0 : 0.0;
                    double moveCost = ((dx != 0 && dz != 0) ? 1.414 : 1.0) + terrainCost;
                    double newG = current.g + moveCost;
                    double newH = Math.sqrt(Math.pow(nx - end.x, 2) + Math.pow(nz - end.z, 2));

                    openSet.add(new Pathfinder.Node2D(nx, nz, current, newG, newH, isBlocked));
                }
            }
        }
        return new ArrayList<>();
    }

    private static class Point {
        int x, z;
        Point(int x, int z) { this.x = x; this.z = z; }
        double dist(Pathfinder.Point o) { return Math.sqrt(Math.pow(x-o.x, 2) + Math.pow(z-o.z, 2)); }
    }

    private static class PathPoint extends Pathfinder.Point {
        boolean forced;
        PathPoint(int x, int z, boolean forced) { super(x, z); this.forced = forced; }
    }

    private static class Node2D implements Comparable<Pathfinder.Node2D> {
        int x, z;
        Pathfinder.Node2D parent;
        double g, h;
        boolean forced;

        Node2D(int x, int z, Pathfinder.Node2D p, double g, double h, boolean forced) {
            this.x = x; this.z = z; this.parent = p; this.g = g; this.h = h; this.forced = forced;
        }

        @Override public int compareTo(Pathfinder.Node2D o) { return Double.compare(g+h, o.g+o.h); }
        double dist(Pathfinder.Point p) { return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(z - p.z, 2)); }
    }

    private static Pathfinder.Point findNearestFreePoint(int tx, int tz, boolean[][] grid, int minX, int minZ, int w, int h) {
        for (int r = 0; r < 40; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int nx = tx + dx;
                    int nz = tz + dz;
                    int gx = nx - minX;
                    int gz = nz - minZ;
                    if (gx >= 0 && gx < w && gz >= 0 && gz < h && !grid[gx][gz]) return new Pathfinder.Point(nx, nz);
                }
            }
        }
        return null;
    }

    private static List<Pathfinder.PathPoint> reconstruct(Pathfinder.Node2D end) {
        List<Pathfinder.PathPoint> path = new ArrayList<>();
        Pathfinder.Node2D cur = end;
        while (cur != null) {
            path.add(new Pathfinder.PathPoint(cur.x, cur.z, cur.forced));
            cur = cur.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static List<Pathfinder.PathPoint> generateLineForced(Pathfinder.Point start, Pathfinder.Point end) {
        List<Pathfinder.PathPoint> line = new ArrayList<>();
        double dist = start.dist(end);
        double dx = (end.x - start.x) / dist;
        double dz = (end.z - start.z) / dist;
        for (double d = 0; d <= dist; d += 1.0) {
            line.add(new Pathfinder.PathPoint((int)(start.x + dx*d), (int)(start.z + dz*d), true));
        }
        return line;
    }

    private static double calculateIdealHeight(double r, int minR, int maxR, int hMin, int hMax) {
        double ratio = (r - minR) / (double) (maxR - minR);
        ratio = Math.max(0, Math.min(1, ratio));
        return hMin + (ratio * (hMax - hMin));
    }

    private static long asLong(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }
    
}
