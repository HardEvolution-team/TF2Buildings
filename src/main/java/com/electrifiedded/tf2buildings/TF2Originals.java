package com.electrifiedded.tf2buildings;

import com.electrifiedded.archaiclib.original.OriginalModAssets;

/** ARR handling: textures/sounds/lang come from the player's own original 1.7.10 jars in archaic_originals. */
public final class TF2Originals {
    public static final String URL = "https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1272373 (TF2 Teleporter, Sentry, Dispenser by pitman-87)";

    private TF2Originals() {}

    /** Called from the constructor of each @Mod. Our own sounds.json / lang replace the 1.7 ones. */
    public static void require(String domain, String jarName) {
        OriginalModAssets.hide(domain, "sounds.json");
        OriginalModAssets.hide(domain, "lang/en_us.lang");
        OriginalModAssets.require(domain, domain, jarName + " (1.7.10, by pitman-87)", URL, domain);
    }
}
