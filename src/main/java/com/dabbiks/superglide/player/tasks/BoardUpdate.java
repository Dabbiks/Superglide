package com.dabbiks.superglide.player.tasks;

import com.dabbiks.superglide.player.scoreboard.BoardManager;
import com.dabbiks.superglide.player.state.PlayerState;
import com.dabbiks.superglide.player.state.PlayerStateManager;
import com.dabbiks.superglide.tasks.Task;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.entity.Player;

import static com.dabbiks.superglide.Superglide.groupU;

public class BoardUpdate extends Task {

    protected long getPeriod() {
        return 20;
    }

    protected void tick() {
        for (Player player : groupU.getAllPlayers()) {
            PlayerState state = PlayerStateManager.getPlayerState(player.getUniqueId());
            if (state == PlayerState.WAITING && player.getWorld().equals(Constants.world)) {
                BoardManager.setLobbyBoard(player);
                continue;
            }
            if (state == PlayerState.PLAYING) {
                BoardManager.setInGameBoard(player);
                continue;
            }
            BoardManager.setSpectatorBoard(player);
        }
    }

}
