package com.dabbiks.superglide.game.world.pathfinder;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisualizePath {

    private static final List<Integer> activeTasks = new ArrayList<>();

    public static void visualizePath(HashMap<Integer, Location> pathMap, Color color) {
        if (pathMap == null || pathMap.isEmpty()) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        int size = pathMap.size();

        for (int i = 0; i < size; i++) {
            Location current = pathMap.get(i);
            if (current == null) continue;

            current.getWorld().spawnParticle(Particle.DUST, current, 1, 0, 0, 0, 0, dustOptions);
        }
    }

    // ! TEST
    public static void paths(JavaPlugin plugin) {
        stopAllVisualizations();

        generateAndVisualize(plugin, 160, 360, 185, 150, Color.RED);
        generateAndVisualize(plugin, 150, 375, 183, 150, Color.BLUE);
        generateAndVisualize(plugin, 175, 325, 180, 160, Color.GREEN);
        generateAndVisualize(plugin, 175, 325, 180, 160, Color.YELLOW);
    }
    // !

    public static void stopAllVisualizations() {
        for (int taskId : activeTasks) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        activeTasks.clear();
    }

    private static void generateAndVisualize(JavaPlugin plugin, int minR, int maxR, int hMin, int hMax, Color color) {
        Pathfinder.generatePathAsync(minR, maxR, hMin, hMax).thenAccept(rawPathMap -> {

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (rawPathMap != null && !rawPathMap.isEmpty()) {

                    HashMap<Integer, Location> smoothPath = PathSmoother.smoothPath(rawPathMap, 20);

                    plugin.getLogger().info("Pathfinder zakończony. Surowe punkty: " + rawPathMap.size() +
                            ", Gładkie punkty: " + smoothPath.size());

                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        visualizePath(smoothPath, color);
                    }, 0L, 2L);

                    activeTasks.add(task.getTaskId());

                } else {
                    plugin.getLogger().warning("Nie udało się wygenerować ścieżki (" + color.asRGB() + ") - pusta mapa.");
                }
            });

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}