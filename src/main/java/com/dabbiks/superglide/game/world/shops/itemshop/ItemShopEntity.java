package com.dabbiks.superglide.game.world.shops.itemshop;

import com.dabbiks.superglide.game.world.shops.ShopEntity;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;

import static com.dabbiks.superglide.Superglide.symbols;

public class ItemShopEntity {

    private final ShopEntity itemShopEntity;

    public ItemShopEntity(HashMap<Integer, Location> track, int trackIndex) {
        itemShopEntity = new ShopEntity(track, trackIndex);

        itemShopEntity.spawn();
        itemShopEntity.applySkinToMannequin(symbols.itemShopPlayer);
        itemShopEntity.setHarness(Material.YELLOW_HARNESS);
        itemShopEntity.startFlightTask();
    }

}
