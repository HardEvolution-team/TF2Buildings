package com.electrifiedded.tf2buildings.sentry.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Straight-flying, gravity-less sentry projectile (common code of the 1.7 bullet and rocket). */
public abstract class EntityTF2Projectile extends Entity {
    public EntityLivingBase owner;
    protected int ticksInAir;

    public EntityTF2Projectile(World world) {
        super(world);
        setSize(0.25F, 0.25F);
    }

    public EntityTF2Projectile(World world, EntityLivingBase shooter) {
        this(world);
        owner = shooter;
        setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        posX -= MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        posY -= 0.1F;
        posZ -= MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
    }

    @Override
    protected void entityInit() {}

    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        float len = MathHelper.sqrt(x * x + y * y + z * z);
        x /= len; y /= len; z /= len;
        x += rand.nextGaussian() * 0.0075F * inaccuracy;
        y += rand.nextGaussian() * 0.0075F * inaccuracy;
        z += rand.nextGaussian() * 0.0075F * inaccuracy;
        motionX = x * speed;
        motionY = y * speed;
        motionZ = z * speed;
        float h = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0 / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, h) * 180.0 / Math.PI);
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;
        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float h = MathHelper.sqrt(x * x + z * z);
            prevRotationYaw = rotationYaw = (float) (Math.atan2(x, z) * 180.0 / Math.PI);
            prevRotationPitch = rotationPitch = (float) (Math.atan2(y, h) * 180.0 / Math.PI);
        }
    }

    protected abstract void onImpact(RayTraceResult hit);

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (++ticksInAir > 200) {
            setDead();
            return;
        }
        Vec3d from = new Vec3d(posX, posY, posZ);
        Vec3d to = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
        RayTraceResult hit = world.rayTraceBlocks(from, to, false, true, false);
        if (hit != null) to = new Vec3d(hit.hitVec.x, hit.hitVec.y, hit.hitVec.z);

        Entity found = null;
        double best = 0.0;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0));
        for (Entity e : list) {
            if (!e.canBeCollidedWith() || e instanceof EntityTF2Sentry || (e == owner && ticksInAir < 5)) continue;
            AxisAlignedBB bb = e.getEntityBoundingBox().grow(0.3);
            RayTraceResult r = bb.calculateIntercept(from, to);
            if (r != null) {
                double d = from.distanceTo(r.hitVec);
                if (d < best || best == 0.0) {
                    found = e;
                    best = d;
                }
            }
        }
        if (found != null) hit = new RayTraceResult(found);
        if (hit != null) {
            onImpact(hit);
            setDead();
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float h = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0 / Math.PI);
        rotationPitch = (float) (Math.atan2(motionY, h) * 180.0 / Math.PI);
        while (rotationPitch - prevRotationPitch < -180.0F) prevRotationPitch -= 360.0F;
        while (rotationPitch - prevRotationPitch >= 180.0F) prevRotationPitch += 360.0F;
        while (rotationYaw - prevRotationYaw < -180.0F) prevRotationYaw -= 360.0F;
        while (rotationYaw - prevRotationYaw >= 180.0F) prevRotationYaw += 360.0F;
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        if (isInWater() && world.isRemote) {
            for (int i = 0; i < 4; i++)
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25, posY - motionY * 0.25, posZ - motionZ * 0.25, motionX, motionY, motionZ);
        }
        setPosition(posX, posY, posZ);
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {}

    @Override
    public boolean writeToNBTOptional(NBTTagCompound tag) { return false; }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {}
}
