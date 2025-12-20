package com.dabbiks.superglide.game.teams;

import com.dabbiks.superglide.cosmetics.cages.Cage;
import com.dabbiks.superglide.game.world.SchematicPaster;
import com.dabbiks.superglide.player.data.persistent.PersistentData;
import com.dabbiks.superglide.player.data.persistent.PersistentDataManager;
import com.dabbiks.superglide.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

import static com.dabbiks.superglide.Superglide.plugin;

public class TeamTeleport {

    public static List<Location> getSpawnLocations() {
        List<Location> locations = new ArrayList<>();
        World world = Constants.world;

        if (world == null) return locations;

        int totalTeams = Constants.maxPlayerCount / Constants.teamSize;
        double radius = 200.0;
        double centerX = 0.0;
        double centerZ = 0.0;
        double y = 300.0;

        for (int i = 0; i < totalTeams; i++) {
            double angle = 2 * Math.PI * i / totalTeams;

            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);

            locations.add(new Location(world, x, y, z));
        }
        return locations;
    }

    public static void setupTeamsAndTeleport(List<Location> locations, List<Team> teams) {
        World world = Constants.world;
        if (world == null) return;

        for (int i = 0; i < teams.size(); i++) {
            if (i >= locations.size()) break;

            Team team = teams.get(i);
            List<Player> players = TeamManager.getPlayersFromTeam(team.getName());
            if (players.isEmpty()) continue;

            Location spawnLoc = locations.get(i);

            players.getFirst();
            PersistentData data = PersistentDataManager.getData(players.getFirst().getUniqueId());
            String schematic = data.getCage().name().toLowerCase();

            if (schematic.isEmpty()) schematic = "default_cage";
            data.setCage(Cage.DEFAULT_CAGE);

            SchematicPaster.pasteSchematic(schematic, spawnLoc);

            Location tpLoc = spawnLoc.clone().add(0.5, 1, 0.5);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player player : players) {
                    player.teleport(tpLoc);
                }
            }, 20L);
        }
    }

}
