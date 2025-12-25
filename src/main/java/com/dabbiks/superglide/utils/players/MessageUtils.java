package com.dabbiks.superglide.utils.players;

import org.bukkit.entity.Player;

import java.util.List;

public class MessageUtils {

    public void sendMessageToPlayer(Player player, String message) {
        player.sendMessage(message);
    }

    public void sendMessageToPlayers(List<Player> players, String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }
}
