package com.dabbiks.superglide.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class Constants {

    // * LOBBY

    public static final int countdown      = 60;
    public static final int teamSize       = 1;
    public static final int minPlayerCount = teamSize * 4;
    public static final int maxPlayerCount = teamSize * 16;

    // * WORLD

    public static final String worldName  = "game";
    public static final double borderSize = 400;
    public static World world = Bukkit.getWorld(worldName);

}
