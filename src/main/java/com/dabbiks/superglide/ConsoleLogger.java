package com.dabbiks.superglide;

import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class ConsoleLogger {

    Logger logger = Bukkit.getLogger();

    public void info(Type type, String string) {
        logger.info("[" + type.name() + "] " + string);
    }

    public void warning(Type type, String string) {
        logger.warning("[" + type.name() + "] " + string);
    }

    public enum Type {
        PLAYER_DATA,
        WORLD_GENERATOR
    }

}
