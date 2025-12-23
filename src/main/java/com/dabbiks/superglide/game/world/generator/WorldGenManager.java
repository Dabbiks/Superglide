package com.dabbiks.superglide.game.world.generator;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dabbiks.superglide.Superglide.plugin;

public class WorldGenManager {

    // * CONFIG

    private final int MAP_RADIUS = 400;
    private final int CENTER_Y = 200;
    private final int CLEAN_RADIUS = 500;
    private final int CLEAN_MIN_Y = 0;
    private final int CLEAN_MAX_Y = 250;

    // * TOP LAYER
    private final int TOP_MAIN_ISLAND_RADIUS = 180;
    private final int SAFE_ZONE_BUFFER = 10;
    private final int ISLAND_SPACING = 1;
    private final int GENERATION_ATTEMPTS = 300000;
    private final int MIN_ISLAND_RADIUS = 25;
    private final int MAX_ISLAND_ADDITIONAL_SIZE = 80;
    private final double OUTLIER_CHANCE = 0.05;
    private final int OUTLIER_BONUS = 40;
    private final int MAX_SEARCH_RADIUS = 85;
    private final double MAX_Y_DROP = 40.0;

    // * BRIDGES (DISABLED)
    private final double BRIDGE_CHANCE = 0.0;
    private final int MAX_BRIDGE_LENGTH = 120;
    private final int MIN_BRIDGE_LENGTH = 15;
    private final double BRIDGE_SEGMENT_STEP = 0.6;
    private final double BRIDGE_SAG_AMOUNT = 2.5;
    private final double BRIDGE_NOISE_XZ = 0.0;
    private final double BRIDGE_RADIUS_BASE = 0.1;

    // * EFFICIENCY
    private final int BLOCKS_PER_TICK = 24000;
    private final long CLEANING_TIME_LIMIT_MS = 40;
    private final long DELAY_BEFORE_GENERATION = 20;
    private final long DELAY_BEFORE_BIOMES = 20;

    // * ------------------------------------------------------------------------

    private final Queue<BlockSetData> blockQueue = new ConcurrentLinkedQueue<>();
    private final List<IslandData> topIslands = new ArrayList<>();
    private final List<IslandData> netherIslands = new ArrayList<>();

    public void startProcess() {
        ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Initiating terrain cleaner");
        clearAreaAndThenGenerate();
    }

    private void clearAreaAndThenGenerate() {
        World world = Constants.world;
        new TerrainCleaner(world).runTaskTimer(plugin, 0L, 1L);
    }

    private class TerrainCleaner extends BukkitRunnable {
        private final World world;
        private int x = -CLEAN_RADIUS;
        private int z = -CLEAN_RADIUS;
        private int tickCounter = 0;

        TerrainCleaner(World world) {
            this.world = world;
        }

        @Override
        public void run() {
            if (tickCounter % 200 == 0) {
                removeAllItems(world);
            }
            tickCounter++;

            long startTime = System.currentTimeMillis();
            processCleaningBatch(startTime);
        }

        private void processCleaningBatch(long startTime) {
            while (System.currentTimeMillis() - startTime < CLEANING_TIME_LIMIT_MS) {
                cleanColumn(x, z);

                z++;
                if (z > CLEAN_RADIUS) {
                    z = -CLEAN_RADIUS;
                    x++;
                }

                if (x > CLEAN_RADIUS) {
                    finishCleaning();
                    return;
                }
            }
        }

        private void cleanColumn(int cx, int cz) {
            for (int y = CLEAN_MIN_Y; y <= CLEAN_MAX_Y; y++) {
                if (world.getBlockAt(cx, y, cz).getType() != Material.AIR) {
                    world.getBlockAt(cx, y, cz).setType(Material.AIR);
                }
            }
        }

        private void finishCleaning() {
            this.cancel();
            removeAllItems(world);
            ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Terrain cleaner successfully deleted map");
            Bukkit.getScheduler().runTaskLater(plugin, WorldGenManager.this::startGeneration, DELAY_BEFORE_GENERATION);
        }
    }

    private void removeAllItems(World world) {
        for (Entity entity : world.getEntitiesByClass(Item.class)) {
            entity.remove();
        }
    }

