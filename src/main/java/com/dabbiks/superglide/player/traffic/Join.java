package com.dabbiks.superglide.player.traffic;

import com.dabbiks.superglide.player.data.persistent.PersistentDataManager;
import com.dabbiks.superglide.player.state.PlayerState;
import com.dabbiks.superglide.player.state.PlayerStateManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerStateManager.setPlayerState(player.getUniqueId(), PlayerState.WAITING);
        PersistentDataManager.loadData(player.getUniqueId());
    }
}
