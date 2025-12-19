package com.dabbiks.superglide.cosmetics;

public enum Cage {
    DEFAULT_CAGE("Domyślna", CosmeticTier.POPULAR),
    GOLDEN_CAGE("Złota", CosmeticTier.EPIC),
    DIAMOND_CAGE("Diamentowa", CosmeticTier.LEGENDARY);

    private String name;
    private CosmeticTier tier;

    Cage(String name, CosmeticTier tier) {
        this.name = name;
        this.tier = tier;
    }
}
