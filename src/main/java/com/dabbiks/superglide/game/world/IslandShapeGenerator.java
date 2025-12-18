package com.dabbiks.superglide.game.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import com.dabbiks.superglide.game.world.WorldGenManager.BlockSetData;

import java.util.Queue;
import java.util.Random;

public class IslandShapeGenerator {

    private static class GenerationContext {
        final Queue<BlockSetData> queue;
        final Location center;
        final int baseRadius;
        final Material material;
        final Random random;
        final SimplexOctaveGenerator detailNoise;
        final SimplexOctaveGenerator shapeNoise;
        final SimplexOctaveGenerator mountainNoise;
        final boolean isMountainous;
        final int maxDepth;
        final int cy;

        GenerationContext(Queue<BlockSetData> queue, Location center, int baseRadius, Material material, Random random,
                          SimplexOctaveGenerator detailNoise, SimplexOctaveGenerator shapeNoise,
                          SimplexOctaveGenerator mountainNoise, boolean isMountainous, int maxDepth) {
            this.queue = queue;
            this.center = center;
            this.baseRadius = baseRadius;
            this.material = material;
            this.random = random;
            this.detailNoise = detailNoise;
            this.shapeNoise = shapeNoise;
            this.mountainNoise = mountainNoise;
            this.isMountainous = isMountainous;
            this.maxDepth = maxDepth;
            this.cy = center.getBlockY();
        }
    }

    public static void generateToQueue(Queue<BlockSetData> queue, Location center, int baseRadius, Material material) {
        World world = center.getWorld();
        Random random = new Random(world.getSeed() + center.getBlockX() + center.getBlockZ());

        SimplexOctaveGenerator mountainNoise = null;
        boolean isMountainous = shouldGenerateMountains(baseRadius, random);

        if (isMountainous) {
            mountainNoise = new SimplexOctaveGenerator(random, 8);
            mountainNoise.setScale(0.012);
        }

        SimplexOctaveGenerator detailNoise = new SimplexOctaveGenerator(random, 8);
        detailNoise.setScale(0.08);

        SimplexOctaveGenerator shapeNoise = new SimplexOctaveGenerator(random, 8);
        shapeNoise.setScale(0.021);

        int maxDepth = calculateMaxDepth(baseRadius);
        int loopRange = (int) (baseRadius * 1.3);

        GenerationContext context = new GenerationContext(
                queue, center, baseRadius, material, random,
                detailNoise, shapeNoise, mountainNoise, isMountainous, maxDepth
        );

        scanArea(context, loopRange);
    }

    private static boolean shouldGenerateMountains(int baseRadius, Random random) {
        return baseRadius > 40 && random.nextDouble() < 0.40;
    }

    private static int calculateMaxDepth(int baseRadius) {
        int depth = (int) (baseRadius * 0.9);
        return Math.min(depth, 60);
    }

    private static void scanArea(GenerationContext ctx, int loopRange) {
        for (int x = -loopRange; x <= loopRange; x++) {
            for (int z = -loopRange; z <= loopRange; z++) {
                processColumn(ctx, x, z);
            }
        }
    }

    private static void processColumn(GenerationContext ctx, int x, int z) {
        double dist = Math.sqrt(x * x + z * z);
        int cx = ctx.center.getBlockX();
        int cz = ctx.center.getBlockZ();

        double irregularRadius = calculateIrregularRadius(ctx, cx + x, cz + z, ctx.baseRadius);

        if (dist > irregularRadius) return;

        int surfaceY = calculateSurfaceY(ctx, cx + x, cz + z, dist, irregularRadius);
        fillVerticalColumn(ctx, x, z, dist, irregularRadius, surfaceY);
    }

    private static double calculateIrregularRadius(GenerationContext ctx, int realX, int realZ, int baseRadius) {
        double shapeMod = ctx.shapeNoise.noise(realX, realZ, 0.5, 0.5);
        return baseRadius * (0.75 + (shapeMod * 0.6));
    }

    private static int calculateSurfaceY(GenerationContext ctx, int realX, int realZ, double dist, double irregularRadius) {
        double edgeRatio = dist / irregularRadius;
        double edgeFalloff = Math.pow(edgeRatio, 3) * 10.0;
        double surfaceVar = ctx.detailNoise.noise(realX, realZ, 0.5, 0.5) * 3.5;
        double mountainHeight = calculateMountainHeight(ctx, realX, realZ);

        return (int) (ctx.cy + surfaceVar + mountainHeight - edgeFalloff);
    }

    private static double calculateMountainHeight(GenerationContext ctx, int realX, int realZ) {
        if (!ctx.isMountainous) return 0;

        double mVal = ctx.mountainNoise.noise(realX, realZ, 0.5, 0.5);
        if (mVal <= 0.1) return 0;

        return (mVal - 0.1) * 25.0;
    }

    private static void fillVerticalColumn(GenerationContext ctx, int x, int z, double dist, double irregularRadius, int surfaceY) {
        for (int y = surfaceY + 2; y >= ctx.cy - ctx.maxDepth; y--) {
            processBlockCandidate(ctx, x, y, z, dist, irregularRadius, surfaceY);
        }
    }

    private static void processBlockCandidate(GenerationContext ctx, int x, int y, int z, double dist, double irregularRadius, int surfaceY) {
        if (y > surfaceY) return;

        double depthRatio = (double) (ctx.cy - y) / ctx.maxDepth;
        double currentRadiusLimit = irregularRadius * (1.0 - Math.pow(depthRatio, 1.2));

        int realX = ctx.center.getBlockX() + x;
        int realZ = ctx.center.getBlockZ() + z;

        double wallNoise = ctx.detailNoise.noise(realX, y, realZ, 0.5, 0.5) * 5.0;

        if (dist >= currentRadiusLimit + wallNoise) return;

        ctx.queue.add(new BlockSetData(realX, y, realZ, ctx.material));
    }
}
