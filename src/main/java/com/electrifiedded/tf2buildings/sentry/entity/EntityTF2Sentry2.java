package com.electrifiedded.tf2buildings.sentry.entity;

import com.electrifiedded.tf2buildings.sentry.SentryConfig;
import net.minecraft.world.World;

public class EntityTF2Sentry2 extends EntityTF2Sentry {
    public EntityTF2Sentry2(World world) {
        super(world);
        setHealth(getMaxHealth());
        attackDelay = 5;
        upgradesNeeded = 20;
        range = SentryConfig.range2;
        rangeUpDown = SentryConfig.rangeUpDown2;
    }

    @Override
    protected double getBaseHealth() { return 15.0; }

    @Override
    protected EntityTF2Sentry createUpgrade() { return new EntityTF2Sentry3(world); }

    @Override
    public int getState() { return 2; }

    @Override
    protected String getShootSound() { return "sentry_2shoot"; }

    @Override
    public int getMaxAmmo() { return 100; }
}
