package com.dabbiks.superglide.utils.player;

import org.bukkit.entity.Player;

import java.util.List;

public class TitleUtils {

    public void sendTitleToPlayer(Player player, String title, String subtitle, int ticks) {
        player.sendTitle(title, subtitle, 0, ticks, 0);
    }

    public void sendTitleToPlayers(List<Player> players, String title, String subtitle, int ticks) {
        for (Player player : players) {
            player.sendTitle(title, subtitle, 0, ticks, 0);
        }
    }

}
