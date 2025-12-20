package com.dabbiks.superglide.items;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {

    public Map<String, ItemDefinition> itemDefinitions = new HashMap<>();

    public ItemDefinition getItem(String identifier) {
        return itemDefinitions.getOrDefault(identifier, null);
    }

}
