package com.dabbiks.superglide.items.guns;

import com.dabbiks.superglide.ConsoleLogger;
import com.dabbiks.superglide.items.ItemBuilder;
import com.dabbiks.superglide.items.ItemManager;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import static com.dabbiks.superglide.Superglide.itemManager;

public class GunBuilder extends ItemBuilder {

    private final GunDefinition gunDefinition;

    public GunBuilder(GunDefinition definition) {
        super(definition);
        this.gunDefinition = definition;
    }

    @Override
    public ItemStack build() {
        ItemStack item = super.build();
        NBTItem nbt = new NBTItem(item);

        nbt.setDouble("fireRate", gunDefinition.getFireRate());
        nbt.setDouble("bulletDamage", gunDefinition.getBulletDamage());
        nbt.setDouble("bulletSpeed", gunDefinition.getBulletSpeed());
        nbt.setInteger("ammoCapacity", gunDefinition.getAmmoCapacity());
        nbt.setInteger("reloadTicks", gunDefinition.getReloadTicks());
        nbt.setInteger("scope", gunDefinition.getScope());
        nbt.setDouble("spread", gunDefinition.getSpread());
        nbt.setDouble("recoilY", gunDefinition.getRecoilY());
        nbt.setDouble("recoilX", gunDefinition.getRecoilX());

        if (gunDefinition.getAmmoType() != null) {
            nbt.setString("ammoType", gunDefinition.getAmmoType().name());
        }

        if (gunDefinition.getFireMode() != null) {
            nbt.setString("defaultFireMode", gunDefinition.getFireMode().name());
        }

        GunInstance instance = gunDefinition.getGunInstanceDefinition();
        if (instance == null) {
            ConsoleLogger.warning(ConsoleLogger.Type.PLUGIN, "GunInstance failed to load");
            return nbt.getItem();
        }
        itemManager.gunInstances.put(itemManager.getNextGunInstanceIndex(), instance);
        nbt.setInteger("gunInstance", itemManager.getNextGunInstanceIndex());

        return nbt.getItem();
    }
}