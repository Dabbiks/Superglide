package com.dabbiks.superglide.player.data.persistent;

import com.dabbiks.superglide.Superglide;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistentDataManager {

    private static Map<UUID, PersistentData> dataMap = new HashMap<>();

    @Nullable
    public static PersistentData getData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public static void loadData(UUID uuid) {
        PersistentData persistentData = Superglide.persistentDataJson.loadPlayerData(uuid);
        if (persistentData == null) {
            persistentData = new PersistentData();
            persistentData.setPlayerId(uuid);
        }
        dataMap.put(uuid, persistentData);
    }

    public static void delData(UUID uuid) {
        dataMap.remove(uuid);
    }

    public static void saveData(UUID uuid) {
        PersistentData persistentData = getData(uuid);
        Superglide.persistentDataJson.savePlayerData(persistentData);
    }
}
