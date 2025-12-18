package com.dabbiks.superglide.game.teams;

import java.util.ArrayList;
import java.util.List;

public class TeamLoader {

    private static final List<TeamData> teams = new ArrayList<>();

    public static void initiateTeams() {
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
    }

    public static TeamData createTeams() {
        for (TeamData team : teams) {
            TeamManager.createTeam(team.getName());
        }
        return null;
    }

}
