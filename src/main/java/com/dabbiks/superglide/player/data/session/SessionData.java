package com.dabbiks.superglide.player.data.session;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static com.dabbiks.superglide.Superglide.plugin;
import static com.dabbiks.superglide.Superglide.timeU;

public class SessionData {

    private final Map<SessionStats, Integer> stats = new HashMap<>();
    private final EnumSet<SessionTag> tags = EnumSet.noneOf(SessionTag.class);
    private final Map<SessionTag, Integer> tagTasks = new HashMap<>();

    // ------------------------------------------------------------------

    public void addStat(SessionStats stat, int amount) { stats.put(stat, stats.getOrDefault(stat, 0) + amount); }
    public void removeStat(SessionStats stat, int amount) { stats.put(stat, stats.getOrDefault(stat, 0) - amount); }
    public int getStat(SessionStats stat) { return stats.getOrDefault(stat, 0); }

    public void addTag(SessionTag tag) { tags.add(tag); }
    public void addTagForTicks(SessionTag tag, long ticks) {
        addTag(tag);
        int taskID = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    SessionData data = this;
                    data.removeTag(tag);
                },
                ticks
        ).getTaskId();
        Integer oldTask = tagTasks.put(tag, taskID);
        if (oldTask != null) Bukkit.getScheduler().cancelTask(oldTask);
    }
    public void removeTag(SessionTag tag) {
        tags.remove(tag);
        Integer taskID = tagTasks.remove(tag);
        if (taskID != null) Bukkit.getScheduler().cancelTask(taskID);
    }
    public void clearTags() {
        for (int taskID : tagTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
        tags.clear();
        tagTasks.clear();
    }
    public boolean hasTag(SessionTag tag) { return tags.contains(tag); }

    // * ----------------------------------------------------------------

    private Player attacker;
    private long attackerTime;
    private final Map<Player, Long> assists = new HashMap<>();

    // ------------------------------------------------------------------

    public void setAttacker(Player attacker) {
        if (this.attacker != null && this.attacker != attacker) {
            assists.put(this.attacker, attackerTime);
            attackerTime = timeU.getTime();
            this.attacker = attacker;
            assists.remove(attacker);
        } else {
            this.attacker = attacker;
            attackerTime = timeU.getTime();
        }
    }
    public Player getAttacker() { return attacker; }
    public long getAttackerTime() { return attackerTime; }
    public List<Player> getAssists() {
        List<Player> recentAssists = new ArrayList<>();
        for (Player player : assists.keySet()) {
            if (assists.get(player) < timeU.getTime() - 180) continue;
            recentAssists.add(player);
        }
        return recentAssists;
    }

    // * ----------------------------------------------------------------

    private int rankPointsModifier;

    // ------------------------------------------------------------------
    // * ----------------------------------------------------------------

    private Team team;

    // ------------------------------------------------------------------

    public void setTeam(Team team) { this.team = team; }
    public Team getTeam() { return team; }

    // * ----------------------------------------------------------------


}
