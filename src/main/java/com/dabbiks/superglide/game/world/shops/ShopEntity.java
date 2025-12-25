package com.dabbiks.superglide.game.world.shops;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.utils.Constants;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.dabbiks.superglide.Superglide.plugin;

public class ShopEntity {

    public static final List<ShopEntity> shopEntities = new ArrayList<>();

    private HappyGhast happyGhast;
    private Mannequin mannequin;
    private BukkitTask flightTask;
    private final HashMap<Integer, Location> track;

    private int trackIndex;
    private int boostedTicks;

    public ShopEntity(HashMap<Integer, Location> track, int trackIndex) {
        this.track = track;
        this.trackIndex = trackIndex;

        shopEntities.add(this);
    }

    public void spawn() {
        World world = Constants.world;
        if (world == null) {
            ConsoleLogger.warning(ConsoleLogger.Type.WORLD_GENERATOR, "Error while spawning ShopEntity: World is null!");
            return;
        }

        HappyGhast happyGhast = (HappyGhast) world.spawnEntity(track.get(1), EntityType.HAPPY_GHAST);

        happyGhast.setAI(false);
        happyGhast.setGravity(false);
        happyGhast.setSilent(true);
        happyGhast.setInvulnerable(true);
        happyGhast.setPersistent(true);
        happyGhast.setRemoveWhenFarAway(false);
        this.happyGhast = happyGhast;

        Mannequin mannequin = (Mannequin) world.spawnEntity(track.get(1), EntityType.MANNEQUIN);

        mannequin.setAI(false);
        mannequin.setGravity(false);
        mannequin.setSilent(true);
        mannequin.setInvulnerable(true);
        mannequin.setPersistent(true);
        mannequin.setRemoveWhenFarAway(false);
        this.mannequin = mannequin;

        happyGhast.addPassenger(mannequin);
    }

    public void applySkinToMannequin(String textureValue) {
        UUID uuid = UUID.nameUUIDFromBytes(textureValue.getBytes());
        PlayerProfile legacyProfile = Bukkit.createProfile(uuid, null);
        legacyProfile.setProperty(new ProfileProperty("textures", textureValue));

        mannequin.setProfile(ResolvableProfile.resolvableProfile(legacyProfile));
    }

    public void setHarness(Material material) {
        if (happyGhast.getEquipment() == null) return;
        happyGhast.getEquipment().setItem(EquipmentSlot.BODY, new ItemStack(material));
    }

    public void startFlightTask() {
        flightTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (happyGhast == null) {
                    flightTask.cancel();
                    return;
                }

                boolean isStopped = isPlayerClose() || isOtherShopOnTrack();
                if (isStopped) boostedTicks++;

                boolean isBoosted = boostedTicks > 0;
                if (!isStopped) {
                    updatePosition(getNextPosition(isBoosted));
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updatePosition(int trackIndex) {
        happyGhast.teleport(track.getOrDefault(trackIndex, new Location(Constants.world, 0, 300, 0)));
    }

    private int getNextPosition(boolean isTickBoosted) {
        int nextIndex = trackIndex + 1;
        if (isTickBoosted) nextIndex++;
        if (nextIndex > track.size()) { trackIndex = 1; return 1; }
        trackIndex = nextIndex;
        return trackIndex;
    }

    private boolean isPlayerClose() {
        for (Entity entity : happyGhast.getNearbyEntities(2.5, 4.0, 2.5)) {
            if (!(entity instanceof Player)) continue;
            if (entity.getLocation().getY() >= happyGhast.getLocation().getY() + 2) return true;
        }
        return false;
    }

    private boolean isOtherShopOnTrack() {
        for (ShopEntity shopEntity : shopEntities) {
            if (trackIndex - shopEntity.trackIndex + 100 <= 0) return true;

            boolean isLowerThanZero = shopEntity.trackIndex - 100 < 0;
            if (!isLowerThanZero) continue;
            if ((track.size() - trackIndex) + shopEntity.trackIndex < 100) continue;
            return true;
        }
        return false;
    }

    public Location getPosition() {
        return track.getOrDefault(trackIndex, new Location(Constants.world, 0, 300, 0));
    }
}
