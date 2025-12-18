package com.dabbiks.superglide;

import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class ConsoleLogger {

    private static final Logger logger = Bukkit.getLogger();

    public static void info(Type type, String string) {
        logger.info("[" + type.name() + "] " + string);
    }

    public static void warning(Type type, String string) {
        logger.warning("[" + type.name() + "] " + string);
    }

    public enum Type {
        PLAYER_DATA,
        WORLD_GENERATOR,
        PLUGIN
    }

}
