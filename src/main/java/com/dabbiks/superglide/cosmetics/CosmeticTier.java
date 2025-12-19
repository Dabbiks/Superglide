package com.dabbiks.superglide.cosmetics;

public enum CosmeticTier {
    POPULAR("", 3000),
    RARE("R ", 6500),
    EPIC("E ", 10000),
    MYTHICAL("M ", 14000),
    LEGENDARY("L ", 20000);

    private String icon;
    private int price;

    CosmeticTier(String icon, int price) {
        this.price = price;
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public int getPrice() {
        return price;
    }

}
