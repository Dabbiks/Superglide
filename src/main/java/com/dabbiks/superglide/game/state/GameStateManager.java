package com.dabbiks.superglide.game.state;

public class GameStateManager {

    private static GameState gameState = GameState.WAIT;
    public static GameState getGameState() { return gameState; }
    public static void setGameState(GameState state) { gameState = state; }

}
