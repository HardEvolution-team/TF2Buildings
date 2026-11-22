package com.electrifiedded.tf2buildings.sentry.entity;

import com.electrifiedded.tf2buildings.sentry.SentryConfig;
import com.electrifiedded.tf2buildings.sentry.SentrySounds;
import com.electrifiedded.tf2buildings.sentry.SentryTargets;
import com.electrifiedded.tf2buildings.sentry.TF2SentryMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

/** Level 1 sentry gun. Levels 2/3 extend it. */
public class EntityTF2Sentry extends EntityLiving implements IAnimals {
    private static final DataParameter<Boolean> BLUE = EntityDataManager.createKey(EntityTF2Sentry.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> KILLS = EntityDataManager.createKey(EntityTF2Sentry.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> UPGRADES = EntityDataManager.createKey(EntityTF2Sentry.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> AMMO = EntityDataManager.createKey(EntityTF2Sentry.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> ROCKETS = EntityDataManager.createKey(EntityTF2Sentry.class, DataSerializers.VARINT);
    private static final DataParameter<Float> BASE_YAW = EntityDataManager.createKey(EntityTF2Sentry.class, DataSerializers.FLOAT);

    public int range;
    public int rangeUpDown;
    public Entity target;
    public int attackDelay = 10;
    public int upgradesNeeded = 10;
    protected int attackTimer;
    protected Entity entityToAttack;
    private boolean dropAsItem;

    public EntityTF2Sentry(World world) {
        super(world);
        setSize(0.9F, 1.3F);
        setHealth(getMaxHealth());
        range = SentryConfig.range1;
        rangeUpDown = SentryConfig.rangeUpDown1;
    }

    public EntityTF2Sentry(World world, double x, double y, double z, int rotation) {
        this(world);
        setBaseYaw(90 * rotation);
        rotationYaw = rotationYawHead = 90 * rotation;
        setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(BLUE, false);
        dataManager.register(KILLS, 0);
        dataManager.register(UPGRADES, 0);
        dataManager.register(AMMO, getMaxAmmo());
        dataManager.register(ROCKETS, getMaxRockets());
        dataManager.register(BASE_YAW, 0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getBaseHealth());
    }

    protected double getBaseHealth() { return 10.0; }

    public int get(DataParameter<Integer> p) { return dataManager.get(p); }

    public void set(DataParameter<Integer> p, int v) { dataManager.set(p, v); }

    public void setBaseYaw(float yaw) { dataManager.set(BASE_YAW, yaw); }

    @Override
    public void onUpdate() {
        if (world.isRemote && getHealth() < getMaxHealth() / 2.0F) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX + rand.nextFloat() / 2.0F - 0.25F,
                    posY + rand.nextFloat() * 0.5F + 0.5F, posZ + rand.nextFloat() / 2.0F - 0.25F, 0, 0, 0);
        }
        // the base never turns, only the head follows targets (1.7 skipped the body rotation code the same way)
        renderYawOffset = prevRenderYawOffset = dataManager.get(BASE_YAW);
        onEntityUpdate();
        onLivingUpdate();
        if (attackTimer > 0) attackTimer--;
    }

    protected Entity findPlayerToAttack() {
        boolean changed = false;
        if (target != null && (target.isDead || !canEntityBeSeen(target) || target instanceof EntityEnderman || target instanceof EntityPlayer))
            target = null;
        List<EntityLiving> entities = world.getEntitiesWithinAABB(EntityLiving.class,
                new AxisAlignedBB(posX - range, posY - rangeUpDown, posZ - range, posX + range, posY + rangeUpDown, posZ + range));
        for (EntityLiving ent : entities) {
            if (ent instanceof EntityTF2Sentry) continue;
            String name = EntityList.getEntityString(ent);
            if (name != null && SentryTargets.SERVER.contains(name) && !(ent instanceof EntityTameable && ((EntityTameable) ent).isTamed())) {
                if (!canEntityBeSeen(ent)) continue;
                if (target == null || getDistanceSq(ent) < getDistanceSq(target)) {
                    target = ent;
                    changed = true;
                }
            }
        }
        if (changed) SentrySounds.play(this, "sentry_spot", SentryConfig.spotVolume);
        return target;
    }

    protected void upgradeSentry() {
        SentrySounds.play(this, "sentry_finish", 0.3F);
        EntityTF2Sentry next = createUpgrade();
        if (next == null || world.isRemote) return;
        next.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
        next.rotationYawHead = rotationYawHead;
        next.setBaseYaw(dataManager.get(BASE_YAW));
        next.set(KILLS, get(KILLS));
        if (isBlue()) next.setBlue();
        world.spawnEntity(next);
        setDead();
    }

    protected EntityTF2Sentry createUpgrade() { return new EntityTF2Sentry2(world); }

    protected void attackEntity(Entity entity) {
        if (!canEntityBeSeen(entity)) return;
        double dx = entity.posX - posX;
        double dy = entity.posY - posY + entity.getEyeHeight() - 0.6;
        double dz = entity.posZ - posZ;
        if (attackTimer == 0) {
            if (!SentryConfig.infiniteAmmo && get(AMMO) <= 0) {
                SentrySounds.play(this, "sentry_empty", SentryConfig.shootVolume);
            } else {
                EntityTF2Bullet bullet = new EntityTF2Bullet(world, this);
                SentrySounds.play(this, getShootSound(), SentryConfig.shootVolume);
                bullet.shoot(dx, dy, dz, 5.0F, 0.0F);
                world.spawnEntity(bullet);
                if (!SentryConfig.infiniteAmmo) set(AMMO, get(AMMO) - 1);
            }
            attackTimer = attackDelay;
        }
        faceEntity(entity, 15.0F, 15.0F);
    }

    @Override
    public void faceEntity(Entity e, float maxYaw, float maxPitch) {
        double dx = e.posX - posX;
        double dz = e.posZ - posZ;
        double dy;
        if (e instanceof EntityLivingBase) dy = posY + getEyeHeight() - (e.posY + e.getEyeHeight());
        else dy = (e.getEntityBoundingBox().minY + e.getEntityBoundingBox().maxY) / 2.0 - (posY + getEyeHeight());
        double h = MathHelper.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (Math.atan2(dy, h) * 180.0 / Math.PI);
        rotationPitch = updateRotation(rotationPitch, pitch, maxPitch);
        rotationYaw = updateRotation(rotationYaw, yaw, maxYaw);
        rotationYawHead = rotationYaw;
    }

    private static float updateRotation(float cur, float want, float max) {
        float d = MathHelper.wrapDegrees(want - cur);
        if (d > max) d = max;
        if (d < -max) d = -max;
        return cur + d;
    }

    protected boolean isWrench(ItemStack s) {
        return !s.isEmpty() && (s.getItem() == TF2SentryMod.wrench || s.getItem() == TF2SentryMod.wrenchGolden);
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float amount) {
        if (src.getTrueSource() instanceof EntityPlayer && src.getImmediateSource() == src.getTrueSource()) {
            ItemStack stack = ((EntityPlayer) src.getTrueSource()).getHeldItemMainhand();
            if (isWrench(stack)) {
                if (!world.isRemote) wrenchHit(stack.getItem() == TF2SentryMod.wrenchGolden ? 2 : 1);
                return true;
            }
        }
        return super.attackEntityFrom(src, amount);
    }

    /** heal / refill / upgrade on a wrench hit; power 2 = golden wrench */
    protected void wrenchHit(int power) {
        extinguish();
        if (getHealth() < getMaxHealth()) {
            heal(power);
            showHearts();
        }
        if (get(AMMO) < getMaxAmmo()) {
            set(AMMO, Math.min(getMaxAmmo(), get(AMMO) + 10 * power));
            showHearts();
        }
        SentrySounds.play(this, "sentry_upgrading", SentryConfig.upgradeVolume);
        if (canUpgrade()) {
            set(UPGRADES, get(UPGRADES) + power);
            if (get(UPGRADES) >= upgradesNeeded) upgradeSentry();
        }
    }

    protected boolean canUpgrade() { return true; }

    public void showHearts() {
        if (world instanceof WorldServer)
            ((WorldServer) world).spawnParticle(EnumParticleTypes.HEART, posX, posY + 0.5 + rand.nextFloat() * height,
                    posZ, 1, width / 2, 0.1, width / 2, 0.02);
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (isWrench(player.getHeldItem(hand))) {
            if (world.isRemote) TF2SentryMod.proxy.openStatusGUI(this);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public float getEyeHeight() { return 1.0F; }

    @Override
    protected void updateAITasks() {
        entityToAttack = findPlayerToAttack();
        if (entityToAttack != null && !entityToAttack.isEntityAlive()) entityToAttack = null;
        if (entityToAttack != null && canEntityBeSeen(entityToAttack)) attackEntity(entityToAttack);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        super.travel(0, 0, 0);
    }

    @Override
    public void onDeath(DamageSource src) {
        if (src.getTrueSource() instanceof EntityPlayer && !world.isRemote && !dropAsItem) {
            dropAsItem = true;
            entityDropItem(new ItemStack(isBlue() ? TF2SentryMod.sentryBlue : TF2SentryMod.sentryRed), 0.0F);
        }
        super.onDeath(src);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        if (!dropAsItem) dropItem(Items.IRON_INGOT, getState());
    }

    @Override
    protected Item getDropItem() { return Items.IRON_INGOT; }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public boolean getCanSpawnHere() { return false; }

    @Override
    protected boolean canDespawn() { return false; }

    @Override
    public boolean canBePushed() { return false; }

    @Override
    public void knockBack(Entity e, float strength, double x, double z) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (isBlue()) tag.setBoolean("Blue", true);
        tag.setFloat("yawOffset", dataManager.get(BASE_YAW));
        tag.setInteger("ammo", get(AMMO));
        tag.setInteger("upgrades", get(UPGRADES));
        tag.setInteger("kills", get(KILLS));
        tag.setInteger("rockets", get(ROCKETS));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.getBoolean("Blue")) setBlue();
        setBaseYaw(tag.getFloat("yawOffset"));
        set(AMMO, tag.getInteger("ammo"));
        set(UPGRADES, tag.getInteger("upgrades"));
        set(KILLS, tag.getInteger("kills"));
        if (tag.hasKey("rockets")) set(ROCKETS, tag.getInteger("rockets"));
    }

    public int getState() { return 1; }

    public boolean isBlue() { return dataManager.get(BLUE); }

    public void setBlue() { dataManager.set(BLUE, true); }

    @Override
    public void onKillEntity(EntityLivingBase killed) {
        set(KILLS, get(KILLS) + 1);
    }

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SentrySounds.get("sentry_damage"); }

    @Override
    protected SoundEvent getDeathSound() { return SentrySounds.get("sentry_damage"); }

    @Override
    protected float getSoundVolume() { return 0.2F; }

    protected String getShootSound() { return "sentry_1shoot"; }

    public int getMaxAmmo() { return 50; }

    public int getMaxRockets() { return 20; }
}
