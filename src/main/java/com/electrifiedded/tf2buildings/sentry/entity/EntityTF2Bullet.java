package com.electrifiedded.tf2buildings.sentry.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityTF2Bullet extends EntityTF2Projectile {
    public int strength = 4;

    public EntityTF2Bullet(World world) {
        super(world);
    }

    public EntityTF2Bullet(World world, EntityLivingBase shooter) {
        super(world, shooter);
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        if (world.isRemote || hit.entityHit == null) return;
        DamageSource src = DamageSource.causeThrownDamage(this, owner);
        if (hit.entityHit instanceof EntityEnderman) {
            // endermen would teleport away from projectiles; the original simply killed them
            hit.entityHit.attackEntityFrom(DamageSource.causeMobDamage(owner == null ? (EntityEnderman) hit.entityHit : owner), strength);
            return;
        }
        if (hit.entityHit.attackEntityFrom(src, strength) && hit.entityHit instanceof EntityLiving) {
            EntityLiving l = (EntityLiving) hit.entityHit;
            l.setArrowCountInEntity(l.getArrowCountInEntity() + 1);
        }
    }
}
