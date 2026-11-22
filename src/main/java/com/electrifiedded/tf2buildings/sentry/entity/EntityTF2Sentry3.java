package com.electrifiedded.tf2buildings.sentry.entity;

import com.electrifiedded.tf2buildings.sentry.SentryConfig;
import com.electrifiedded.tf2buildings.sentry.SentrySounds;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityTF2Sentry3 extends EntityTF2Sentry {
    public int rocketTime = 40;

    public EntityTF2Sentry3(World world) {
        super(world);
        range = SentryConfig.range3;
        rangeUpDown = SentryConfig.rangeUpDown3;
        setHealth(getMaxHealth());
        attackDelay = 5;
    }

    @Override
    protected double getBaseHealth() { return 20.0; }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (rocketTime > 0) rocketTime--;
    }

    @Override
    protected boolean canUpgrade() { return false; }

    @Override
    protected void wrenchHit(int power) {
        if (get(ROCKETS) < getMaxRockets()) {
            set(ROCKETS, Math.min(getMaxRockets(), get(ROCKETS) + power));
            showHearts();
        }
        super.wrenchHit(power);
    }

    @Override
    protected EntityTF2Sentry createUpgrade() { return null; }

    @Override
    protected void attackEntity(Entity entity) {
        if (canEntityBeSeen(entity) && rocketTime == 0) {
            double dx = entity.posX - posX;
            double dy = entity.posY - posY + entity.getEyeHeight() - 0.6F;
            double dz = entity.posZ - posZ;
            if (!SentryConfig.infiniteAmmo && get(ROCKETS) <= 0) {
                SentrySounds.play(this, "sentry_empty", SentryConfig.shootVolume);
            } else {
                EntityTF2Rocket rocket = new EntityTF2Rocket(world, this);
                rocket.shoot(dx, dy, dz, 3.0F, 0.0F);
                world.spawnEntity(rocket);
                if (!SentryConfig.infiniteAmmo) set(ROCKETS, get(ROCKETS) - 1);
                SentrySounds.play(this, "sentry_rocket", SentryConfig.shootVolume);
            }
            rocketTime = 40;
        }
        super.attackEntity(entity);
    }

    @Override
    public int getState() { return 3; }

    @Override
    protected String getShootSound() { return "sentry_3shoot"; }

    @Override
    public int getMaxAmmo() { return 200; }
}
