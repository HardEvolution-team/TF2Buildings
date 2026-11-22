package com.electrifiedded.tf2buildings.sentry;

import com.electrifiedded.archaiclib.legacy.sound.LegacyModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public final class SentrySounds {
    private SentrySounds() {}

    public static SoundEvent get(String name) {
        return LegacyModSounds.get(TF2SentryMod.MODID, name);
    }

    /** server side: heard by everybody around (replaces the 1.7 "effect" packets) */
    public static void play(Entity at, String name, float volume) {
        SoundEvent e = get(name);
        if (e != null && !at.world.isRemote)
            at.world.playSound(null, at.posX, at.posY, at.posZ, e, SoundCategory.HOSTILE, volume, 1.0F);
    }
}
