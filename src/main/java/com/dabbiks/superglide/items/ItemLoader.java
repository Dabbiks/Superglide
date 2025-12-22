package com.dabbiks.superglide.items;

import com.dabbiks.superglide.items.guns.GunDefinition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import static com.dabbiks.superglide.Superglide.itemManager;
import static com.dabbiks.superglide.Superglide.plugin;

public class ItemLoader {

    public static void loadItems() {
        File folder = new File(plugin.getDataFolder(), "items");
        if (!folder.exists()) folder.mkdirs();

        Gson gson = new Gson();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        ItemManager manager = itemManager;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                if (jsonObject.has("fireRate")) {
                    GunDefinition gun = gson.fromJson(jsonObject, GunDefinition.class);
                    manager.gunDefinitions.put(gun.getIdentifier(), gun);
                } else {
                    ItemDefinition item = gson.fromJson(jsonObject, ItemDefinition.class);
                    manager.itemDefinitions.put(item.getIdentifier(), item);
                }

            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while loading item from file: " + file.getName(), e);
            }
        }
    }

}
