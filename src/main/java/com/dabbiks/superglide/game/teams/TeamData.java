package com.dabbiks.superglide.game.teams;

public class TeamData {

    private final String name;
    private final String banner;
    private final String bigIcon;
    private final String smallIcon;

    public TeamData(String name, String banner, String bigIcon, String smallIcon) {
        this.name = name;
        this.banner = banner;
        this.bigIcon = bigIcon;
        this.smallIcon = smallIcon;
    }

    public String getName() {
        return name;
    }

    public String getBanner() {
        return banner;
    }

    public String getBigIcon() {
        return bigIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }
}
