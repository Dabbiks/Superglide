package com.dabbiks.superglide.items;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class ItemBuilder {

    private final ItemDefinition definition;

    public ItemBuilder(ItemDefinition definition) {
        this.definition = definition;
    }

    public ItemStack build() {
        Material material = Material.matchMaterial(definition.getMaterial());
        if (material == null) material = Material.BARRIER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(definition.getName());
            meta.setLore(definition.getLore());
            meta.setCustomModelData(definition.getCustomModelData());

            if (meta instanceof PotionMeta potionMeta && definition.getPotionData() != null) {
                handlePotionLogic(potionMeta, definition.getPotionData());
            }

            item.setItemMeta(meta);
        }

        NBTItem nbt = new NBTItem(item);
        nbt.setInteger("price", definition.getPrice());
        nbt.setBoolean("clearTags", definition.isClearTags());

        if (definition.getAttributeMap() != null) {
            definition.getAttributeMap().forEach(nbt::setDouble);
        }

        if (definition.getPerkList() != null) {
            definition.getPerkList().forEach(perk -> nbt.setBoolean(perk, true));
        }

        return nbt.getItem();
    }

    private void handlePotionLogic(PotionMeta meta, Map<String, Object> data) {
        if (data.containsKey("type")) {
            PotionEffectType type = PotionEffectType.getByName((String) data.get("type"));
            if (type != null) {
                int duration = ((Number) data.getOrDefault("duration", 0)).intValue();
                int amplifier = ((Number) data.getOrDefault("amplifier", 0)).intValue();

                meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
            }
        }

        if (data.containsKey("color")) {
            String colorStr = (String) data.get("color");
            try {
                Color color = colorStr.startsWith("#")
                        ? Color.fromRGB(Integer.parseInt(colorStr.substring(1), 16))
                        : getColorByName(colorStr);
                meta.setColor(color);
            } catch (Exception ignored) {}
        }
    }

    private Color getColorByName(String name) {
        return switch (name.toUpperCase()) {
            case "RED" -> Color.RED;
            case "BLUE" -> Color.BLUE;
            case "GREEN" -> Color.GREEN;
            case "YELLOW" -> Color.YELLOW;
            case "AQUA" -> Color.AQUA;
            default -> Color.WHITE;
        };
    }
}