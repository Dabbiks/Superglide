package com.dabbiks.superglide.items;

import com.dabbiks.superglide.items.guns.GunDefinition;
import com.dabbiks.superglide.items.guns.GunInstance;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {

    public Map<String, ItemDefinition> itemDefinitions = new HashMap<>();
    public Map<String, GunDefinition> gunDefinitions = new HashMap<>();
    public Map<Integer, GunInstance> gunInstances = new HashMap<>();

    public ItemDefinition getItem(String identifier) {
        return itemDefinitions.getOrDefault(identifier, null);
    }

}
