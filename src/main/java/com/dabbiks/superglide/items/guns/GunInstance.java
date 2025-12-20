package com.dabbiks.superglide.items.guns;

public class GunInstance {

    private int ammoAmount;
    private GunFireMode currentFireMode;
    private boolean isReloading;
    private double currentShotCooldown;
    private int reloadCustomModelData;
    private int skinId;

    public GunInstance(int ammoAmount, GunFireMode fireMode, int skinId, int customModelData) {
        this.ammoAmount = ammoAmount;
        this.currentFireMode = fireMode;
        this.skinId = skinId;
        isReloading = false;
        currentShotCooldown = 0;
        reloadCustomModelData = customModelData + 1;
    }

    public int getAmmoAmount() { return ammoAmount; }
    public void setAmmoAmount(int ammoAmount) { this.ammoAmount = ammoAmount; }
    public void incrementAmmoAmount() { this.ammoAmount++; }
    public void decrementAmmoAmount() { this.ammoAmount--; }

    public GunFireMode getCurrentFireMode() { return currentFireMode; }
    public void setCurrentFireMode(GunFireMode fireMode) { currentFireMode = fireMode; }

    public boolean getIsReloading() { return isReloading; }
    public void setIsReloading(boolean bool) { isReloading = bool; }

    public double getCurrentShotCooldown() { return currentShotCooldown; }
    public void setCurrentShotCooldown(double cooldown) { currentShotCooldown = cooldown; }

    public int getReloadCustomModelData() { return reloadCustomModelData; }

    public int getSkinId() { return skinId; }
    public void setSkinId(int id) { skinId = id; }
}
