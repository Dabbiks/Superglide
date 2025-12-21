package com.dabbiks.superglide.player.scoreboard;

import com.dabbiks.superglide.player.data.persistent.PersistentData;
import com.dabbiks.superglide.player.data.persistent.PersistentDataManager;
import com.dabbiks.superglide.player.data.session.SessionData;
import com.dabbiks.superglide.player.data.session.SessionDataManager;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BoardManager {

    private static final Map<Player, FastBoard> boards = new HashMap<>();

    public static void addBoard(Player player) {
        boards.put(player, new FastBoard(player));
    }

    public static FastBoard getBoard(Player player) {
        return boards.getOrDefault(player, new FastBoard(player));
    }

    public static void setLobbyBoard(Player player) {
        FastBoard board = getBoard(player);

        PersistentData data = PersistentDataManager.getData(player.getUniqueId());
        SessionData sessionData = SessionDataManager.getData(player.getUniqueId());

        board.updateTitle("TITLE");

        board.updateLines(
                "A",
                "",
                ""
        );
    }

    public static void setInGameBoard(Player player) {
        FastBoard board = getBoard(player);

        PersistentData data = PersistentDataManager.getData(player.getUniqueId());
        SessionData sessionData = SessionDataManager.getData(player.getUniqueId());

        board.updateTitle("TITLE");

        board.updateLines(
                "B",
                "",
                ""
        );
    }

    public static void setSpectatorBoard(Player player) {
        FastBoard board = getBoard(player);

        PersistentData data = PersistentDataManager.getData(player.getUniqueId());
        SessionData sessionData = SessionDataManager.getData(player.getUniqueId());

        board.updateTitle("TITLE");

        board.updateLines(
                "C",
                "",
                ""
        );
    }

}
