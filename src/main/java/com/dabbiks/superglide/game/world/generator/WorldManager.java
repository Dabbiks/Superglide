package com.dabbiks.superglide.game.world.generator;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.*;

import static com.dabbiks.superglide.Superglide.plugin;

public class WorldManager {

    public static boolean isWorldGenerated;

    public static void createWorld() {
        WorldCreator creator = new WorldCreator(Constants.worldName);
        creator.createWorld();
        ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "World successfully created");

        Constants.world = Bukkit.getWorld(Constants.worldName);
        Bukkit.broadcastMessage("" + Bukkit.getWorld(Constants.worldName).getName());

        setRules();
        generateMap();
    }

    private static void setRules() {
        World world = Constants.world;

        world.setGameRule(GameRule.PVP, false);
        world.setGameRule(GameRule.LOCATOR_BAR, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);

        ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Game rules successfully set");
    }

    private static void generateMap() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Map generator started");
            new WorldGenManager().startProcess();
        }, 20);
    }

}
