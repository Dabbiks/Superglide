package com.dabbiks.superglide.items;

import com.dabbiks.superglide.items.guns.GunDefinition;
import com.dabbiks.superglide.items.guns.GunInstance;
import com.dabbiks.superglide.items.guns.mechanics.WeaponTrait;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {

    private int gunInstanceIndex = 0;

    public Map<String, ItemDefinition> itemDefinitions = new HashMap<>();
    public Map<String, GunDefinition> gunDefinitions = new HashMap<>();
    public Map<Integer, GunInstance> gunInstances = new HashMap<>();

    public ItemDefinition getItem(String identifier) {
        return itemDefinitions.getOrDefault(identifier, null);
    }

    public int getNextGunInstanceIndex() {
        gunInstanceIndex++;
        return gunInstanceIndex;
    }

    // * ----------------------------------------------------------------------

    public Map<String, WeaponTrait> weaponTraits = new HashMap<>();

    public void registerTrait(String gunIdentifier, WeaponTrait trait) {
        weaponTraits.put(gunIdentifier, trait);
    }

    public WeaponTrait getTrait(String gunIdentifier) {
        return weaponTraits.get(gunIdentifier);
    }

}
