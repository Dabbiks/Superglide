package com.dabbiks.superglide.game.tasks;

import com.dabbiks.superglide.game.state.GameState;
import com.dabbiks.superglide.game.state.GameStateManager;
import com.dabbiks.superglide.tasks.Task;

import static com.dabbiks.superglide.Superglide.timeU;

public class Time extends Task {

    protected long getPeriod() {
        return 20;
    }

    protected void tick() {
        if (!(GameStateManager.getGameState() == GameState.PLAY)) return;
        timeU.incrementTime();
    }

}
