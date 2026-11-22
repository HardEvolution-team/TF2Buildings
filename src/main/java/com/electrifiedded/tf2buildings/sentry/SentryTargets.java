package com.electrifiedded.tf2buildings.sentry;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.*;
import java.util.*;

/** Server-side list of entity names (EntityList.getEntityString) sentries attack; client keeps a mirror. */
public final class SentryTargets {
    public static final Set<String> SERVER = Collections.synchronizedSet(new LinkedHashSet<>());
    public static final List<String> CLIENT = new ArrayList<>();
    private static final String[] DEFAULTS = {"Creeper", "Skeleton", "Spider", "Giant", "Zombie", "Slime", "Ghast", "PigZombie"};

    private SentryTargets() {}

    private static File file() {
        MinecraftServer s = FMLCommonHandler.instance().getMinecraftServerInstance();
        return s.getFile("TF2SentryTargetlist.txt");
    }

    public static void read() {
        SERVER.clear();
        File f = file();
        if (!f.exists()) {
            SERVER.addAll(Arrays.asList(DEFAULTS));
            write();
            return;
        }
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(f)) {
            p.load(in);
            for (String k : p.stringPropertyNames()) if ("true".equals(p.getProperty(k))) SERVER.add(k);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write() {
        Properties p = new Properties();
        synchronized (SERVER) { for (String s : SERVER) p.setProperty(s, "true"); }
        try (OutputStream out = new FileOutputStream(file())) {
            p.store(out, "TF2 Sentry Targetlist");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
