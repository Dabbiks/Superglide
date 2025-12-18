package com.dabbiks.superglide.utils.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TitleUtils {

    public void sendTitle(List<Player> players, String title, String subtitle, int ticks) {
        for (Player player : players) {
            player.sendTitle(title, subtitle, 0, ticks, 0);
        }
    }

}
