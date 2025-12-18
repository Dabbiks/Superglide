package com.dabbiks.superglide.player.traffic;

import com.dabbiks.superglide.player.state.PlayerState;
import com.dabbiks.superglide.player.state.PlayerStateManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerStateManager.setPlayerState(event.getPlayer().getUniqueId(), PlayerState.WAITING);
    }
}
