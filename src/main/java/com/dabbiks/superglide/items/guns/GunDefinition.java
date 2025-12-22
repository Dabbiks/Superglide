package com.dabbiks.superglide.items.guns;

import com.dabbiks.superglide.items.ItemDefinition;

public class GunDefinition extends ItemDefinition {

    private double fireRate;
    private double bulletDamage;
    private double bulletSpeed;
    private int ammoCapacity;
    private int reloadTicks;
    private int scope;
    private double spread;
    private double recoilY;
    private double recoilX;
    private GunAmmunitionType ammoType;
    private GunFireMode fireMode;
    private GunInstance gunInstanceDefinition;

    public double getFireRate() { return fireRate; }
    public void setFireRate(double fireRate) { this.fireRate = fireRate; }

    public double getBulletDamage() { return bulletDamage; }
    public void setBulletDamage(double bulletDamage) { this.bulletDamage = bulletDamage; }

    public double getBulletSpeed() { return bulletSpeed; }
    public void setBulletSpeed(double bulletSpeed) { this.bulletSpeed = bulletSpeed; }

    public int getAmmoCapacity() { return ammoCapacity; }
    public void setAmmoCapacity(int ammoCapacity) { this.ammoCapacity = ammoCapacity; }

    public int getReloadTicks() { return reloadTicks; }
    public void setReloadTicks(int reloadTicks) { this.reloadTicks = reloadTicks; }

    public int getScope() { return scope; }
    public void setScope(int scope) { this.scope = scope; }

    public double getSpread() { return spread; }
    public void setSpread(double spread) { this.spread = spread; }

    public double getRecoilY() { return recoilY; }
    public void setRecoilY(double recoilY) { this.recoilY = recoilY; }

    public double getRecoilX() { return recoilX; }
    public void setRecoilX(double recoilX) { this.recoilX = recoilX; }

    public GunAmmunitionType getAmmoType() { return ammoType; }
    public void setAmmoType(GunAmmunitionType ammoType) { this.ammoType = ammoType; }

    public GunFireMode getFireMode() { return fireMode; }
    public void setFireMode(GunFireMode fireMode) { this.fireMode = fireMode; }

    public GunInstance getGunInstanceDefinition() { return gunInstanceDefinition; }
    public void setGunInstanceDefinition(GunInstance gunInstanceDefinition) { this.gunInstanceDefinition = gunInstanceDefinition; }
}
