package com.dabbiks.superglide.player.data.persistent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import static com.dabbiks.superglide.Superglide.plugin;

public class PersistentDataJson {

    private Gson gson;
    private File dataFolder;

    public PersistentDataJson() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        dataFolder = new File(plugin.getDataFolder() + "/player-data");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public PersistentData loadPlayerData(UUID playerId) {
        File file = new File(dataFolder, playerId.toString() + ".json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            PersistentData data = gson.fromJson(reader, PersistentData.class);
            data.setPlayerId(playerId);

            data.setPlayerName(Bukkit.getPlayer(playerId).getName());

            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void savePlayerData(PersistentData data) {
        File file = new File(dataFolder, data.getPlayerId().toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
