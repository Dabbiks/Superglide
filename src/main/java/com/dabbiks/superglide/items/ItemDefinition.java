package com.dabbiks.superglide.items;

import java.util.List;
import java.util.Map;

public class ItemDefinition {

    private String identifier;
    private String name;
    private List<String> lore;
    private String material;
    private int customModelData;
    private int price;
    private boolean clearTags;
    private Map<String, Double> attributeMap;
    private Map<String, Object> potionData;
    private List<String> perkList;

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

    public boolean isClearTags() { return clearTags; }
    public void setClearTags(boolean clearTags) { this.clearTags = clearTags; }

    public Map<String, Double> getAttributeMap() { return attributeMap; }
    public void setAttributeMap(Map<String, Double> attributeMap) { this.attributeMap = attributeMap; }

    public Map<String, Object> getPotionData() { return potionData; }
    public void setPotionData(Map<String, Object> potionData) { this.potionData = potionData; }

    public List<String> getPerkList() { return perkList; }
    public void setPerkList(List<String> perkList) { this.perkList = perkList; }
}