    private void startGeneration() {
        ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Initiating island generator");
        World world = Constants.world;

        new BukkitRunnable() {
            @Override
            public void run() {
                executeGenerationLogic(world);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void executeGenerationLogic(World world) {
        Location center = new Location(world, 0, CENTER_Y, 0);
        Random random = new Random();

        planTopIslands(center, random);

        int netherRadius = MAP_RADIUS / 2;
        NetherLayerPlanner.planNetherIslands(netherIslands, world, netherRadius, random);

        queueIslandsGeneration();
        generateSolidBridges(topIslands, random);

        startPlacingBlocks(world);
    }

    private void queueIslandsGeneration() {
        for (IslandData island : topIslands) {
            IslandShapeGenerator.generateToQueue(blockQueue, island.center, island.radius, island.material);
        }
        for (IslandData island : netherIslands) {
            IslandShapeGenerator.generateToQueue(blockQueue, island.center, island.radius, island.material);
        }
    }

    private void planTopIslands(Location centerLoc, Random random) {
        topIslands.clear();
        topIslands.add(new IslandData(centerLoc, TOP_MAIN_ISLAND_RADIUS, Material.WHITE_WOOL));

        double safeZoneDist = TOP_MAIN_ISLAND_RADIUS + SAFE_ZONE_BUFFER;

        for (int i = 0; i < GENERATION_ATTEMPTS; i++) {
            attemptSingleIslandPlan(centerLoc, random, safeZoneDist);
        }
    }

    private void attemptSingleIslandPlan(Location centerLoc, Random random, double safeZoneDist) {
        int range = MAP_RADIUS + 20;
        int x = random.nextInt(range * 2) - range;
        int z = random.nextInt(range * 2) - range;

        double distFromCenter = Math.sqrt(x * x + z * z);

        if (distFromCenter > MAP_RADIUS) return;
        if (distFromCenter < safeZoneDist) return;

        double drop = (distFromCenter / MAP_RADIUS) * MAX_Y_DROP;
        int y = CENTER_Y - (int) drop + (random.nextInt(7) - 3);
        Location candidateLoc = new Location(centerLoc.getWorld(), x, y, z);

        int finalRadius = calculateIslandRadius(distFromCenter, safeZoneDist, random, candidateLoc);

        if (finalRadius >= 8) {
            topIslands.add(new IslandData(candidateLoc, finalRadius - ISLAND_SPACING, Material.WHITE_WOOL));
        }
    }

    private int calculateIslandRadius(double distFromCenter, double safeZoneDist, Random random, Location candidateLoc) {
        double distScale = Math.min(1.0, (distFromCenter - safeZoneDist) / (MAP_RADIUS - safeZoneDist));
        double sizeMultiplier = 1.0 - distScale;

        int desiredRadius = MIN_ISLAND_RADIUS + (int)(MAX_ISLAND_ADDITIONAL_SIZE * sizeMultiplier);
        if (random.nextDouble() < OUTLIER_CHANCE) desiredRadius += OUTLIER_BONUS;

        int maxAllowedRadius = getSpaceAvailable(candidateLoc, topIslands);
        return Math.min(desiredRadius, maxAllowedRadius);
    }

    private int getSpaceAvailable(Location loc, List<IslandData> islandList) {
        double closestDist = Double.MAX_VALUE;
        for (IslandData existing : islandList) {
            double dist = existing.center.distance(loc);
            double distToEdge = dist - existing.radius;
            if (distToEdge < closestDist) closestDist = distToEdge;
        }
        return (int) Math.min(MAX_SEARCH_RADIUS, closestDist);
    }

    private void generateSolidBridges(List<IslandData> islandList, Random random) {
        for (IslandData current : islandList) {
            attemptBridgeFromIsland(current, islandList, random);
        }
    }

    private void attemptBridgeFromIsland(IslandData current, List<IslandData> islandList, Random random) {
        if (random.nextDouble() > BRIDGE_CHANCE) return;

        IslandData neighbor = getNearestNeighbor(current, islandList);
        if (neighbor == null) return;

        createSolidBridge(current, neighbor, random);
    }

    private IslandData getNearestNeighbor(IslandData source, List<IslandData> islandList) {
        IslandData nearest = null;
        double minDst = Double.MAX_VALUE;
        double maxDistSq = MAX_BRIDGE_LENGTH * MAX_BRIDGE_LENGTH;
        double minDistSq = MIN_BRIDGE_LENGTH * MIN_BRIDGE_LENGTH;

        for (IslandData other : islandList) {
            if (other == source) continue;

            double dst = source.center.distanceSquared(other.center);

            if (dst >= minDst) continue;
            if (dst >= maxDistSq) continue;
            if (dst <= minDistSq) continue;

            minDst = dst;
            nearest = other;
        }
        return nearest;
    }

    private void createSolidBridge(IslandData startIsland, IslandData endIsland, Random random) {
        Vector p1 = startIsland.center.toVector().clone().subtract(new Vector(0, 3, 0));
        Vector p2 = endIsland.center.toVector().clone().subtract(new Vector(0, 3, 0));
        double distance = p1.distance(p2);
        Vector dir = p2.clone().subtract(p1).normalize();

        for (double d = 0; d < distance; d += BRIDGE_SEGMENT_STEP) {
            processBridgeSegment(p1, dir, d, distance, random);
        }
    }

    private void processBridgeSegment(Vector p1, Vector dir, double d, double distance, Random random) {
        Vector currentPos = p1.clone().add(dir.clone().multiply(d));
        double progress = d / distance;
        double sag = Math.sin(progress * Math.PI) * BRIDGE_SAG_AMOUNT;
        currentPos.setY(currentPos.getY() - sag);

        double noiseX = Math.sin(d * 0.3) * BRIDGE_NOISE_XZ;
        double noiseZ = Math.cos(d * 0.3) * BRIDGE_NOISE_XZ;

        int bx = (int) (currentPos.getX() + noiseX);
        int by = (int) currentPos.getY();
        int bz = (int) (currentPos.getZ() + noiseZ);

        double radius = BRIDGE_RADIUS_BASE + random.nextDouble();
        generateBridgeSegmentBlocks(bx, by, bz, radius);
    }

    private void generateBridgeSegmentBlocks(int cx, int cy, int cz, double radius) {
        int rCeil = (int) Math.ceil(radius);
        double rSq = radius * radius;
        for (int x = -rCeil; x <= rCeil; x++) {
            for (int y = -rCeil; y <= rCeil; y++) {
                for (int z = -rCeil; z <= rCeil; z++) {
                    addBridgeBlockIfInRange(cx, cy, cz, x, y, z, rSq);
                }
            }
        }
    }

    private void addBridgeBlockIfInRange(int cx, int cy, int cz, int x, int y, int z, double rSq) {
        if (x*x + y*y + z*z > rSq) return;
        blockQueue.add(new BlockSetData(cx + x, cy + y, cz + z, Material.OAK_PLANKS));
    }

    private void startPlacingBlocks(World world) {
        new BlockPlacer(world).runTaskTimer(plugin, 0L, 1L);
    }

    private class BlockPlacer extends BukkitRunnable {
        private final World world;

        BlockPlacer(World world) {
            this.world = world;
        }

        @Override
        public void run() {
            processQueueBatch();

            if (blockQueue.isEmpty()) {
                finishPlacement();
            }
        }

        private void processQueueBatch() {
            int processed = 0;
            while (!blockQueue.isEmpty() && processed < BLOCKS_PER_TICK) {
                BlockSetData data = blockQueue.poll();
                if (data != null) placeSingleBlock(data);
                processed++;
            }
        }

        private void placeSingleBlock(BlockSetData data) {
            if (!world.isChunkLoaded(data.x >> 4, data.z >> 4)) {
                world.loadChunk(data.x >> 4, data.z >> 4);
            }
            Material currentType = world.getBlockAt(data.x, data.y, data.z).getType();
            if (currentType == Material.AIR || currentType == Material.OAK_PLANKS) {
                world.getBlockAt(data.x, data.y, data.z).setType(data.material);
            }
        }

        private void finishPlacement() {
            ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Islands successfully generated");
            this.cancel();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new BiomeApplicator().applyBiomes(topIslands);
                new NetherBiomeApplicator().applyNetherBiomes(netherIslands);
            }, DELAY_BEFORE_BIOMES);
        }
    }

    public static class IslandData {
        public Location center;
        public int radius;
        public Material material;
        public IslandData(Location c, int r, Material m) { center = c; radius = r; material = m; }
    }

    public static class BlockSetData {
        public int x, y, z;
        public Material material;
        public BlockSetData(int x, int y, int z, Material m) {
            this.x = x; this.y = y; this.z = z; this.material = m;
        }
    }
}
