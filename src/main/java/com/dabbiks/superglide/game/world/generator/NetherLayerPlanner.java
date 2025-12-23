package com.dabbiks.superglide.game.world.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import com.dabbiks.superglide.game.world.generator.WorldGenManager.IslandData;

import java.util.List;
import java.util.Random;

public class NetherLayerPlanner {

    private static final int NETHER_CENTER_Y = 110;
    private static final int NETHER_MAIN_RADIUS = 100;

    private static final int MAX_SIDE_ISLAND_SIZE = 60;
    private static final int MIN_SIDE_ISLAND_SIZE = 15;

    private static final int GENERATION_ATTEMPTS = 50000;
    private static final int ISLAND_SPACING = 1;

    public static void planNetherIslands(List<IslandData> islands, World world, int maxRadius, Random random) {
        islands.clear();
        Location centerLoc = new Location(world, 0, NETHER_CENTER_Y, 0);

        islands.add(new IslandData(centerLoc, NETHER_MAIN_RADIUS, Material.RED_WOOL));

        double safeZoneDist = NETHER_MAIN_RADIUS + 10;

        for (int i = 0; i < GENERATION_ATTEMPTS; i++) {
            int x = random.nextInt(maxRadius * 2) - maxRadius;
            int z = random.nextInt(maxRadius * 2) - maxRadius;

            double distFromCenter = Math.sqrt(x * x + z * z);

            if (distFromCenter > maxRadius) continue;
            if (distFromCenter < safeZoneDist) continue;

            int y = NETHER_CENTER_Y + (random.nextInt(11) - 5);

            double normalizedDist = (distFromCenter - safeZoneDist) / (maxRadius - safeZoneDist);
            if (normalizedDist < 0) normalizedDist = 0;
            if (normalizedDist > 1) normalizedDist = 1;

            int currentSize = (int) (MAX_SIDE_ISLAND_SIZE - (normalizedDist * (MAX_SIDE_ISLAND_SIZE - MIN_SIDE_ISLAND_SIZE)));

            Location candidateLoc = new Location(world, x, y, z);

            if (hasSpace(candidateLoc, currentSize, islands)) {
                islands.add(new IslandData(candidateLoc, currentSize, Material.RED_WOOL));
            }
        }
    }

    private static boolean hasSpace(Location loc, int radius, List<IslandData> islands) {
        for (IslandData existing : islands) {
            double dist = existing.center.distance(loc);
            if (dist < existing.radius + radius + ISLAND_SPACING) {
                return false;
            }
        }
        return true;
    }
}
