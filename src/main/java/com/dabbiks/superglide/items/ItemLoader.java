package com.dabbiks.superglide.items;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import static com.dabbiks.superglide.Superglide.plugin;

public class ItemLoader {

    public static void loadItems() {
        File folder = new File(plugin.getDataFolder(), "items");
        if (!folder.exists()) folder.mkdirs();

        Gson gson = new Gson();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                ItemDefinition loaded = gson.fromJson(reader, ItemDefinition.class);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while loading item from file: " + file.getName(), e);
            }
        }
    }

}
