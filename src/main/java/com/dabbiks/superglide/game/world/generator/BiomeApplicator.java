package com.dabbiks.superglide.game.world.generator;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.VisualizePath;
import com.dabbiks.superglide.utils.Constants;
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
import com.dabbiks.superglide.game.world.generator.WorldGenManager.IslandData;
import com.dabbiks.superglide.game.world.generator.StructureGenerator.StructureType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dabbiks.superglide.ConsoleLogger.Type.WORLD_GENERATOR;
import static com.dabbiks.superglide.Superglide.instance;
import static com.dabbiks.superglide.Superglide.plugin;

public class BiomeApplicator {

    // * CONFIG

    // Zwiększony margines, aby na pewno złapać wystające elementy wysp
    private final int SCAN_MARGIN_ADDITION = 120;
    private final int SCAN_MAX_Y = 215;
    private final int SCAN_MIN_Y = 110;

    private final int BIOME_APPLY_MIN_Y = 130;

    private final double BIOME_NOISE_SCALE = 0.002;
    private final int BIOME_NOISE_OCTAVES = 8;

    // --- NOWE: Konfiguracja "Domain Warping" dla granic biomów ---
    private final double BORDER_NOISE_SCALE = 0.015; // Częstotliwość zakłóceń
    private final double BORDER_NOISE_AMPLITUDE = 25.0; // Siła wykrzywiania granic (w blokach)

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

    private final int BLOCKS_PER_TICK = 64000;
    private final int TREES_PER_TICK = 32;

    // * ----------------------------------------------------------------------------

    private final Queue<ExtendedBlockData> blockQueue = new ConcurrentLinkedQueue<>();
    private final Queue<TreeRequest> treeQueue = new ConcurrentLinkedQueue<>();
    private final Queue<StructureRequest> structureQueue = new ConcurrentLinkedQueue<>();

    private final Map<IslandData, CustomBiome> islandBiomes = new HashMap<>();
    private final StructureGenerator structureGen = new StructureGenerator();

    private volatile boolean isCalculationDone = false;

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
        ConsoleLogger.info(WORLD_GENERATOR, "Applying biomes (Global Scan w/ Blending)");
        World world = Constants.world;
        Random random = new Random();

        initializeGeneratorsAndBiomes(islands, random);

        isCalculationDone = false;
        applyChanges(world);

