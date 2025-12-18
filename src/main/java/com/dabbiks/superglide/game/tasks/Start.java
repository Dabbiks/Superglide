package com.dabbiks.superglide.game.tasks;

import com.dabbiks.superglide.game.state.GameState;
import com.dabbiks.superglide.game.state.GameStateManager;
import com.dabbiks.superglide.player.state.PlayerStateManager;
import com.dabbiks.superglide.tasks.Task;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static com.dabbiks.superglide.Superglide.*;

public class Start extends Task {

    private static int countdown = Constants.countdown;

    protected long getPeriod() { return 20; }

    protected void tick() {

        List<Player> players = groupU.getAllPlayers();
        GameState gameState = GameStateManager.getGameState();

        int playerCount = players.size();

        if (gameState == GameState.START && countdown >= 0) {
            titleU.sendTitle(players, "", "Odliczanie", 30);
            countdown--;
        }
        if (gameState == GameState.WAIT && playerCount >= Constants.minPlayerCount) {
            GameStateManager.setGameState(GameState.START);
        }
        if (gameState == GameState.START && playerCount < Constants.minPlayerCount) {
            GameStateManager.setGameState(GameState.WAIT);
            soundU.playSoundToPlayer(players, Sound.ENTITY_VILLAGER_NO, 0.3F, 1);
            titleU.sendTitle(players, "", "Odliczanie przerwane", 30);
            countdown = Constants.countdown;
        }
        if (gameState == GameState.START && countdown < 0) {
            GameStateManager.setGameState(GameState.PLAY);
            Constants.world.setTime(1000);
        }


    }

    public static int getCountdown() { return countdown; }
}
