package com.electrifiedded.tf2buildings.dispenser;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public final class DispenserConfig {
    public static float fillUpVolume = 0.4F, beamVolume = 0.3F;
    public static boolean allowHealing = true, allowFeeding = true, allowRepairing = true, allowNeutralizingAffects = true;
    public static boolean defaultHealing = true, defaultFeeding = true, defaultRepairing = true, defaultNeutralizingAffects = false;
    private static String[] itemsCfg = new String[0];
    public static final Set<Item> IGNORED = new HashSet<>();

    private DispenserConfig() {}

    public static void load(File f) {
        Configuration c = new Configuration(f);
        c.load();
        fillUpVolume = c.getFloat("fillUpVolume", "sound", 0.4F, 0F, 1F, "");
        beamVolume = c.getFloat("beamVolume", "sound", 0.3F, 0F, 1F, "");
        String allow = "Set to false, if nobody is allowed to change that option!";
        allowHealing = c.getBoolean("allowHealing", "allow", true, allow);
        allowFeeding = c.getBoolean("allowFeeding", "allow", true, allow);
        allowRepairing = c.getBoolean("allowRepairing", "allow", true, allow);
        allowNeutralizingAffects = c.getBoolean("allowNeutralizingAffects", "allow", true, allow);
        String def = "Whether that option is enabled by default on a new dispenser.";
        defaultHealing = c.getBoolean("defaultHealing", "default", true, def);
        defaultFeeding = c.getBoolean("defaultFeeding", "default", true, def);
        defaultRepairing = c.getBoolean("defaultRepairing", "default", true, def);
        defaultNeutralizingAffects = c.getBoolean("defaultNeutralizingAffects", "default", false, def);
        itemsCfg = c.getStringList("ignoredItems", "general", new String[0],
                "Item registry names the dispenser will not repair, e.g. minecraft:iron_shovel");
        if (c.hasChanged()) c.save();
    }

    /** postInit, when all items exist */
    public static void readItems() {
        IGNORED.clear();
        for (String s : itemsCfg) {
            Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s.trim()));
            if (i != null) IGNORED.add(i);
        }
    }
}
