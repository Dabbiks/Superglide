package com.dabbiks.superglide.items;

import java.util.List;

public class ItemDefinition {

    private String identifier;
    private String name;
    private List<String> lore;
    private String material;
    private int customModelData;
    private int price;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public int getCustomModelData() { return customModelData; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

}
