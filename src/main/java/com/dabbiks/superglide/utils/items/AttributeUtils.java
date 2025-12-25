package com.dabbiks.superglide.utils.items;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class AttributeUtils {

    public ItemStack addAttributeToItem(ItemStack item, Attribute attribute, double value, AttributeModifier.Operation operation, EquipmentSlot slot) {
        if (item == null || !item.hasItemMeta()) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        AttributeModifier modifier = new AttributeModifier(
                UUID.randomUUID(),
                attribute.name().toLowerCase() + "_bonus",
                value,
                operation,
                slot
        );

        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);
        return item;
    }

}
