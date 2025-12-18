package com.dabbiks.superglide.utils.player;

import com.dabbiks.superglide.player.state.PlayerState;
import com.dabbiks.superglide.player.state.PlayerStateManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupUtils {

    public List<Player> getPlayers(PlayerState state) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : PlayerStateManager.playerStates.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (!player.isOnline()) continue;
            if (PlayerStateManager.getPlayerState(uuid) == state) {
                players.add(player);
            }
        }
        return players;
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>(getPlayers(PlayerState.PLAYING));
        players.addAll(getPlayers(PlayerState.WAITING));
        return players;
    }

}
