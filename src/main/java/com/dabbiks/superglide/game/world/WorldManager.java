package com.dabbiks.superglide.game.world;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.*;

import java.io.Console;
import java.io.File;

import static com.dabbiks.superglide.Superglide.plugin;

public class WorldManager {

    public void createWorld() {
        File worldFolder     = new File(Bukkit.getWorldContainer(), Constants.worldName);

        if (worldFolder.exists()) {
            generateMap();
            return;
        }

        WorldCreator creator = new WorldCreator(Constants.worldName);
        creator.createWorld();
        Constants.world = Bukkit.getWorld(Constants.worldName);

        ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "World successfully created");
    }

    public void setRules() {
        World world = Constants.world;

        world.setGameRule(GameRules.PVP, false);
        world.setGameRule(GameRules.LOCATOR_BAR, false);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.SPAWN_PATROLS, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRules.REDUCED_DEBUG_INFO, false);
        world.setGameRule(GameRules.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
        world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        world.setGameRule(GameRules.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);
        world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);

        ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Game rules successfully set");
    }

    public void generateMap() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ConsoleLogger.info(ConsoleLogger.Type.WORLD_GENERATOR, "Map generator started");
            new WorldGenManager().startProcess();
        }, 600L);
    }

}
