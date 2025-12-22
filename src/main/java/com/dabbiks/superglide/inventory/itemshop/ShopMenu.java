package com.dabbiks.superglide.inventory.itemshop;

import com.dabbiks.superglide.items.ItemDefinition;
import com.dabbiks.superglide.items.ItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Map;

import static com.dabbiks.superglide.Superglide.itemManager;

public class ShopMenu {

    public static void open(Player player) {

        Gui.Builder.Normal guiBuilder = Gui.normal()
                .setStructure(
                        "#########",
                        "#.......#",
                        "#.......#",
                        "#.......#",
                        "#.......#",
                        "#########"
                )
                .addIngredient('#', new ItemBuilder(Material.AIR).setDisplayName(""));

        Gui gui = guiBuilder.build();

        for (Map.Entry<String, ItemDefinition> entry : itemManager.itemDefinitions.entrySet()) {
            ItemDefinition definition = entry.getValue();

            ItemStack itemStack = new com.dabbiks.superglide.items.ItemBuilder(definition).build();

            gui.addItems(new ShopItem(itemStack, player));
        }

        Window.single()
                .setGui(gui)
                .setTitle("Sklep z przedmiotami")
                .build(player)
                .open();
    }

}
