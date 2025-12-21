package com.dabbiks.superglide.player.data.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionDataManager {

    private static final Map<UUID, SessionData> sessionDataMap = new HashMap<>();

    public static SessionData getData(UUID uuid) {
        if (!sessionDataMap.containsKey(uuid)) sessionDataMap.put(uuid, new SessionData());
        return sessionDataMap.get(uuid);
    }

}