        new BukkitRunnable() {
            @Override
            public void run() {
                SimplexOctaveGenerator stoneNoise = new SimplexOctaveGenerator(random, 4);
                stoneNoise.setScale(STONE_PATCH_SCALE);
                SimplexOctaveGenerator fernNoise = new SimplexOctaveGenerator(random, 2);
                fernNoise.setScale(FERN_NOISE_SCALE);

                // Szum do zakrzywiania granic
                SimplexOctaveGenerator borderNoise = new SimplexOctaveGenerator(random, 2);
                borderNoise.setScale(BORDER_NOISE_SCALE);

                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
                int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

                // Obliczamy bounding box
                for (IslandData island : islands) {
                    int r = island.radius + SCAN_MARGIN_ADDITION;
                    if (island.center.getBlockX() - r < minX) minX = island.center.getBlockX() - r;
                    if (island.center.getBlockX() + r > maxX) maxX = island.center.getBlockX() + r;
                    if (island.center.getBlockZ() - r < minZ) minZ = island.center.getBlockZ() - r;
                    if (island.center.getBlockZ() + r > maxZ) maxZ = island.center.getBlockZ() + r;
                }

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {

                        // --- LOGIKA MIESZANIA GRANIC (DOMAIN WARPING) ---
                        // Zamiast szukać biomu dla (x, z), szukamy dla "zaburzonego" (x, z).
                        // To sprawia, że linia podziału "pływa".

                        double noiseX = borderNoise.noise(x, z, 0.5, 0.5) * BORDER_NOISE_AMPLITUDE;
                        double noiseZ = borderNoise.noise(z, x, 0.5, 0.5) * BORDER_NOISE_AMPLITUDE; // Odwrócone parametry dla innego wzoru

                        int distortedX = (int) (x + noiseX);
                        int distortedZ = (int) (z + noiseZ);

                        IslandData closest = null;
                        double minDistSq = Double.MAX_VALUE;

                        for (IslandData island : islands) {
                            // Sprawdzamy dystans do ZABURZONEGO punktu
                            int dx = island.center.getBlockX() - distortedX;
                            int dz = island.center.getBlockZ() - distortedZ;

                            // Szerokie pre-check (na oryginalnych coordach dla wydajności)
                            if (Math.abs(island.center.getBlockX() - x) > 400 || Math.abs(island.center.getBlockZ() - z) > 400) continue;

                            double dSq = (dx * dx) + (dz * dz);
                            if (dSq < minDistSq) {
                                minDistSq = dSq;
                                closest = island;
                            }
                        }

                        if (closest == null) continue;

                        // --- POPRAWKA NA POZOSTAJĄCĄ WEŁNĘ ---
                        // Usunąłem sprawdzanie "maxDist". Jeśli jesteśmy w obrębie bounding boxa (minX-maxX),
                        // to ZAWSZE przypisujemy najbliższy biom. Dzięki temu wystające kawałki wełny
                        // zostaną pomalowane biomem, który jest do nich najbliżej (nawet jeśli daleko od centrum).

                        CustomBiome biome = islandBiomes.get(closest);

                        processGlobalColumn(world, x, z, biome, random, stoneNoise, fernNoise);
                    }
                }

                isCalculationDone = true;
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

    private void processGlobalColumn(World world, int x, int z, CustomBiome biome, Random random, SimplexOctaveGenerator stoneNoise, SimplexOctaveGenerator fernNoise) {
        int currentY = SCAN_MAX_Y;

        while (currentY > SCAN_MIN_Y) {
            Material mat = world.getBlockAt(x, currentY, z).getType();

            if (mat == Material.AIR) {
                if (currentY > BIOME_APPLY_MIN_Y) {
                    blockQueue.add(new ExtendedBlockData(x, currentY, z, null, biome, null, false, 1, true));
                }
                currentY--;
                continue;
            }

            // Każda wełna znaleziona w tym skanie MUSI zostać pomalowana
            if (mat == Material.WHITE_WOOL) {
                break;
            }

            if (currentY > BIOME_APPLY_MIN_Y) {
                blockQueue.add(new ExtendedBlockData(x, currentY, z, null, biome, null, false, 1, true));
            }
            currentY--;
        }

        if (currentY <= SCAN_MIN_Y) return;

        int startY = currentY;
        int depth = 0;
        boolean firstSurfaceDecorated = false;

        for (int y = startY; y > SCAN_MIN_Y; y--) {
            Material currentMat = world.getBlockAt(x, y, z).getType();

            if (currentMat == Material.AIR) {
                depth = -1;
                if (y > BIOME_APPLY_MIN_Y) {
                    blockQueue.add(new ExtendedBlockData(x, y, z, null, biome, null, false, 1, true));
                }
                continue;
            }

            // Ważne: Akceptujemy wełnę bezwarunkowo tutaj, bo jesteśmy w środku pętli globalnej
            if (currentMat != Material.WHITE_WOOL) continue;

            boolean shouldApplyBiome = (y > BIOME_APPLY_MIN_Y);
            processBlockLayer(x, y, z, depth, biome, random, stoneNoise, fernNoise, firstSurfaceDecorated, shouldApplyBiome);

            if (depth == 0) firstSurfaceDecorated = true;
            depth++;
        }
    }

    private void processBlockLayer(int x, int y, int z, int depth, CustomBiome biome, Random random, SimplexOctaveGenerator stoneNoise, SimplexOctaveGenerator fernNoise, boolean firstSurfaceDecorated, boolean applyBiome) {
        Material newMat = determineBaseMaterial(biome, depth, y);
        newMat = applyStoneVariation(newMat, x, y, z, depth, stoneNoise);

        boolean makeGrassSnowy = false;

        if (depth == 0 && !firstSurfaceDecorated && y < 255) {
            boolean placedSnow = addDecorations(x, y + 1, z, biome, random, fernNoise);
            if (placedSnow && biome == CustomBiome.SPRUCE && newMat == Material.GRASS_BLOCK) {
                makeGrassSnowy = true;
            }
        }

        blockQueue.add(new ExtendedBlockData(x, y, z, newMat, biome, null, makeGrassSnowy, 1, applyBiome));
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
        boolean b = true;

        switch (biome) {
            case DESERT: decorateDesert(x, y, z, biome, chance, random, b); break;
            case MESA: decorateMesa(x, y, z, biome, chance, random, b); break;
            case JUNGLE: decorateJungle(x, y, z, biome, chance, random, b); break;
            case FOREST: decorateForest(x, y, z, biome, chance, random, b); break;
            case SPRUCE: return decorateSpruce(x, y, z, biome, chance, random, fernNoise, b);
            case OLD_GROWTH_PINE_TAIGA: decorateOldGrowth(x, y, z, biome, chance, random, b); break;
            case SAVANNA: decorateSavanna(x, y, z, biome, chance, b); break;
            case MUSHROOM_FIELDS: decorateMushroom(x, y, z, biome, chance, b); break;
            case CHERRY_GROVE: decorateCherry(x, y, z, biome, chance, random, b); break;
            case PALE_GARDEN: decoratePaleGarden(x, y, z, biome, chance, random, fernNoise, b); break;
        }
        return false;
    }

    private void addDeco(int x, int y, int z, Material mat, CustomBiome biome, boolean applyBiome) {
        if (y <= BIOME_APPLY_MIN_Y) applyBiome = false;
        blockQueue.add(new ExtendedBlockData(x, y, z, mat, biome, null, false, 1, applyBiome));
    }

    private void addDeco(int x, int y, int z, Material mat, CustomBiome biome, Bisected.Half half, boolean applyBiome) {
        if (y <= BIOME_APPLY_MIN_Y) applyBiome = false;
        blockQueue.add(new ExtendedBlockData(x, y, z, mat, biome, half, false, 1, applyBiome));
    }

    private void decorateDesert(int x, int y, int z, CustomBiome biome, double chance, Random random, boolean b) {
        if (chance < 0.03) {
            addDeco(x, y, z, Material.DEAD_BUSH, biome, b);
            return;
        }
        if (chance < 0.05) {
            int h = 1 + random.nextInt(3);
            for(int i=0; i<h; i++) addDeco(x, y+i, z, Material.CACTUS, biome, b);
        }
    }

    private void decorateMesa(int x, int y, int z, CustomBiome biome, double chance, Random random, boolean b) {
        if (chance < 0.02) {
            int h = 1 + random.nextInt(2);
            for(int i=0; i<h; i++) addDeco(x, y+i, z, Material.CACTUS, biome, b);
            return;
        }
        if (chance < 0.05) {
            addDeco(x, y, z, Material.DEAD_BUSH, biome, b);
        }
    }

    private void decorateJungle(int x, int y, int z, CustomBiome biome, double chance, Random random, boolean b) {
        if (chance < 0.20) {
            addDeco(x, y, z, Material.SHORT_GRASS, biome, b);
        } else if (chance < 0.50) {
            createTallGrass(x, y, z, biome);
        } else if (chance < 0.55) {
            addDeco(x, y, z, Material.BLUE_ORCHID, biome, b);
        } else if (chance < 0.60) {
            createBush(x, y, z, Material.JUNGLE_LEAVES, biome, random);
        }
    }

    private void decorateForest(int x, int y, int z, CustomBiome biome, double chance, Random random, boolean b) {
        if (chance < 0.15) {
            addDeco(x, y, z, Material.SHORT_GRASS, biome, b);
        } else if (chance < 0.20) {
            addDeco(x, y, z, random.nextBoolean() ? Material.POPPY : Material.DANDELION, biome, b);
        } else if (chance < 0.02) {
            addDeco(x, y, z, Material.FIREFLY_BUSH, biome, b);
        }
    }

    private boolean decorateSpruce(int x, int y, int z, CustomBiome biome, double chance, Random random, SimplexOctaveGenerator fernNoise, boolean b) {
        if (chance < 0.02) {
            createBush(x, y, z, Material.SPRUCE_LEAVES, biome, random);
            return false;
        }

        double cluster = fernNoise.noise(x, z, 0.5, 0.5);
        if (cluster > 0.5 && random.nextDouble() < 0.15) {
            if (random.nextDouble() < 0.5) createLargeFern(x, y, z, biome);
            else addDeco(x, y, z, random.nextBoolean() ? Material.BROWN_MUSHROOM : Material.RED_MUSHROOM, biome, b);
            return false;
        }

        addDeco(x, y, z, Material.SNOW, biome, b);
        return true;
    }

    private void decorateOldGrowth(int x, int y, int z, CustomBiome biome, double chance, Random random, boolean b) {
        if (chance < 0.015) {
            createBoulder(x, y, z, biome, random);
        } else if (chance < 0.06) {
            createBush(x, y, z, Material.SPRUCE_LEAVES, biome, random);
        } else if (chance < 0.30) {
            addDeco(x, y, z, Material.FERN, biome, b);
        } else if (chance < 0.35) {
            createLargeFern(x, y, z, biome);
        }
    }

    private void decorateSavanna(int x, int y, int z, CustomBiome biome, double chance, boolean b) {
        if (chance < 0.50) {
            addDeco(x, y, z, Material.SHORT_GRASS, biome, b);
        } else if (chance < 0.70) {
            createTallGrass(x, y, z, biome);
        }
    }

    private void decorateMushroom(int x, int y, int z, CustomBiome biome, double chance, boolean b) {
        if (chance < 0.03) {
            addDeco(x, y, z, Material.RED_MUSHROOM, biome, b);
        } else if (chance < 0.06) {
            addDeco(x, y, z, Material.BROWN_MUSHROOM, biome, b);
        }
    }

    private void decorateCherry(int x, int y, int z, CustomBiome biome, double chance, Random random, boolean b) {
        if (chance < 0.40) {
            int petals = 1 + random.nextInt(4);
            boolean apply = y > BIOME_APPLY_MIN_Y;
            blockQueue.add(new ExtendedBlockData(x, y, z, Material.PINK_PETALS, biome, null, false, petals, apply));
        }
    }

    private void decoratePaleGarden(int x, int y, int z, CustomBiome biome, double chance, Random random, SimplexOctaveGenerator fernNoise, boolean b) {
        double mossCluster = fernNoise.noise(x, z, 0.8, 0.8) + (random.nextDouble() * 0.1);
        if (mossCluster > 0.4) {
            addDeco(x, y, z, Material.PALE_MOSS_CARPET, biome, b);
            return;
        }
        if (chance < 0.05) {
            addDeco(x, y, z, Material.CLOSED_EYEBLOSSOM, biome, b);
            return;
        }
        if (chance < 0.35) {
            addDeco(x, y, z, Material.SHORT_GRASS, biome, b);
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
        boolean b = y > BIOME_APPLY_MIN_Y;
        blockQueue.add(new ExtendedBlockData(x, y, z, leafType, biome, null, false, 1, b));
        int[][] offsets = {{1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}, {0,1,0}};
        for (int[] off : offsets) {
            if (random.nextBoolean()) {
                boolean bOff = (y + off[1]) > BIOME_APPLY_MIN_Y;
                blockQueue.add(new ExtendedBlockData(x + off[0], y + off[1], z + off[2], leafType, biome, null, false, 1, bOff));
            }
        }
    }

    private void createBoulder(int x, int y, int z, CustomBiome biome, Random random) {
        int size = random.nextBoolean() ? 2 : 3;
        for(int bx = 0; bx < size; bx++) {
            for(int by = 0; by < size; by++) {
                for(int bz = 0; bz < size; bz++) {
                    Material mat = random.nextBoolean() ? Material.COBBLESTONE : Material.MOSSY_COBBLESTONE;
                    boolean b = (y + by - 1) > BIOME_APPLY_MIN_Y;
                    blockQueue.add(new ExtendedBlockData(x + bx, y + by - 1, z + bz, mat, biome, null, false, 1, b));
                }
            }
        }
    }

    private void createTallGrass(int x, int y, int z, CustomBiome biome) {
        boolean b1 = y > BIOME_APPLY_MIN_Y;
        boolean b2 = (y + 1) > BIOME_APPLY_MIN_Y;
        blockQueue.add(new ExtendedBlockData(x, y, z, Material.TALL_GRASS, biome, Bisected.Half.BOTTOM, false, 1, b1));
        blockQueue.add(new ExtendedBlockData(x, y + 1, z, Material.TALL_GRASS, biome, Bisected.Half.TOP, false, 1, b2));
    }

    private void createLargeFern(int x, int y, int z, CustomBiome biome) {
        boolean b1 = y > BIOME_APPLY_MIN_Y;
        boolean b2 = (y + 1) > BIOME_APPLY_MIN_Y;
        blockQueue.add(new ExtendedBlockData(x, y, z, Material.LARGE_FERN, biome, Bisected.Half.BOTTOM, false, 1, b1));
        blockQueue.add(new ExtendedBlockData(x, y + 1, z, Material.LARGE_FERN, biome, Bisected.Half.TOP, false, 1, b2));
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

                if (blockQueue.isEmpty() && isCalculationDone) {
                    processStructureQueue(world);
                    processTreeQueue(world);

                    if (treeQueue.isEmpty() && structureQueue.isEmpty()) {
                        removeDroppedItems(world);
                        ConsoleLogger.info(WORLD_GENERATOR, "Biomes applied successfully");
                        WorldManager.isWorldGenerated = true;
                        VisualizePath.paths(instance);
                        this.cancel();
                    }
                } else if (blockQueue.isEmpty()) {
                    processStructureQueue(world);
                    processTreeQueue(world);
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

        if (data.applyBiome) {
            Biome mcBiome = mapToBiome(data.biomeType);
            world.setBiome(data.x, data.y, data.z, mcBiome);
        }

        if (data.material == null) return;

        Block b = world.getBlockAt(data.x, data.y, data.z);
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
            ((PinkPetals) bd).setFlowerAmount(4);
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
        boolean applyBiome;

        public ExtendedBlockData(int x, int y, int z, Material m, CustomBiome b, Bisected.Half h, boolean snowy, int petalCount, boolean applyBiome) {
            this.x = x; this.y = y; this.z = z; this.material = m; this.biomeType = b;
            this.half = h; this.isSnowy = snowy; this.petalCount = petalCount;
            this.applyBiome = applyBiome;
        }
    }
}