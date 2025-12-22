package com.dabbiks.superglide;

import com.dabbiks.superglide.game.teams.TeamLoader;
import com.dabbiks.superglide.game.teams.TeamManager;
import com.dabbiks.superglide.game.world.WorldManager;
import com.dabbiks.superglide.items.ItemManager;
import com.dabbiks.superglide.player.data.persistent.PersistentDataJson;
import com.dabbiks.superglide.player.traffic.Join;
import com.dabbiks.superglide.player.traffic.Quit;
import com.dabbiks.superglide.tasks.TaskManager;
import com.dabbiks.superglide.utils.other.TimeUtils;
import com.dabbiks.superglide.utils.player.GroupUtils;
import com.dabbiks.superglide.utils.player.MessageUtils;
import com.dabbiks.superglide.utils.player.SoundUtils;
import com.dabbiks.superglide.utils.player.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Superglide extends JavaPlugin {

    public static Plugin plugin;
    public static Superglide instance;

    public static PersistentDataJson persistentDataJson;
    public static ItemManager itemManager;

    public static GroupUtils groupU;
    public static MessageUtils messageU;
    public static SoundUtils soundU;
    public static TitleUtils titleU;
    public static TimeUtils timeU;

    @Override
    public void onEnable() {
        plugin   = this;
        instance = this;

        persistentDataJson = new PersistentDataJson();
        itemManager = new ItemManager();

        groupU   = new GroupUtils();
        messageU = new MessageUtils();
        soundU   = new SoundUtils();
        titleU   = new TitleUtils();
        timeU    = new TimeUtils();

        // * -----------------------------------------

        TeamManager.removeAllTeams();
        TeamLoader.initiateTeams();
        TeamLoader.createTeams();

        WorldManager.createWorld();

        new TaskManager();

        Bukkit.getPluginManager().registerEvents(new Join(), this);
        Bukkit.getPluginManager().registerEvents(new Quit(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
