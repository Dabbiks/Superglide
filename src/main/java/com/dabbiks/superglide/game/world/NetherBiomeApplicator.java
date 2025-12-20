package com.dabbiks.superglide.game.world;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import com.dabbiks.superglide.game.world.WorldGenManager.IslandData;
import com.dabbiks.superglide.game.world.StructureGenerator.StructureType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dabbiks.superglide.Superglide.plugin;

public class NetherBiomeApplicator {

    // ZAKRES SKANOWANIA
    private final int SCAN_MAX_Y = 139;
    private final int SCAN_MIN_Y = 39;
    private final int SCAN_MARGIN_ADDITION = 85;

    // MATERIAŁY BAZOWE
    private final Material TARGET_WOOL = Material.RED_WOOL;
    private final Material MAT_DEFAULT_BASE = Material.NETHERRACK;

    // MATERIAŁY SOUL SAND VALLEY
    private final Material MAT_SSV_SURFACE = Material.SOUL_SOIL;
    private final Material MAT_SSV_UNDER = Material.SOUL_SAND;
    private final Material MAT_SSV_DEEP = Material.DRIPSTONE_BLOCK;

    // SZANSE DEKORACJI
    private final double CRIMSON_ROOTS = 0.30;
    private final double CRIMSON_FUNGUS = 0.01;
    private final double CRIMSON_TREE = 0.03;

    private final double WARPED_ROOTS = 0.30;
    private final double WARPED_SPROUTS = 0.20;
    private final double WARPED_FUNGUS = 0.01;
    private final double WARPED_TREE = 0.03;

    // SZANSE SOUL SAND VALLEY
    private final double SSV_FIRE_CHANCE = 0.05;
    private final double SSV_BASALT_COLUMN = 0.012;
    private final double SSV_FOSSIL_CHANCE = 0.003;

    // --- NOWE: SZANSA NA PORTAL (NETHER) ---
    private final double CHANCE_PORTAL = 0.003;

    // WYDAJNOŚĆ
    private final int BLOCKS_PER_TICK = 8000;
    private final int TREES_PER_TICK = 4;

    private final Queue<NetherBlockData> blockQueue = new ConcurrentLinkedQueue<>();
    private final Queue<TreeRequest> treeQueue = new ConcurrentLinkedQueue<>();
    private final Queue<StructureRequest> structureQueue = new ConcurrentLinkedQueue<>();

    private final Map<IslandData, NetherBiome> islandBiomes = new HashMap<>();
    private final SchematicPaster paster = new SchematicPaster();
    private final StructureGenerator structureGen = new StructureGenerator();

    private enum NetherBiome {
        CRIMSON_FOREST, WARPED_FOREST, SOUL_SAND_VALLEY
    }

