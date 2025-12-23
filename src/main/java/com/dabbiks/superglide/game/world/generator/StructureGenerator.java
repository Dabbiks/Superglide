package com.dabbiks.superglide.game.world.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class StructureGenerator {

    private static final List<LootItem> TEMPLE_LOOT = Arrays.asList(
            new LootItem(Material.IRON_INGOT, 2, 8, 0.7),
            new LootItem(Material.GOLD_INGOT, 1, 6, 0.5),
            new LootItem(Material.DIAMOND, 1, 3, 0.3),
            new LootItem(Material.EMERALD, 1, 4, 0.2),
            new LootItem(Material.BONE, 4, 12, 0.6),
            new LootItem(Material.ROTTEN_FLESH, 2, 10, 0.6),
            new LootItem(Material.SADDLE, 1, 1, 0.1)
    );

    private static final List<LootItem> PYRAMID_LOOT = Arrays.asList(
            new LootItem(Material.GOLD_INGOT, 2, 14, 0.8),
            new LootItem(Material.BONE, 4, 15, 0.7),
            new LootItem(Material.ROTTEN_FLESH, 3, 15, 0.8),
            new LootItem(Material.GUNPOWDER, 1, 5, 0.5),
            new LootItem(Material.TNT, 1, 2, 0.3),
            new LootItem(Material.DIAMOND, 1, 3, 0.2),
            new LootItem(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 0.05),
            new LootItem(Material.GOLDEN_APPLE, 1, 2, 0.2)
    );

    private static final List<LootItem> PORTAL_LOOT = Arrays.asList(
            new LootItem(Material.GOLD_NUGGET, 5, 15, 0.8),
            new LootItem(Material.FLINT_AND_STEEL, 1, 1, 0.4),
            new LootItem(Material.OBSIDIAN, 1, 3, 0.5),
            new LootItem(Material.FIRE_CHARGE, 1, 1, 0.4),
            new LootItem(Material.GOLDEN_APPLE, 1, 1, 0.2),
            new LootItem(Material.GOLDEN_AXE, 1, 1, 0.3),
            new LootItem(Material.GLISTERING_MELON_SLICE, 1, 3, 0.3)
    );

    private static final List<LootItem> MINE_LOOT = Arrays.asList(
            new LootItem(Material.GOLD_INGOT, 3, 12, 0.8),
            new LootItem(Material.IRON_INGOT, 2, 10, 0.7),
            new LootItem(Material.RAIL, 4, 16, 0.6),
            new LootItem(Material.POWERED_RAIL, 2, 8, 0.4),
            new LootItem(Material.TNT, 1, 3, 0.3),
            new LootItem(Material.TORCH, 4, 16, 0.8),
            new LootItem(Material.MINECART, 1, 1, 0.1),
            new LootItem(Material.CHEST_MINECART, 1, 1, 0.05),
            new LootItem(Material.DIAMOND, 1, 2, 0.15),
            new LootItem(Material.BREAD, 1, 3, 0.5)
    );

    // =================================================================================

    public enum StructureType {
        JUNGLE_TEMPLE,
        PYRAMID,
        PORTAL,
        MINE
    }

    private static class StructureConfig {
        final String schemFile;
        final List<LootItem> lootTable;
        final int scanSize;
        final int heightOffset;

        StructureConfig(String s, List<LootItem> l, int scan, int h) {
            this.schemFile = s; this.lootTable = l; this.scanSize = scan; this.heightOffset = h;
        }
    }

    private final List<Location> allStructures = new CopyOnWriteArrayList<>();

    private int jungleTempleCount = 0;
    private int pyramidCount = 0;
    private int portalCount = 0;
    private int mineCount = 0;

    private final SchematicPaster paster;
    private final Random random;

    private final Set<Material> INFECTABLE_BLOCKS = EnumSet.of(
            Material.SAND, Material.GRASS_BLOCK, Material.RED_SAND, Material.PODZOL,
            Material.DIRT, Material.COARSE_DIRT, Material.GRAVEL, Material.SNOW_BLOCK,
            Material.STONE, Material.WHITE_WOOL
    );

    public StructureGenerator() {
        this.paster = new SchematicPaster();
        this.random = new Random();
    }

    public synchronized boolean tryScheduleStructure(int x, int y, int z, World world, StructureType type) {
        Location loc = new Location(world, x, y, z);

        if (!checkStructureLimitsAndSafety(type, loc)) {
            return false;
        }

        int radiusToCheck = (type == StructureType.MINE) ? 8 : (type == StructureType.PORTAL ? 2 : 3);
        int depthToCheck = (type == StructureType.MINE) ? 5 : 5;

        if (!validateTerrain(world, x, y, z, radiusToCheck, depthToCheck)) {
            return false;
        }

        registerStructure(type, loc);
        return true;
    }

    private boolean checkStructureLimitsAndSafety(StructureType type, Location loc) {
        switch (type) {
            case JUNGLE_TEMPLE:
                if (jungleTempleCount >= 2) return false;
                return isSafeDistance(loc, 150);
            case PYRAMID:
                if (pyramidCount >= 2) return false;
                return isSafeDistance(loc, 150);
            case PORTAL:
                if (portalCount >= 4) return false;
                return isSafeDistance(loc, 200);
            case MINE:
                if (mineCount >= 2) return false;
                return isSafeDistance(loc, 50);
            default:
                return false;
        }
    }

    private void registerStructure(StructureType type, Location loc) {
        allStructures.add(loc);
        switch (type) {
            case JUNGLE_TEMPLE: jungleTempleCount++; break;
            case PYRAMID:       pyramidCount++;      break;
            case PORTAL:        portalCount++;       break;
            case MINE:          mineCount++;         break;
        }
    }

    private boolean isSafeDistance(Location loc, int minDistance) {
        double minDstSq = minDistance * minDistance;
        for (Location existing : allStructures) {
            if (existing.distanceSquared(loc) < minDstSq) {
                return false;
            }
        }
        return true;
    }

    public void generate(Location loc, StructureType type) {
        StructureConfig config = getStructureConfig(type);

        Location pasteLoc = loc.clone().add(0, config.heightOffset, 0);
        paster.pasteSchematic(config.schemFile, pasteLoc);

        if (type == StructureType.PORTAL) {
            infectTerrain(pasteLoc, 8);
            weatherPortal(pasteLoc, 5);
        }

        fillChestsInArea(pasteLoc, config.scanSize, config.scanSize, config.scanSize, config.lootTable);
    }

    private StructureConfig getStructureConfig(StructureType type) {
        switch (type) {
            case JUNGLE_TEMPLE: return new StructureConfig("jungle_temple.schem", TEMPLE_LOOT, 10, 0);
            case PYRAMID:       return new StructureConfig("pyramid.schem", PYRAMID_LOOT, 12, -1);
            case PORTAL:        return new StructureConfig("portal.schem", PORTAL_LOOT, 6, 0);
            case MINE:          return new StructureConfig("mine.schem", MINE_LOOT, 35, -1);
            default:            throw new IllegalArgumentException("Unknown structure type");
        }
    }

    private void infectTerrain(Location center, int radius) {
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                processInfectColumn(world, cx, cy, cz, x, z, radius);
            }
        }
    }

    private void processInfectColumn(World world, int cx, int cy, int cz, int x, int z, int radius) {
        double dist = Math.sqrt(x*x + z*z);
        if (dist > radius) return;

        double chance = 1.0 - (dist / (double)radius);
        if (random.nextDouble() >= chance) return;

        for (int y = -2; y <= 2; y++) {
            Block b = world.getBlockAt(cx + x, cy + y, cz + z);
            if (INFECTABLE_BLOCKS.contains(b.getType())) {
                b.setType(Material.NETHERRACK);
            }
        }
    }

    private void weatherPortal(Location center, int size) {
        World world = center.getWorld();
        int startX = center.getBlockX() - size;
        int endX = center.getBlockX() + size;
        int startY = center.getBlockY();
        int endY = center.getBlockY() + 8;
        int startZ = center.getBlockZ() - size;
        int endZ = center.getBlockZ() + size;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    attemptWeatherBlock(world.getBlockAt(x, y, z));
                }
            }
        }
    }

    private void attemptWeatherBlock(Block b) {
        if (b.getType() != Material.OBSIDIAN) return;
        if (random.nextDouble() >= 0.40) return;

        b.setType(Material.CRYING_OBSIDIAN);
    }

    private boolean validateTerrain(World world, int cx, int cy, int cz, int radius, int depthCheck) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (!isGroundFoundInColumn(world, cx + x, cy, cz + z, depthCheck)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isGroundFoundInColumn(World world, int x, int startY, int z, int depthCheck) {
        for (int y = 1; y <= depthCheck; y++) {
            Material mat = world.getBlockAt(x, startY - y, z).getType();
            if (mat == Material.CACTUS) return false;

            if (mat.isSolid() && !mat.toString().contains("LEAVES") && mat != Material.AIR) {
                return true;
            }
        }
        return false;
    }

    private void fillChestsInArea(Location center, int sizeX, int sizeY, int sizeZ, List<LootItem> lootTable) {
        World world = center.getWorld();
        int startX = center.getBlockX() - (sizeX / 2);
        int startY = center.getBlockY() - 3;
        int startZ = center.getBlockZ() - (sizeZ / 2);

        for (int x = 0; x < sizeX; x++) {
            for (int y = -4; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Block block = world.getBlockAt(startX + x, center.getBlockY() + y, startZ + z);
                    processPotentialChest(block, lootTable);
                }
            }
        }
    }

    private void processPotentialChest(Block block, List<LootItem> lootTable) {
        if (!(block.getState() instanceof Chest)) return;
        fillSingleChest((Chest) block.getState(), lootTable);
    }

    private void fillSingleChest(Chest chest, List<LootItem> lootTable) {
        Inventory inv = chest.getInventory();
        inv.clear();
        for (LootItem item : lootTable) {
            processLootItem(inv, item);
        }
    }

    private void processLootItem(Inventory inv, LootItem item) {
        if (random.nextDouble() > item.chance) return;

        int amount = item.min + random.nextInt(item.max - item.min + 1);
        for (int i = 0; i < amount; i++) {
            addItemToInventory(inv, item);
        }
    }

    private void addItemToInventory(Inventory inv, LootItem item) {
        int slot = random.nextInt(inv.getSize());
        ItemStack current = inv.getItem(slot);

        if (current == null || current.getType() == Material.AIR) {
            inv.setItem(slot, new ItemStack(item.material, 1));
            return;
        }

        if (current.getType() == item.material && current.getAmount() < current.getMaxStackSize()) {
            current.setAmount(current.getAmount() + 1);
            return;
        }

        inv.addItem(new ItemStack(item.material, 1));
    }

    private static class LootItem {
        Material material;
        int min, max;
        double chance;
        public LootItem(Material m, int min, int max, double chance) {
            this.material = m; this.min = min; this.max = max; this.chance = chance;
        }
    }
}
