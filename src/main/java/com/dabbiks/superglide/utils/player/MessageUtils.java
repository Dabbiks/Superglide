package com.dabbiks.superglide.utils.player;

import org.bukkit.entity.Player;

import java.util.List;

public class MessageUtils {

    public void sendMessage(List<Player> players, String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }
}