    public void applyNetherBiomes(List<IslandData> islands) {
        // ! DEBUG
        World world = Constants.world;
        Random random = new Random();

        assignBiomesToIslands(islands, random);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (IslandData island : islands) {
                    processIslandStrict(island, islands, world, random);
                }
                applyChanges(world);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void assignBiomesToIslands(List<IslandData> islands, Random random) {
        SimplexOctaveGenerator biomeNoise = new SimplexOctaveGenerator(random, 2);
        biomeNoise.setScale(0.005);

        for (IslandData island : islands) {
            double noiseVal = biomeNoise.noise(island.center.getBlockX(), island.center.getBlockZ(), 0.5, 0.5);
            islandBiomes.put(island, determineBiome(noiseVal));
        }
    }

    private NetherBiome determineBiome(double noiseVal) {
        if (noiseVal < -0.3) return NetherBiome.SOUL_SAND_VALLEY;
        if (noiseVal < 0.3) return NetherBiome.WARPED_FOREST;
        return NetherBiome.CRIMSON_FOREST;
    }

    private void processIslandStrict(IslandData targetIsland, List<IslandData> allIslands, World world, Random random) {
        int cx = targetIsland.center.getBlockX();
        int cz = targetIsland.center.getBlockZ();
        NetherBiome biome = islandBiomes.get(targetIsland);

        int scanRange = targetIsland.radius + SCAN_MARGIN_ADDITION;
        double maxScanDistSq = scanRange * scanRange;

        for (int x = -scanRange; x <= scanRange; x++) {
            for (int z = -scanRange; z <= scanRange; z++) {
                processCoordinateInIsland(x, z, cx, cz, maxScanDistSq, targetIsland, allIslands, world, biome, random);
            }
        }
    }

    private void processCoordinateInIsland(int x, int z, int cx, int cz, double maxScanDistSq, IslandData targetIsland, List<IslandData> allIslands, World world, NetherBiome biome, Random random) {
        if (x * x + z * z > maxScanDistSq) return;

        int realX = cx + x;
        int realZ = cz + z;

        IslandData closest = getClosestIsland(realX, realZ, allIslands, targetIsland);
        if (closest != targetIsland) return;

        processColumn(world, realX, realZ, biome, random);
    }

    private IslandData getClosestIsland(int x, int z, List<IslandData> islands, IslandData currentTarget) {
        IslandData closest = currentTarget;
        double minDistSq = distanceSq(x, z, currentTarget.center.getBlockX(), currentTarget.center.getBlockZ());

        for (IslandData other : islands) {
            if (other == currentTarget) continue;
            if (Math.abs(other.center.getBlockX() - x) > 300 || Math.abs(other.center.getBlockZ() - z) > 300) continue;

            double distSq = distanceSq(x, z, other.center.getBlockX(), other.center.getBlockZ());
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = other;
            }
        }
        return closest;
    }

