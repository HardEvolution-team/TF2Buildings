package com.electrifiedded.tf2buildings.dispenser.client;

import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The 1.7 energy beam: an invisible point that moves from the dispenser towards the player
 * (1/5 of the remaining distance per tick) and leaves small team coloured particles behind.
 */
public final class DispenserBeams {
    private static final List<Beam> BEAMS = new ArrayList<>();

    private DispenserBeams() {}

    public static void spawn(BlockPos pos, int[] ids) {
        Minecraft mc = Minecraft.getMinecraft();
        World w = mc.world;
        if (w == null) return;
        TileEntity te = w.getTileEntity(pos);
        boolean red = te instanceof TileEntityTF2Dispenser && ((TileEntityTF2Dispenser) te).isRed();
        for (int id : ids) {
            Entity p = w.getEntityByID(id);
            if (p == null) continue;
            Beam b = new Beam(pos, p, p == mc.player ? -0.5F : 1.0F);
            if (red) b.setColor(0.953F, 0.42F, 0.333F);
            else b.setColor(0.2F, 0.545F, 0.839F);
            BEAMS.add(b);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.isGamePaused()) {
            if (mc.world == null) BEAMS.clear();
            return;
        }
        for (Iterator<Beam> it = BEAMS.iterator(); it.hasNext(); ) {
            Beam b = it.next();
            if (!b.tick(mc)) it.remove();
        }
    }

    private static final class Beam {
        final BlockPos origin;
        final Entity target;
        final float offset;
        double x, y, z;
        float r, g, bl;
        int age;

        Beam(BlockPos origin, Entity target, float offset) {
            this.origin = origin;
            this.target = target;
            this.offset = offset;
            x = origin.getX() + 0.5;
            y = origin.getY() + 0.5;
            z = origin.getZ() + 0.5;
        }

        void setColor(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.bl = b;
        }

        boolean tick(Minecraft mc) {
            if (target.isDead || target.world != mc.world || ++age > 40) return false;
            x += (target.posX - x) / 5.0;
            y += (target.posY + offset - y) / 5.0;
            z += (target.posZ - z) / 5.0;
            mc.effectRenderer.addEffect(new EnergyParticle(mc.world, x, y, z, r, g, bl));
            double ox = origin.getX() + 0.5, oy = origin.getY() + 0.5, oz = origin.getZ() + 0.5;
            double dis = (x - ox) * (x - ox) + (y - oy) * (y - oy) + (z - oz) * (z - oz);
            return !(dis > 28.0 || dis > target.getDistanceSq(ox, oy, oz) - 1.0);
        }
    }

    /** the 1.7 EntityTF2EnergyFX: a small fading reddust-like sprite */
    private static final class EnergyParticle extends Particle {
        EnergyParticle(World w, double x, double y, double z, float r, float g, float b) {
            super(w, x, y, z, 0, 0, 0);
            motionX *= 0.1F;
            motionY *= 0.1F;
            motionZ *= 0.1F;
            float f = (float) Math.random() * 0.4F + 0.6F;
            particleRed = ((float) (Math.random() * 0.2F) + 0.8F) * r * f;
            particleGreen = ((float) (Math.random() * 0.2F) + 0.8F) * g * f;
            particleBlue = ((float) (Math.random() * 0.2F) + 0.8F) * b * f;
            particleScale *= 0.25F;
            particleMaxAge = (int) (4.0 / (Math.random() * 0.8 + 0.2));
            canCollide = false;
        }

        @Override
        public void onUpdate() {
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            if (particleAge++ >= particleMaxAge) setExpired();
            setParticleTextureIndex(7 - particleAge * 8 / Math.max(1, particleMaxAge));
            move(motionX, motionY, motionZ);
            motionX *= 0.96F;
            motionY *= 0.96F;
            motionZ *= 0.96F;
        }
    }
}
