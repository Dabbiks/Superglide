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

    private static List<Location> spawnLocations = new ArrayList<>();

    public static List<Location> calculateSpawnLocations() {
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
        setSpawnLocations(locations);

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

            String schematic = "";
            if (data.getCage() == null) { schematic = "default_cage.schem"; data.setCage(Cage.DEFAULT_CAGE); }
            if (schematic.isEmpty()) schematic = data.getCage().getName().toLowerCase();

            SchematicPaster.pasteSchematic(schematic, spawnLoc);

            Location tpLoc = spawnLoc.clone().add(0.5, 1, 0.5);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player player : players) {
                    player.teleport(tpLoc);
                }
            }, 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Location location : spawnLocations) {

                    int radius = 5;

                    for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
                        for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                            for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {

                                location.getWorld().getBlockAt(x, y, z).setType(Material.AIR);

                            }
                        }
                    }
                }
            }, 180L);
        }
    }

    private static void setSpawnLocations(List<Location> locations) {
        spawnLocations = locations;
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }
}
