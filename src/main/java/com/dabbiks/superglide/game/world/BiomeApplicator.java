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
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Snowable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.PinkPetals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import com.dabbiks.superglide.game.world.WorldGenManager.IslandData;
import com.dabbiks.superglide.game.world.StructureGenerator.StructureType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dabbiks.superglide.ConsoleLogger.Type.WORLD_GENERATOR;
import static com.dabbiks.superglide.Superglide.plugin;

public class BiomeApplicator {

    // * CONFIG

    private final int SCAN_MARGIN_ADDITION = 100;
    private final int SCAN_MAX_Y = 255;
    private final int SCAN_MIN_Y = 130;

    private final double BIOME_NOISE_SCALE = 0.002;
    private final int BIOME_NOISE_OCTAVES = 8;

    private final double STONE_PATCH_SCALE = 0.1;
    private final double STONE_PATCH_THRESHOLD = 0.4;

    private final double FERN_NOISE_SCALE = 0.05;

    // * STRUCTURES

    private final double CHANCE_JUNGLE_TEMPLE = 0.005;
    private final double CHANCE_PYRAMID = 0.005;
    private final double CHANCE_PORTAL = 0.002;
    private final double CHANCE_MINE = 0.008;

    private final int DIRT_LAYER_DEPTH = 4;
    private final Material MAT_BASE_STONE = Material.STONE;

    // * BLOCKS

    private final Material MAT_MESA_SURFACE = Material.RED_SAND;
    private final Material MAT_DESERT_SURFACE = Material.SAND;
    private final Material MAT_DESERT_UNDER = Material.SANDSTONE;
    private final Material MAT_FOREST_SURFACE = Material.GRASS_BLOCK;
    private final Material MAT_FOREST_UNDER = Material.DIRT;
    private final Material MAT_SPRUCE_SURFACE = Material.GRASS_BLOCK;
    private final Material MAT_SPRUCE_UNDER = Material.COARSE_DIRT;
    private final Material MAT_OLD_GROWTH_SURFACE = Material.PODZOL;
    private final Material MAT_OLD_GROWTH_UNDER = Material.DIRT;
    private final Material MAT_SAVANNA_SURFACE = Material.GRASS_BLOCK;
    private final Material MAT_SAVANNA_UNDER = Material.DIRT;
    private final Material MAT_MUSHROOM_SURFACE = Material.MYCELIUM;
    private final Material MAT_MUSHROOM_UNDER = Material.DIRT;
    private final Material MAT_CHERRY_SURFACE = Material.GRASS_BLOCK;
    private final Material MAT_CHERRY_UNDER = Material.DIRT;
    private final Material MAT_PALE_SURFACE = Material.GRASS_BLOCK;
    private final Material MAT_PALE_UNDER = Material.DIRT;

    // * TREES

    private final double CHANCE_TREE_JUNGLE = 0.04;
    private final double CHANCE_TREE_FOREST = 0.015;
    private final double CHANCE_TREE_SPRUCE = 0.01;
    private final double CHANCE_TREE_OLD_GROWTH = 0.025;
    private final double CHANCE_TREE_SAVANNA = 0.008;
    private final double CHANCE_TREE_CHERRY = 0.035;
    private final double CHANCE_TREE_PALE = 0.020;

    private final int BLOCKS_PER_TICK = 8000;
    private final int TREES_PER_TICK = 4;

    // * ----------------------------------------------------------------------------

    private final Queue<ExtendedBlockData> blockQueue = new ConcurrentLinkedQueue<>();
    private final Queue<TreeRequest> treeQueue = new ConcurrentLinkedQueue<>();
    private final Queue<StructureRequest> structureQueue = new ConcurrentLinkedQueue<>();

    private final Map<IslandData, CustomBiome> islandBiomes = new HashMap<>();
    private final StructureGenerator structureGen = new StructureGenerator();

    private static final Material[] MESA_PATTERN = {
            Material.TERRACOTTA, Material.WHITE_TERRACOTTA, Material.YELLOW_TERRACOTTA,
            Material.ORANGE_TERRACOTTA, Material.RED_TERRACOTTA, Material.BROWN_TERRACOTTA,
            Material.TERRACOTTA, Material.ORANGE_TERRACOTTA
    };

