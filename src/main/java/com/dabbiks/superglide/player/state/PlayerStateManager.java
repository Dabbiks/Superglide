package com.dabbiks.superglide.player.state;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStateManager {

    public static final Map<UUID, PlayerState> playerStates = new HashMap<>();
    public static PlayerState getPlayerState(UUID uuid) { return playerStates.getOrDefault(uuid, PlayerState.WAITING); }
    public static void setPlayerState(UUID uuid, PlayerState state) { playerStates.put(uuid, state); }
    public static void removePlayerState(UUID uuid) { playerStates.remove(uuid); }

}
