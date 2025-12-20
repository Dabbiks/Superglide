package com.dabbiks.superglide.player.data.persistent;

import com.dabbiks.superglide.cosmetics.cages.Cage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistentData {

    // * ----------------------------------------------------------------

    private UUID playerId;
    private String playerName;
    private Map<PersistentStats, Integer> stats = new HashMap<>();

    // * ----------------------------------------------------------------

    public UUID getPlayerId() { return playerId; }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Map<PersistentStats, Integer> getStats() { return stats; }
    public void addStat(PersistentStats stat, int amount) { stats.put(stat, getStats().getOrDefault(stat, 0) + amount); }
    public void removeStat(PersistentStats stat, int amount) { stats.put(stat, getStats().getOrDefault(stat, 0) - amount); }
    public void setStat(PersistentStats stat, int amount) { stats.put(stat, amount); }

    // * COSMETICS

    private Cage cage;

    public Cage getCage() { return cage; }
    public void setCage(Cage cage) { this.cage = cage; }

}
