package com.dabbiks.superglide.game.world.shops;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.game.world.pathfinder.PathSmoother;
import com.dabbiks.superglide.game.world.pathfinder.Pathfinder;
import com.dabbiks.superglide.game.world.shops.gunshop.GunShopEntity;
import com.dabbiks.superglide.game.world.shops.gunshop.GunShopGui;
import com.dabbiks.superglide.game.world.shops.itemshop.ItemShopEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

import static com.dabbiks.superglide.Superglide.plugin;

public class ShopManager {

    private HashMap<Integer, Location> path = new HashMap<>();

    public void spawnShops() {
        findPathForShops();

        ItemShopEntity itemShop = new ItemShopEntity(path, 0);
        GunShopEntity gunShop = new GunShopEntity(path, (int) (path.size()*0.33));
    }

    private void findPathForShops() {
        Pathfinder.generatePathAsync(175, 325, 180, 160).thenAccept(rawPathMap -> {

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (rawPathMap != null && !rawPathMap.isEmpty()) {

                    path = PathSmoother.smoothPath(rawPathMap, 20);

                    ConsoleLogger.info(ConsoleLogger.Type.PATHFINDER,
                            "Path found (raw " + rawPathMap.size() + ") (smooth " + path.size());

                } else {
                    ConsoleLogger.warning(ConsoleLogger.Type.PATHFINDER, "Could not generate path. HashMap is empty");
                }
            });


        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
