package com.dabbiks.superglide;

import com.dabbiks.superglide.player.data.persistent.PersistentDataJson;
import com.dabbiks.superglide.utils.other.TimeUtils;
import com.dabbiks.superglide.utils.player.GroupUtils;
import com.dabbiks.superglide.utils.player.MessageUtils;
import com.dabbiks.superglide.utils.player.SoundUtils;
import com.dabbiks.superglide.utils.player.TitleUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Superglide extends JavaPlugin {

    public static Plugin plugin;
    public static Superglide instance;

    public static PersistentDataJson persistentDataJson;

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

        groupU   = new GroupUtils();
        messageU = new MessageUtils();
        soundU   = new SoundUtils();
        titleU   = new TitleUtils();
        timeU    = new TimeUtils();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
