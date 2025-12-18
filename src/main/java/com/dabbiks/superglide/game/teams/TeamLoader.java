package com.dabbiks.superglide.game.teams;

import java.util.ArrayList;
import java.util.List;

public class TeamLoader {

    private static final List<TeamData> teams = new ArrayList<>();

    public static void createTeams() {
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
    }

    public static List<TeamData> getTeams() { return teams; }

    public static TeamData getTeamByName(String name) {
        for (TeamData team : teams) {
            if (team.getName().equals(name)) return team;
        }
        return null;
    }

}
