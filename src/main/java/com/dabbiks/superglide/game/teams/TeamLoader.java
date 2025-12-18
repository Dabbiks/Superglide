package com.dabbiks.superglide.game.teams;

import java.util.ArrayList;
import java.util.List;

public class TeamLoader {

    private static final List<TeamData> teams = new ArrayList<>();

    public static void initiateTeams() {
        teams.add(new TeamData("Team 1", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 2", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 3", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 4", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 5", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 6", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 7", "Baner", "BI", "SI"));
        teams.add(new TeamData("Team 8", "Baner", "BI", "SI"));
    }

    public static void createTeams() {
        for (TeamData team : teams) {
            TeamManager.createTeam(team.getName());
        }
    }

}
