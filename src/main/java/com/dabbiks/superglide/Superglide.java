package com.dabbiks.superglide;

import com.dabbiks.superglide.player.data.persistent.PersistentDataJson;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Superglide extends JavaPlugin {

    public static Plugin plugin;
    public static PersistentDataJson persistentDataJson;

    @Override
    public void onEnable() {
        plugin = this;
        persistentDataJson = new PersistentDataJson();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