    private enum CustomBiome {
        DESERT, JUNGLE, FOREST, SPRUCE, MESA,
        OLD_GROWTH_PINE_TAIGA, SAVANNA, MUSHROOM_FIELDS,
        CHERRY_GROVE, PALE_GARDEN
    }

    public void applyBiomes(List<IslandData> islands) {
        ConsoleLogger.info(WORLD_GENERATOR, "Applying surface biomes");
        World world = Constants.world;
        Random random = new Random();

        initializeGeneratorsAndBiomes(islands, random);

        new BukkitRunnable() {
            @Override
            public void run() {
                SimplexOctaveGenerator stoneNoise = new SimplexOctaveGenerator(random, 4);
                stoneNoise.setScale(STONE_PATCH_SCALE);
                SimplexOctaveGenerator fernNoise = new SimplexOctaveGenerator(random, 2);
                fernNoise.setScale(FERN_NOISE_SCALE);

                for (IslandData island : islands) {
                    processIslandStrict(island, islands, world, random, stoneNoise, fernNoise);
                }
                applyChanges(world);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void initializeGeneratorsAndBiomes(List<IslandData> islands, Random random) {
        SimplexOctaveGenerator biomeNoise = new SimplexOctaveGenerator(random, BIOME_NOISE_OCTAVES);
        biomeNoise.setScale(BIOME_NOISE_SCALE);

        for (IslandData island : islands) {
            double noiseVal = biomeNoise.noise(island.center.getBlockX(), island.center.getBlockZ(), 0.5, 0.5);
            islandBiomes.put(island, getBiomeType(noiseVal));
        }
    }

    private void processIslandStrict(IslandData targetIsland, List<IslandData> allIslands, World world, Random random, SimplexOctaveGenerator stoneNoise, SimplexOctaveGenerator fernNoise) {
        int cx = targetIsland.center.getBlockX();
        int cz = targetIsland.center.getBlockZ();
        CustomBiome biome = islandBiomes.get(targetIsland);

        int scanRange = targetIsland.radius + SCAN_MARGIN_ADDITION;
        double maxScanDistSq = scanRange * scanRange;

        for (int x = -scanRange; x <= scanRange; x++) {
            for (int z = -scanRange; z <= scanRange; z++) {
                processCoordinate(x, z, cx, cz, maxScanDistSq, targetIsland, allIslands, world, biome, random, stoneNoise, fernNoise);
            }
        }
    }

    private void processCoordinate(int x, int z, int cx, int cz, double maxScanDistSq, IslandData targetIsland, List<IslandData> allIslands, World world, CustomBiome biome, Random random, SimplexOctaveGenerator stoneNoise, SimplexOctaveGenerator fernNoise) {
        if (x * x + z * z > maxScanDistSq) return;
        int realX = cx + x;
        int realZ = cz + z;

        IslandData closest = getClosestIsland(realX, realZ, allIslands, targetIsland);
        if (closest != targetIsland) return;

        processFullColumn(world, realX, realZ, biome, random, stoneNoise, fernNoise);
    }

    private IslandData getClosestIsland(int x, int z, List<IslandData> islands, IslandData currentTarget) {
        IslandData closest = currentTarget;
        double minDistSq = distanceSq(x, z, currentTarget.center.getBlockX(), currentTarget.center.getBlockZ());
        for (IslandData other : islands) {
            if (other == currentTarget) continue;
            if (Math.abs(other.center.getBlockX() - x) > 350 || Math.abs(other.center.getBlockZ() - z) > 350) continue;
            double distSq = distanceSq(x, z, other.center.getBlockX(), other.center.getBlockZ());
            if (distSq < minDistSq) {
                minDistSq = distSq; closest = other;
            }
        }
        return closest;
    }

    private double distanceSq(int x1, int z1, int x2, int z2) {
        return Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2);
    }

    private void processFullColumn(World world, int x, int z, CustomBiome biome, Random random, SimplexOctaveGenerator stoneNoise, SimplexOctaveGenerator fernNoise) {
        int surfaceY = findSurfaceY(world, x, z);
        if (surfaceY == -1) return;

        int startY = Math.min(SCAN_MAX_Y, surfaceY + 2);
        int depth = -1;
        boolean firstSurfaceDecorated = false;

        for (int y = startY; y > SCAN_MIN_Y; y--) {
            Material currentMat = world.getBlockAt(x, y, z).getType();

            if (currentMat == Material.AIR) {
                depth = -1;
                continue;
            }
            if (currentMat != Material.WHITE_WOOL) continue;

            depth++;
            processBlockLayer(x, y, z, depth, biome, random, stoneNoise, fernNoise, firstSurfaceDecorated);
            if (depth == 0) firstSurfaceDecorated = true;
        }
    }

    private int findSurfaceY(World world, int x, int z) {
        for (int y = SCAN_MAX_Y; y > SCAN_MIN_Y; y--) {
            if (world.getBlockAt(x, y, z).getType() == Material.WHITE_WOOL) {
                return y;
            }
        }
        return -1;
    }

    private void processBlockLayer(int x, int y, int z, int depth, CustomBiome biome, Random random, SimplexOctaveGenerator stoneNoise, SimplexOctaveGenerator fernNoise, boolean firstSurfaceDecorated) {
        Material newMat = determineBaseMaterial(biome, depth, y);
        newMat = applyStoneVariation(newMat, x, y, z, depth, stoneNoise);

        boolean makeGrassSnowy = false;

        if (depth == 0 && !firstSurfaceDecorated && y < 255) {
            boolean placedSnow = addDecorations(x, y + 1, z, biome, random, fernNoise);
            if (placedSnow && biome == CustomBiome.SPRUCE && newMat == Material.GRASS_BLOCK) {
                makeGrassSnowy = true;
            }
        }

        blockQueue.add(new ExtendedBlockData(x, y, z, newMat, biome, null, makeGrassSnowy));
    }

    private Material determineBaseMaterial(CustomBiome biome, int depth, int y) {
        switch (biome) {
            case DESERT: return (depth == 0) ? MAT_DESERT_SURFACE : MAT_DESERT_UNDER;
            case MESA: return (depth == 0) ? MAT_MESA_SURFACE : MESA_PATTERN[Math.abs(y) % MESA_PATTERN.length];
            case SPRUCE: return (depth == 0) ? MAT_SPRUCE_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_SPRUCE_UNDER : MAT_BASE_STONE);
            case OLD_GROWTH_PINE_TAIGA: return (depth == 0) ? MAT_OLD_GROWTH_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_OLD_GROWTH_UNDER : MAT_BASE_STONE);
            case SAVANNA: return (depth == 0) ? MAT_SAVANNA_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_SAVANNA_UNDER : MAT_BASE_STONE);
            case MUSHROOM_FIELDS: return (depth == 0) ? MAT_MUSHROOM_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_MUSHROOM_UNDER : MAT_BASE_STONE);
            case CHERRY_GROVE: return (depth == 0) ? MAT_CHERRY_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_CHERRY_UNDER : MAT_BASE_STONE);
            case PALE_GARDEN: return (depth == 0) ? MAT_PALE_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_PALE_UNDER : MAT_BASE_STONE);
            default: return (depth == 0) ? MAT_FOREST_SURFACE : (depth < DIRT_LAYER_DEPTH ? MAT_FOREST_UNDER : MAT_BASE_STONE);
        }
    }

    private Material applyStoneVariation(Material mat, int x, int y, int z, int depth, SimplexOctaveGenerator stoneNoise) {
        if (mat != Material.STONE || depth <= 2) return mat;

        double stoneVal = stoneNoise.noise(x, y, z, 0.5, 0.5);
        if (stoneVal > STONE_PATCH_THRESHOLD + 0.2) return Material.ANDESITE;
        if (stoneVal > STONE_PATCH_THRESHOLD) return Material.GRANITE;
        if (stoneVal < -STONE_PATCH_THRESHOLD) return Material.DIORITE;
        return mat;
    }

    private boolean addDecorations(int x, int y, int z, CustomBiome biome, Random random, SimplexOctaveGenerator fernNoise) {
        World world = Constants.world;

        if (tryScheduleStructure(x, y, z, world, biome, random)) return false;
        if (trySpawnTree(x, y, z, biome, random)) return false;

        return decorateBiomeSurface(x, y, z, biome, random, fernNoise);
    }

    private boolean tryScheduleStructure(int x, int y, int z, World world, CustomBiome biome, Random random) {
        if (random.nextDouble() < CHANCE_PORTAL) {
            return scheduleIfPossible(x, y, z, world, StructureType.PORTAL);
        }
        if (biome == CustomBiome.JUNGLE && random.nextDouble() < CHANCE_JUNGLE_TEMPLE) {
            return scheduleIfPossible(x, y, z, world, StructureType.JUNGLE_TEMPLE);
        }
        if (biome == CustomBiome.DESERT && random.nextDouble() < CHANCE_PYRAMID) {
            return scheduleIfPossible(x, y, z, world, StructureType.PYRAMID);
        }
        if (biome == CustomBiome.MESA && random.nextDouble() < CHANCE_MINE) {
            return scheduleIfPossible(x, y, z, world, StructureType.MINE);
        }
        return false;
    }

    private boolean scheduleIfPossible(int x, int y, int z, World world, StructureType type) {
        if (structureGen.tryScheduleStructure(x, y, z, world, type)) {
            structureQueue.add(new StructureRequest(x, y, z, type));
            return true;
        }
        return false;
    }

    private boolean decorateBiomeSurface(int x, int y, int z, CustomBiome biome, Random random, SimplexOctaveGenerator fernNoise) {
        double chance = random.nextDouble();
        switch (biome) {
            case DESERT: decorateDesert(x, y, z, biome, chance, random); break;
            case MESA: decorateMesa(x, y, z, biome, chance, random); break;
            case JUNGLE: decorateJungle(x, y, z, biome, chance, random); break;
            case FOREST: decorateForest(x, y, z, biome, chance, random); break;
            case SPRUCE: return decorateSpruce(x, y, z, biome, chance, random, fernNoise);
            case OLD_GROWTH_PINE_TAIGA: decorateOldGrowth(x, y, z, biome, chance, random); break;
            case SAVANNA: decorateSavanna(x, y, z, biome, chance); break;
            case MUSHROOM_FIELDS: decorateMushroom(x, y, z, biome, chance); break;
            case CHERRY_GROVE: decorateCherry(x, y, z, biome, chance, random); break;
            case PALE_GARDEN: decoratePaleGarden(x, y, z, biome, chance, random, fernNoise); break;
        }
        return false;
    }

    private void decorateDesert(int x, int y, int z, CustomBiome biome, double chance, Random random) {
        if (chance < 0.03) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.DEAD_BUSH, biome));
            return;
        }
        if (chance < 0.05) {
            int h = 1 + random.nextInt(3);
            for(int i=0; i<h; i++) blockQueue.add(new ExtendedBlockData(x, y+i, z, Material.CACTUS, biome));
        }
    }

    private void decorateMesa(int x, int y, int z, CustomBiome biome, double chance, Random random) {
        if (chance < 0.02) {
            int h = 1 + random.nextInt(2);
            for(int i=0; i<h; i++) blockQueue.add(new ExtendedBlockData(x, y+i, z, Material.CACTUS, biome));
            return;
        }
        if (chance < 0.05) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.DEAD_BUSH, biome));
        }
    }

    private void decorateJungle(int x, int y, int z, CustomBiome biome, double chance, Random random) {
        if (chance < 0.20) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.SHORT_GRASS, biome));
        } else if (chance < 0.50) {
            createTallGrass(x, y, z, biome);
        } else if (chance < 0.55) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.BLUE_ORCHID, biome));
        } else if (chance < 0.60) {
            createBush(x, y, z, Material.JUNGLE_LEAVES, biome, random);
        }
    }

    private void decorateForest(int x, int y, int z, CustomBiome biome, double chance, Random random) {
        if (chance < 0.15) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.SHORT_GRASS, biome));
        } else if (chance < 0.20) {
            blockQueue.add(new ExtendedBlockData(x, y, z, random.nextBoolean() ? Material.POPPY : Material.DANDELION, biome));
        } else if (chance < 0.02) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.FIREFLY_BUSH, biome));
        }
    }

    private boolean decorateSpruce(int x, int y, int z, CustomBiome biome, double chance, Random random, SimplexOctaveGenerator fernNoise) {
        if (chance < 0.02) {
            createBush(x, y, z, Material.SPRUCE_LEAVES, biome, random);
            return false;
        }

        double cluster = fernNoise.noise(x, z, 0.5, 0.5);
        if (cluster > 0.5 && random.nextDouble() < 0.15) {
            if (random.nextDouble() < 0.5) createLargeFern(x, y, z, biome);
            else blockQueue.add(new ExtendedBlockData(x, y, z, random.nextBoolean() ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM, biome));
            return false;
        }

        blockQueue.add(new ExtendedBlockData(x, y, z, Material.SNOW, biome));
        return true;
    }

    private void decorateOldGrowth(int x, int y, int z, CustomBiome biome, double chance, Random random) {
        if (chance < 0.015) {
            createBoulder(x, y, z, biome, random);
        } else if (chance < 0.06) {
            createBush(x, y, z, Material.SPRUCE_LEAVES, biome, random);
        } else if (chance < 0.30) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.FERN, biome));
        } else if (chance < 0.35) {
            createLargeFern(x, y, z, biome);
        }
    }

    private void decorateSavanna(int x, int y, int z, CustomBiome biome, double chance) {
        if (chance < 0.50) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.SHORT_GRASS, biome));
        } else if (chance < 0.70) {
            createTallGrass(x, y, z, biome);
        }
    }

    private void decorateMushroom(int x, int y, int z, CustomBiome biome, double chance) {
        if (chance < 0.03) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.RED_MUSHROOM, biome));
        } else if (chance < 0.06) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.BROWN_MUSHROOM, biome));
        }
    }

    private void decorateCherry(int x, int y, int z, CustomBiome biome, double chance, Random random) {
        if (chance < 0.40) {
            int petals = 1 + random.nextInt(4);
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.PINK_PETALS, biome, null, false, petals));
        }
    }

    private void decoratePaleGarden(int x, int y, int z, CustomBiome biome, double chance, Random random, SimplexOctaveGenerator fernNoise) {
        double mossCluster = fernNoise.noise(x, z, 0.8, 0.8) + (random.nextDouble() * 0.1);
        if (mossCluster > 0.4) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.PALE_MOSS_CARPET, biome));
            return;
        }
        if (chance < 0.05) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.CLOSED_EYEBLOSSOM, biome));
            return;
        }
        if (chance < 0.35) {
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.SHORT_GRASS, biome));
        }
    }

    private boolean trySpawnTree(int x, int y, int z, CustomBiome biome, Random random) {
        double chance = random.nextDouble();
        TreeType type = determineTreeType(biome, chance, random);

        if (type != null) {
            treeQueue.add(new TreeRequest(x, y, z, type));
            return true;
        }
        return false;
    }

    private TreeType determineTreeType(CustomBiome biome, double chance, Random random) {
        switch (biome) {
            case JUNGLE: return (chance < CHANCE_TREE_JUNGLE) ? (random.nextBoolean() ? TreeType.JUNGLE : TreeType.SMALL_JUNGLE) : null;
            case FOREST: return (chance < CHANCE_TREE_FOREST) ? (random.nextBoolean() ? TreeType.TREE : TreeType.BIRCH) : null;
            case SPRUCE: return (chance < CHANCE_TREE_SPRUCE) ? TreeType.REDWOOD : null;
            case OLD_GROWTH_PINE_TAIGA: return (chance < CHANCE_TREE_OLD_GROWTH) ? (random.nextDouble() < 0.7 ? TreeType.MEGA_REDWOOD : TreeType.REDWOOD) : null;
            case SAVANNA: return (chance < CHANCE_TREE_SAVANNA) ? TreeType.ACACIA : null;
            case MUSHROOM_FIELDS: return (chance < 0.015) ? (random.nextBoolean() ? TreeType.RED_MUSHROOM : TreeType.BROWN_MUSHROOM) : null;
            case CHERRY_GROVE: return (chance < CHANCE_TREE_CHERRY) ? TreeType.CHERRY : null;
            case PALE_GARDEN: return (chance < CHANCE_TREE_PALE) ? getPaleOakType() : null;
            default: return null;
        }
    }

    private TreeType getPaleOakType() {
        try {
            return TreeType.valueOf("PALE_OAK");
        } catch (IllegalArgumentException e) {
            return TreeType.DARK_OAK;
        }
    }

    private void createBush(int x, int y, int z, Material leafType, CustomBiome biome, Random random) {
        blockQueue.add(new ExtendedBlockData(x, y, z, leafType, biome));
        int[][] offsets = {{1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}, {0,1,0}};
        for (int[] off : offsets) {
            if (random.nextBoolean()) blockQueue.add(new ExtendedBlockData(x + off[0], y + off[1], z + off[2], leafType, biome));
        }
    }

    private void createBoulder(int x, int y, int z, CustomBiome biome, Random random) {
        int size = random.nextBoolean() ? 2 : 3;
        for(int bx = 0; bx < size; bx++) {
            for(int by = 0; by < size; by++) {
                for(int bz = 0; bz < size; bz++) {
                    Material mat = random.nextBoolean() ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE;
                    blockQueue.add(new ExtendedBlockData(x + bx, y + by - 1, z + bz, mat, biome));
                }
            }
        }
    }

    private void createTallGrass(int x, int y, int z, CustomBiome biome) {
        blockQueue.add(new ExtendedBlockData(x, y, z, Material.TALL_GRASS, biome, Bisected.Half.BOTTOM));
        blockQueue.add(new ExtendedBlockData(x, y + 1, z, Material.TALL_GRASS, biome, Bisected.Half.TOP));
    }

    private void createLargeFern(int x, int y, int z, CustomBiome biome) {
        blockQueue.add(new ExtendedBlockData(x, y, z, Material.LARGE_FERN, biome, Bisected.Half.BOTTOM));
        blockQueue.add(new ExtendedBlockData(x, y + 1, z, Material.LARGE_FERN, biome, Bisected.Half.TOP));
    }

    private CustomBiome getBiomeType(double noise) {
        if (noise < -0.70) return CustomBiome.MUSHROOM_FIELDS;
        if (noise < -0.50) return CustomBiome.MESA;
        if (noise < -0.35) return CustomBiome.PALE_GARDEN;
        if (noise < -0.15) return CustomBiome.SPRUCE;
        if (noise < 0.00) return CustomBiome.OLD_GROWTH_PINE_TAIGA;
        if (noise < 0.20) return CustomBiome.CHERRY_GROVE;
        if (noise < 0.35) return CustomBiome.FOREST;
        if (noise < 0.60) return CustomBiome.SAVANNA;
        if (noise < 0.80) return CustomBiome.JUNGLE;
        return CustomBiome.DESERT;
    }

    private void removeDroppedItems(World world) {
        for (Entity entity : world.getEntitiesByClass(Item.class)) {
            entity.remove();
        }
    }

    private void applyChanges(World world) {
        new BukkitRunnable() {
            int tickCounter = 0;

            @Override
            public void run() {
                if (tickCounter % 200 == 0) removeDroppedItems(world);
                tickCounter++;

                processBlockQueue(world);

                if (blockQueue.isEmpty()) {
                    processStructureQueue(world);
                    processTreeQueue(world);

                    if (treeQueue.isEmpty()) {
                        removeDroppedItems(world);
                        ConsoleLogger.info(WORLD_GENERATOR, "Surface painted successfully");
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void processBlockQueue(World world) {
        int processed = 0;
        while (!blockQueue.isEmpty() && processed < BLOCKS_PER_TICK) {
            ExtendedBlockData data = blockQueue.poll();
            if (data != null) applySingleBlock(world, data);
            processed++;
        }
    }

    private void applySingleBlock(World world, ExtendedBlockData data) {
        if (!world.isChunkLoaded(data.x >> 4, data.z >> 4)) {
            world.loadChunk(data.x >> 4, data.z >> 4);
        }
        Block b = world.getBlockAt(data.x, data.y, data.z);

        Biome mcBiome = mapToBiome(data.biomeType);
        world.setBiome(data.x, data.y, data.z, mcBiome);

        BlockData bd = data.material.createBlockData();
        configureBlockData(bd, data);

        b.setBlockData(bd, false);
    }

    private void configureBlockData(BlockData bd, ExtendedBlockData data) {
        if (bd instanceof Leaves leaves) {
            leaves.setPersistent(true);
        } else if (bd instanceof Bisected bisected && data.half != null) {
            bisected.setHalf(data.half);
        } else if (data.isSnowy && bd instanceof Snowable snowable) {
            snowable.setSnowy(true);
        } else if (bd instanceof PinkPetals pp) {
            int amount = Math.max(1, Math.min(4, data.petalCount));
            pp.setFlowerAmount(amount);
        }
    }

    private Biome mapToBiome(CustomBiome cb) {
        switch (cb) {
            case DESERT: return Biome.DESERT;
            case JUNGLE: return Biome.JUNGLE;
            case FOREST: return Biome.FOREST;
            case SPRUCE: return Biome.SNOWY_TAIGA;
            case MESA: return Biome.BADLANDS;
            case OLD_GROWTH_PINE_TAIGA: return Biome.OLD_GROWTH_PINE_TAIGA;
            case SAVANNA: return Biome.SAVANNA;
            case MUSHROOM_FIELDS: return Biome.MUSHROOM_FIELDS;
            case CHERRY_GROVE: return Biome.CHERRY_GROVE;
            case PALE_GARDEN:
                try { return Biome.valueOf("PALE_GARDEN"); }
                catch (IllegalArgumentException e) { return Biome.DARK_FOREST; }
            default: return Biome.PLAINS;
        }
    }

    private void processStructureQueue(World world) {
        while (!structureQueue.isEmpty()) {
            StructureRequest req = structureQueue.poll();
            if (req == null) continue;

            Location loc = new Location(world, req.x, req.y, req.z);
            ConsoleLogger.info(WORLD_GENERATOR, "Generating "  + req.type + " at " + req.x + ", " + req.z);
            structureGen.generate(loc, req.type);
        }
    }

    private void processTreeQueue(World world) {
        int treesProcessed = 0;
        while (!treeQueue.isEmpty() && treesProcessed < TREES_PER_TICK) {
            TreeRequest req = treeQueue.poll();
            if (req != null) {
                try {
                    world.generateTree(new org.bukkit.Location(world, req.x, req.y, req.z), req.type);
                } catch (Exception ignored) {}
            }
            treesProcessed++;
        }
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

    private static class ExtendedBlockData {
        int x, y, z;
        Material material;
        CustomBiome biomeType;
        Bisected.Half half;
        boolean isSnowy;
        int petalCount;

        public ExtendedBlockData(int x, int y, int z, Material m, CustomBiome b) {
            this(x, y, z, m, b, null, false, 1);
        }

        public ExtendedBlockData(int x, int y, int z, Material m, CustomBiome b, Bisected.Half h) {
            this(x, y, z, m, b, h, false, 1);
        }

        public ExtendedBlockData(int x, int y, int z, Material m, CustomBiome b, Bisected.Half h, boolean snowy) {
            this(x, y, z, m, b, h, snowy, 1);
        }

        public ExtendedBlockData(int x, int y, int z, Material m, CustomBiome b, Bisected.Half h, boolean snowy, int petalCount) {
            this.x = x; this.y = y; this.z = z; this.material = m; this.biomeType = b;
            this.half = h; this.isSnowy = snowy; this.petalCount = petalCount;
        }
    }
}
