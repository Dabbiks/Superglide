package com.dabbiks.superglide.game.world.shops.gunshop;

import com.dabbiks.superglide.game.world.shops.ShopEntity;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;

import static com.dabbiks.superglide.Superglide.symbols;

public class GunShopEntity {

    private final ShopEntity gunShopEntity;

    public GunShopEntity(HashMap<Integer, Location> track, int trackIndex) {
        gunShopEntity = new ShopEntity(track, trackIndex);

        gunShopEntity.spawn();
        gunShopEntity.applySkinToMannequin(symbols.itemShopPlayer);
        gunShopEntity.setHarness(Material.YELLOW_HARNESS);
        gunShopEntity.startFlightTask();
    }

}
