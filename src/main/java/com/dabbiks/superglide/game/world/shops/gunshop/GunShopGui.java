package com.dabbiks.superglide.game.world.shops.gunshop;

import com.dabbiks.superglide.game.world.shops.itemshop.GuiItem;
import com.dabbiks.superglide.items.ItemDefinition;
import com.dabbiks.superglide.items.guns.GunDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.Map;

import static com.dabbiks.superglide.Superglide.itemManager;

public class GunShopGui {

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

        for (Map.Entry<String, GunDefinition> entry : itemManager.gunDefinitions.entrySet()) {
            GunDefinition definition = entry.getValue();

            ItemStack itemStack = new com.dabbiks.superglide.items.guns.GunBuilder(definition).build();

            gui.addItems(new GuiItem(itemStack, player));
        }

        Window.single()
                .setGui(gui)
                .setTitle("Sklep z bronią")
                .build(player)
                .open();
    }

}
