package com.dabbiks.superglide.player.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElytraManager {

    public void startGliding(Player player) {
        player.setGliding(true);
    }

    public void stopGliding(Player player) {
        player.setGliding(false);
    }

    public boolean isGliding(Player player) {
        return player.isGliding();
    }

    public void boostVelocity(Player player, double multiplier) {
        Vector direction = player.getLocation().getDirection();
        player.setVelocity(direction.multiply(multiplier));
    }

    public void modifyVelocity(Player player, double x, double y, double z) {
        Vector currentVelocity = player.getVelocity();
        player.setVelocity(currentVelocity.add(new Vector(x, y, z)));
    }

}
