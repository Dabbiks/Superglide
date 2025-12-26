package com.dabbiks.superglide.items.guns;

import com.dabbiks.superglide.items.ItemDefinition;
import com.dabbiks.superglide.items.guns.enums.GunAmmunitionType;
import com.dabbiks.superglide.items.guns.enums.GunFireMode;
import com.dabbiks.superglide.items.guns.enums.ReloadMode;
import com.dabbiks.superglide.items.guns.enums.ScopeType;

public class GunDefinition extends ItemDefinition {

    private double fireRate;
    private double bulletSpeed;
    private int ammoCapacity;

    private int reloadTicks;
    private ReloadMode reloadMode = ReloadMode.MAGAZINE;
    private String ammoMaterial;

    private ScopeType scopeType = ScopeType.NONE;
    private int burstAmount = 0;
    private int burstDelay = 0;

    private double spread;
    private double recoilY;
    private double recoilX;
    private double crouchRecoilMultiplier = 0.75;

    private double bulletDamage;
    private double headshotMultiplier = 1.5;
    private double damageDropoffStart;
    private double damageDropoffEnd;
    private double minDamage;

    private String projectileMaterial;

    public double getFireRate() { return fireRate; }
    public double getBulletSpeed() { return bulletSpeed; }
    public int getAmmoCapacity() { return ammoCapacity; }
    public int getReloadTicks() { return reloadTicks; }
    public ReloadMode getReloadMode() { return reloadMode; }
    public String getAmmoMaterial() { return ammoMaterial; }
    public ScopeType getScopeType() { return scopeType; }
    public int getBurstAmount() { return burstAmount; }
    public int getBurstDelay() { return burstDelay; }
    public double getSpread() { return spread; }
    public double getRecoilY() { return recoilY; }
    public double getRecoilX() { return recoilX; }
    public double getCrouchRecoilMultiplier() { return crouchRecoilMultiplier; }
    public double getBulletDamage() { return bulletDamage; }
    public double getHeadshotMultiplier() { return headshotMultiplier; }
    public double getDamageDropoffStart() { return damageDropoffStart; }
    public double getDamageDropoffEnd() { return damageDropoffEnd; }
    public double getMinDamage() { return minDamage; }
    public String getProjectileMaterial() { return projectileMaterial; }

    public void setFireRate(double fireRate) { this.fireRate = fireRate; }
    public void setBulletSpeed(double bulletSpeed) { this.bulletSpeed = bulletSpeed; }
    public void setAmmoCapacity(int ammoCapacity) { this.ammoCapacity = ammoCapacity; }
    public void setReloadTicks(int reloadTicks) { this.reloadTicks = reloadTicks; }
    public void setReloadMode(ReloadMode reloadMode) { this.reloadMode = reloadMode; }
    public void setAmmoMaterial(String ammoMaterial) { this.ammoMaterial = ammoMaterial; }
    public void setScopeType(ScopeType scopeType) { this.scopeType = scopeType; }
    public void setBurstAmount(int burstAmount) { this.burstAmount = burstAmount; }
    public void setBurstDelay(int burstDelay) { this.burstDelay = burstDelay; }
    public void setSpread(double spread) { this.spread = spread; }
    public void setRecoilY(double recoilY) { this.recoilY = recoilY; }
    public void setRecoilX(double recoilX) { this.recoilX = recoilX; }
    public void setCrouchRecoilMultiplier(double crouchRecoilMultiplier) { this.crouchRecoilMultiplier = crouchRecoilMultiplier; }
    public void setBulletDamage(double bulletDamage) { this.bulletDamage = bulletDamage; }
    public void setHeadshotMultiplier(double headshotMultiplier) { this.headshotMultiplier = headshotMultiplier; }
    public void setDamageDropoffStart(double damageDropoffStart) { this.damageDropoffStart = damageDropoffStart; }
    public void setDamageDropoffEnd(double damageDropoffEnd) { this.damageDropoffEnd = damageDropoffEnd; }
    public void setMinDamage(double minDamage) { this.minDamage = minDamage; }
    public void setProjectileMaterial(String projectileMaterial) { this.projectileMaterial = projectileMaterial; }
}
