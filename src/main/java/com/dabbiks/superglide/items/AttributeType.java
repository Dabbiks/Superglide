package com.dabbiks.superglide.items;

import org.bukkit.attribute.Attribute;

import static com.dabbiks.superglide.Superglide.symbols;

public enum AttributeType {
    ATTACK_DAMAGE(symbols.attackDamage, "Obrażenia", Attribute.ATTACK_DAMAGE),
    ATTACK_SPEED(symbols.attackSpeed, "Prędkość ataku", Attribute.ATTACK_SPEED),
    ATTACK_RANGE(symbols.attackRange, "Zasięg ataku", Attribute.ENTITY_INTERACTION_RANGE);

    private final String icon;
    private final String name;
    private final Attribute attribute;

    AttributeType(String icon, String name, Attribute attribute) {
        this.icon = icon;
        this.name = name;
        this.attribute = attribute;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public Attribute getAttribute() {
        return attribute;
    }
}
