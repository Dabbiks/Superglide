package com.dabbiks.superglide.inventory.itemshop;

import com.dabbiks.superglide.player.data.session.SessionData;
import com.dabbiks.superglide.player.data.session.SessionDataManager;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import static com.dabbiks.superglide.Superglide.soundU;

public class ShopItem extends AbstractItem {
    private final Player player;
    private final ItemStack item;

    public ShopItem(ItemStack item, Player player) {
        this.player = player;
        this.item = item;
    }

    @Override
    public ItemProvider getItemProvider() {
        SessionData sessionData = SessionDataManager.getData(player.getUniqueId());
        NBTItem nbt = new NBTItem(item);

        int playerGold = 100; // ! TODO SESSIONDATA I PLATNOSC
        int priceGold = nbt.getInteger("price");

        ItemBuilder builder = new ItemBuilder(item.clone());

        if (playerGold >= priceGold) {
            builder.addLoreLines("§fIKONA §aKup za " + priceGold + " złota");
        } else {
            builder.addLoreLines("§fIKONABRAK §cBrakuje Ci " + (priceGold-playerGold) + " złota");
        }

        return builder;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        SessionData sessionData = SessionDataManager.getData(player.getUniqueId());
        NBTItem nbt = new NBTItem(item);

        int playerGold = 100; // ! TODO SESSIONDATA I PLATNOSC
        int priceGold = nbt.getInteger("price");

        if (playerGold >= priceGold) {
            soundU.playSoundAtPlayer(player, Sound.ENTITY_VILLAGER_TRADE, 0.8F, 1);
            player.getInventory().addItem(item);
        } else {
            soundU.playSoundAtPlayer(player, Sound.ENTITY_VILLAGER_NO, 0.8F, 1);
        }
        notifyWindows();
    }
}
