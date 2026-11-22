package com.electrifiedded.tf2buildings.sentry.entity;

import com.electrifiedded.tf2buildings.sentry.SentryConfig;
import com.electrifiedded.tf2buildings.sentry.SentrySounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

/** Level 3 rocket: blast that hurts everything except sentries; like the original it breaks no blocks. */
public class EntityTF2Rocket extends EntityTF2Projectile {
    public EntityTF2Rocket(World world) {
        super(world);
    }

    public EntityTF2Rocket(World world, EntityLivingBase shooter) {
        super(world, shooter);
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0, 0, 0);
        super.onUpdate();
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        if (world.isRemote) return;
        if (hit.entityHit != null) hit.entityHit.attackEntityFrom(new EntityDamageSourceIndirect("arrow", this, owner), 10.0F);
        blast(posX, posY, posZ, 2.0F);
    }

    private void blast(double x, double y, double z, float size) {
        AxisAlignedBB box = new AxisAlignedBB(MathHelper.floor(x - size - 1), MathHelper.floor(y - size - 1), MathHelper.floor(z - size - 1),
                MathHelper.floor(x + size + 1), MathHelper.floor(y + size + 1), MathHelper.floor(z + size + 1));
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, box);
        Vec3d center = new Vec3d(x, y, z);
        for (Entity e : list) {
            double dist = e.getDistance(x, y, z) / size;
            if (dist > 1.0 || e instanceof EntityTF2Sentry) continue;
            double dx = e.posX - x, dy = e.posY - y, dz = e.posZ - z;
            double len = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
            if (len == 0) continue;
            dx /= len; dy /= len; dz /= len;
            double impact = (1.0 - dist) * world.getBlockDensity(center, e.getEntityBoundingBox());
            e.attackEntityFrom(DamageSource.causeThrownDamage(this, owner), (int) ((impact * impact + impact) / 2.0 * 16.0 * size + 1.0));
            e.motionX += dx * impact;
            e.motionY += dy * impact;
            e.motionZ += dz * impact;
        }
        SentrySounds.play(this, "sentry_explode", SentryConfig.explosionVolume);
        if (world instanceof WorldServer)
            ((WorldServer) world).spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
    }
}
