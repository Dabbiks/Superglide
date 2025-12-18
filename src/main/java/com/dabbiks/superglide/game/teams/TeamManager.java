package com.dabbiks.superglide.game.teams;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.ConsoleLogger.Type;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

import static com.dabbiks.superglide.Superglide.groupU;

public class TeamManager {

    public static ScoreboardManager manager = Bukkit.getScoreboardManager();
    public static Scoreboard scoreboard = manager.getMainScoreboard();

    public static void createTeam(String name) {
        scoreboard.registerNewTeam(name);
    }

    public static void removeTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team == null) return;
        team.unregister();
    }

    public static void removeAllTeams() {
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
    }

    public static List<Player> getPlayersFromTeam(String name) {
        Team team = scoreboard.getTeam(name);

        if (team == null) {
            return Collections.emptyList();
        }

        return team.getEntries().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Team getTeamFromPlayer(Player player) {
        return scoreboard.getEntryTeam(player.getName());
    }

    public static void addPlayerToTeam(Player player, String name) {
        Team team = scoreboard.getTeam(name);
        if (team == null) return;
        team.addEntry(player.getName());
    }

    public static void removePlayerFromTeam(Player player, String name) {
        Team team = scoreboard.getTeam(name);
        if (team == null) return;
        team.removeEntry(player.getName());
    }

    public static void distributePlayersToExistingTeams() {
        List<Team> teams = new ArrayList<>(scoreboard.getTeams());

        if (teams.isEmpty()) {
            ConsoleLogger.warning(Type.PLUGIN, "There are no teams!");
            return;
        }

        List<Player> players = groupU.getAllPlayers();
        Collections.shuffle(players);

        int maxTeamSize = Constants.teamSize;
        int playerIndex = 0;

        for (Team team : teams) {
            for (int i = 0; i < maxTeamSize; i++) {
                if (playerIndex >= players.size()) return;

                Player player = players.get(playerIndex);
                team.addEntry(player.getName());
                playerIndex++;
            }
        }

        if (playerIndex < players.size()) {
            ConsoleLogger.warning(Type.PLUGIN, "Not enough teams!");
        }
    }

}
