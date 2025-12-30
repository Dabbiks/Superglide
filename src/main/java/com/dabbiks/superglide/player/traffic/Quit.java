package com.dabbiks.superglide.player.traffic;

import com.dabbiks.superglide.player.data.persistent.PersistentDataManager;
import com.dabbiks.superglide.player.scoreboard.BoardManager;
import com.dabbiks.superglide.player.state.PlayerState;
import com.dabbiks.superglide.player.state.PlayerStateManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Quit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PlayerStateManager.removePlayerState(player.getUniqueId());
        PersistentDataManager.saveData(player.getUniqueId());
        PersistentDataManager.delData(player.getUniqueId());

        event.setQuitMessage("- " + player.name());
    }
}
