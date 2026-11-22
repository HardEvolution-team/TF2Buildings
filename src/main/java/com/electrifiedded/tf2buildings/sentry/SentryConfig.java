package com.electrifiedded.tf2buildings.sentry;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public final class SentryConfig {
    public static float shootVolume = 0.2F, spotVolume = 0.2F, explosionVolume = 0.5F, upgradeVolume = 0.3F;
    public static boolean opsOnly = true, infiniteAmmo = false, rocketsBreakBlocks = true;
    public static int range1 = 15, range2 = 20, range3 = 25, rangeUpDown1 = 7, rangeUpDown2 = 7, rangeUpDown3 = 7;

    private SentryConfig() {}

    public static void load(File f) {
        Configuration c = new Configuration(f);
        c.load();
        opsOnly = c.getBoolean("opsOnly", "general", true, "Only OPs may change the sentry target list.");
        infiniteAmmo = c.getBoolean("infiniteAmmo", "general", false, "Sentries never run out of ammo.");
        rocketsBreakBlocks = c.getBoolean("rocketsBreakBlocks", "general", true, "Level 3 rockets destroy blocks (original behaviour).");
        shootVolume = c.getFloat("shootVolume", "sound", 0.2F, 0F, 1F, "");
        spotVolume = c.getFloat("spotVolume", "sound", 0.2F, 0F, 1F, "");
        explosionVolume = c.getFloat("explosionVolume", "sound", 0.5F, 0F, 1F, "");
        upgradeVolume = c.getFloat("upgradeVolume", "sound", 0.3F, 0F, 1F, "");
        range1 = c.getInt("range1", "ranges", 15, 1, 50, "Detecting range of sentry level 1");
        range2 = c.getInt("range2", "ranges", 20, 1, 50, "Detecting range of sentry level 2");
        range3 = c.getInt("range3", "ranges", 25, 1, 50, "Detecting range of sentry level 3");
        rangeUpDown1 = c.getInt("rangeUpDown1", "ranges", 7, 1, 50, "Vertical range of sentry level 1");
        rangeUpDown2 = c.getInt("rangeUpDown2", "ranges", 7, 1, 50, "Vertical range of sentry level 2");
        rangeUpDown3 = c.getInt("rangeUpDown3", "ranges", 7, 1, 50, "Vertical range of sentry level 3");
        if (c.hasChanged()) c.save();
    }
}