    private double distanceSq(int x1, int z1, int x2, int z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2);
    }

    private void processColumn(World world, int x, int z, NetherBiome biome, Random random) {
        int surfaceY = findSurfaceY(world, x, z);
        if (surfaceY == -1) return;

        boolean firstSurfaceDecorated = false;
        int depth = 0;

        for (int y = surfaceY; y >= SCAN_MIN_Y; y--) {
            Material currentMat = world.getBlockAt(x, y, z).getType();
            if (currentMat == Material.AIR) continue;
            if (!isValidBaseMaterial(currentMat)) continue;

            Material newMat = determineNewMaterial(biome, depth);

            if (depth == 0 && !firstSurfaceDecorated) {
                addDecorations(world, x, y + 1, z, biome, random);
                firstSurfaceDecorated = true;
            }

            blockQueue.add(new NetherBlockData(x, y, z, newMat, biome));
            depth++;
        }
    }

    private int findSurfaceY(World world, int x, int z) {
        for (int y = SCAN_MAX_Y; y >= SCAN_MIN_Y; y--) {
            if (world.getBlockAt(x, y, z).getType() == TARGET_WOOL) {
                return y;
            }
        }
        return -1;
    }

    private boolean isValidBaseMaterial(Material mat) {
        return mat == TARGET_WOOL || mat == MAT_DEFAULT_BASE || mat == MAT_SSV_DEEP;
    }

    private Material determineNewMaterial(NetherBiome biome, int depth) {
        if (biome == NetherBiome.SOUL_SAND_VALLEY) {
            if (depth == 0) return MAT_SSV_SURFACE;
            if (depth <= 5) return MAT_SSV_UNDER;
            return MAT_SSV_DEEP;
        }

        if (depth == 0) {
            return (biome == NetherBiome.CRIMSON_FOREST) ? Material.CRIMSON_NYLIUM : Material.WARPED_NYLIUM;
        }
        return MAT_DEFAULT_BASE;
    }

    private void addDecorations(World world, int x, int y, int z, NetherBiome biome, Random random) {
        if (trySchedulePortal(world, x, y, z, random)) return;

        double chance = random.nextDouble();

        if (biome == NetherBiome.SOUL_SAND_VALLEY) {
            decorateSoulSandValley(world, x, y, z, random, chance, biome);
        } else if (biome == NetherBiome.CRIMSON_FOREST) {
            decorateCrimsonForest(x, y, z, chance, biome);
        } else if (biome == NetherBiome.WARPED_FOREST) {
            decorateWarpedForest(x, y, z, chance, biome);
        }
    }

    private boolean trySchedulePortal(World world, int x, int y, int z, Random random) {
        if (random.nextDouble() >= CHANCE_PORTAL) return false;

        if (!structureGen.tryScheduleStructure(x, y, z, world, StructureType.PORTAL)) return false;

        structureQueue.add(new StructureRequest(x, y, z, StructureType.PORTAL));
        return true;
    }

    private void decorateSoulSandValley(World world, int x, int y, int z, Random random, double chance, NetherBiome biome) {
        if (chance < SSV_FOSSIL_CHANCE) {
            createBoneFossil(world, x, y, z, random);
            return;
        }
        if (chance < SSV_BASALT_COLUMN) {
            createBasaltColumn(x, y, z, random);
            return;
        }
        if (chance < SSV_FIRE_CHANCE) {
            blockQueue.add(new NetherBlockData(x, y, z, Material.SOUL_FIRE, biome));
        }
    }

    private void decorateCrimsonForest(int x, int y, int z, double chance, NetherBiome biome) {
        if (chance < CRIMSON_FUNGUS) {
            blockQueue.add(new NetherBlockData(x, y, z, Material.CRIMSON_FUNGUS, biome));
            return;
        }
        if (chance < CRIMSON_TREE) {
            treeQueue.add(new TreeRequest(x, y, z, TreeType.CRIMSON_FUNGUS));
            return;
        }
        if (chance < CRIMSON_ROOTS) {
            blockQueue.add(new NetherBlockData(x, y, z, Material.CRIMSON_ROOTS, biome));
        }
    }

    private void decorateWarpedForest(int x, int y, int z, double chance, NetherBiome biome) {
        if (chance < WARPED_FUNGUS) {
            blockQueue.add(new NetherBlockData(x, y, z, Material.WARPED_FUNGUS, biome));
            return;
        }
        if (chance < WARPED_TREE) {
            treeQueue.add(new TreeRequest(x, y, z, TreeType.WARPED_FUNGUS));
            return;
        }
        if (chance < WARPED_SPROUTS) {
            blockQueue.add(new NetherBlockData(x, y, z, Material.WARPED_ROOTS, biome));
            return;
        }
        if (chance < (WARPED_SPROUTS + 0.15)) {
            blockQueue.add(new NetherBlockData(x, y, z, Material.NETHER_SPROUTS, biome));
        }
    }

    // --- GENERATOR KOLUMN BAZALTOWYCH ---
    private void createBasaltColumn(int x, int y, int z, Random random) {
        int height = 5 + random.nextInt(15);
        double currentRadius = 2.5;

        for (int i = 0; i < height; i++) {
            currentRadius = updateBasaltRadius(currentRadius);
            processBasaltLayer(x, y, z, i, currentRadius, random);
        }
    }

    private double updateBasaltRadius(double currentRadius) {
        if (currentRadius > 1.2) {
            return currentRadius - 0.4;
        }
        if (currentRadius < 1.0) return 1.0;
        return currentRadius;
    }

    private void processBasaltLayer(int x, int y, int z, int i, double currentRadius, Random random) {
        int offsetX = random.nextInt(3) - 1;
        int offsetZ = random.nextInt(3) - 1;
        int rCeil = (int) Math.ceil(currentRadius);
        double rSq = currentRadius * currentRadius;

        for (int bx = -rCeil; bx <= rCeil; bx++) {
            for (int bz = -rCeil; bz <= rCeil; bz++) {
                if (bx * bx + bz * bz > rSq) continue;
                if (random.nextDouble() <= 0.1) continue;

                int targetX = x + bx + (i > 2 ? offsetX : 0);
                int targetZ = z + bz + (i > 2 ? offsetZ : 0);
                blockQueue.add(new NetherBlockData(targetX, y + i, targetZ, Material.BASALT, NetherBiome.SOUL_SAND_VALLEY, true));
            }
        }
    }

    // --- GENERATOR SZKIELETÓW (FOSSILS) ---
    private void createBoneFossil(World world, int x, int y, int z, Random random) {
        int segments = 3 + random.nextInt(4);
        int direction = random.nextInt(2); // 0 = wzdłuż X, 1 = wzdłuż Z

        for (int i = 0; i < segments; i++) {
            processFossilSegment(world, x, y, z, i, direction, random);
        }
    }

    private void processFossilSegment(World world, int startX, int startY, int startZ, int i, int direction, Random random) {
        int currentX = startX + (direction == 0 ? i * 2 : 0);
        int currentZ = startZ + (direction == 1 ? i * 2 : 0);

        int ribHeight = 3 + random.nextInt(3);
        int targetTopY = startY + ribHeight;

        int leg1X = currentX + (direction == 1 ? 2 : 0);
        int leg1Z = currentZ + (direction == 0 ? 2 : 0);

        int leg2X = currentX - (direction == 1 ? 2 : 0);
        int leg2Z = currentZ - (direction == 0 ? 2 : 0);

        int leg1BaseY = findSurfaceBelow(world, leg1X, startY, leg1Z);
        int leg2BaseY = findSurfaceBelow(world, leg2X, startY, leg2Z);

        if (leg1BaseY == Integer.MIN_VALUE || leg2BaseY == Integer.MIN_VALUE) return;

        buildFossilRib(currentX, currentZ, targetTopY, direction);
        fillBoneColumn(leg1X, leg1BaseY, leg1Z, targetTopY);
        fillBoneColumn(leg2X, leg2BaseY, leg2Z, targetTopY);
    }

    private void buildFossilRib(int x, int z, int y, int direction) {
        blockQueue.add(new NetherBlockData(x, y, z, Material.BONE_BLOCK, NetherBiome.SOUL_SAND_VALLEY, true));
        if (direction == 0) {
            blockQueue.add(new NetherBlockData(x, y, z + 1, Material.BONE_BLOCK, NetherBiome.SOUL_SAND_VALLEY, true));
            blockQueue.add(new NetherBlockData(x, y, z - 1, Material.BONE_BLOCK, NetherBiome.SOUL_SAND_VALLEY, true));
        } else {
            blockQueue.add(new NetherBlockData(x + 1, y, z, Material.BONE_BLOCK, NetherBiome.SOUL_SAND_VALLEY, true));
            blockQueue.add(new NetherBlockData(x - 1, y, z, Material.BONE_BLOCK, NetherBiome.SOUL_SAND_VALLEY, true));
        }
    }

    private void fillBoneColumn(int x, int startY, int z, int targetTopY) {
        for (int currentY = startY; currentY < targetTopY; currentY++) {
            blockQueue.add(new NetherBlockData(x, currentY, z, Material.BONE_BLOCK, NetherBiome.SOUL_SAND_VALLEY, true));
        }
    }

    private int findSurfaceBelow(World world, int x, int startY, int z) {
        for (int y = startY; y >= startY - 20; y--) {
            if (y <= SCAN_MIN_Y) break;
            Material mat = world.getBlockAt(x, y, z).getType();
            Material blockBelow = world.getBlockAt(x, y - 1, z).getType();

            if (isAir(mat) && isSolidGround(blockBelow)) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private boolean isAir(Material mat) {
        return mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR;
    }

    private boolean isSolidGround(Material mat) {
        return !isAir(mat) && mat != Material.LAVA && mat != Material.WATER;
    }

    private void removeDroppedItems(World world) {
        for (Entity entity : world.getEntitiesByClass(Item.class)) {
            entity.remove();
        }
    }

    private void applyChanges(World world) {
        new BukkitRunnable() {
            int tickCounter = 0;
            boolean fortressPasted = false;

            @Override
            public void run() {
                tickCounter++;
                if (tickCounter % 200 == 0) removeDroppedItems(world);

                processBlockQueue(world);

                if (blockQueue.isEmpty()) {
                    processPostGeneration(world);
                }
            }

            private void processBlockQueue(World world) {
                int processed = 0;
                while (!blockQueue.isEmpty() && processed < BLOCKS_PER_TICK) {
                    NetherBlockData data = blockQueue.poll();
                    if (data != null) applyBlockData(world, data);
                    processed++;
                }
            }

            private void applyBlockData(World world, NetherBlockData data) {
                if (!world.isChunkLoaded(data.x >> 4, data.z >> 4)) {
                    world.loadChunk(data.x >> 4, data.z >> 4);
                }

                Block b = world.getBlockAt(data.x, data.y, data.z);
                b.setType(data.material, false);

                if (data.orientable && b.getBlockData() instanceof Orientable) {
                    Orientable o = (Orientable) b.getBlockData();
                    o.setAxis(org.bukkit.Axis.Y);
                    b.setBlockData(o, false);
                }

                world.setBiome(data.x, data.y, data.z, getBiomeType(data.biome));
            }

            private Biome getBiomeType(NetherBiome nb) {
                if (nb == NetherBiome.CRIMSON_FOREST) return Biome.CRIMSON_FOREST;
                if (nb == NetherBiome.WARPED_FOREST) return Biome.WARPED_FOREST;
                return Biome.SOUL_SAND_VALLEY;
            }

            private void processPostGeneration(World world) {
                pasteFortressIfNeeded(world);
                processStructureQueue(world);
                processTreeQueue(world);

                if (treeQueue.isEmpty()) {
                    removeDroppedItems(world);
                    ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Nether biomes successfully generated");
                    this.cancel();
                }
            }

            private void pasteFortressIfNeeded(World world) {
                if (fortressPasted) return;
                ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Pasting stronghold...");
                paster.pasteSchematic("twierdza.schem", new Location(world, 0, 125, 0));
                fortressPasted = true;
            }

            private void processStructureQueue(World world) {
                while (!structureQueue.isEmpty()) {
                    StructureRequest req = structureQueue.poll();
                    if (req == null) continue;

                    Location loc = new Location(world, req.x, req.y, req.z);
                    ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Pasting " + req.type.name() + " at " + req.x + " " + req.z);
                    structureGen.generate(loc, req.type);
                }
            }

            private void processTreeQueue(World world) {
                int treesProcessed = 0;
                while (!treeQueue.isEmpty() && treesProcessed < TREES_PER_TICK) {
                    TreeRequest req = treeQueue.poll();
                    if (req != null) generateTreeSafe(world, req);
                    treesProcessed++;
                }
            }

            private void generateTreeSafe(World world, TreeRequest req) {
                try {
                    world.generateTree(new Location(world, req.x, req.y, req.z), req.type);
                } catch (Exception ignored) {}
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static class TreeRequest {
        int x, y, z;
        TreeType type;
        TreeRequest(int x, int y, int z, TreeType type) { this.x = x; this.y = y; this.z = z; this.type = type; }
    }

    private static class StructureRequest {
        int x, y, z;
        StructureType type;
        StructureRequest(int x, int y, int z, StructureType type) { this.x = x; this.y = y; this.z = z; this.type = type; }
    }

    private static class NetherBlockData {
        int x, y, z;
        Material material;
        NetherBiome biome;
        boolean orientable;

        public NetherBlockData(int x, int y, int z, Material m, NetherBiome b) {
            this(x, y, z, m, b, false);
        }

        public NetherBlockData(int x, int y, int z, Material m, NetherBiome b, boolean orientable) {
            this.x = x; this.y = y; this.z = z; this.material = m; this.biome = b;
            this.orientable = orientable;
        }
    }
}