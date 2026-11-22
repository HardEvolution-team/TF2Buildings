package com.electrifiedded.tf2buildings.sentry.client;

import com.electrifiedded.tf2buildings.sentry.SentryCommonProxy;
import com.electrifiedded.tf2buildings.sentry.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class SentryClientProxy extends SentryCommonProxy {
    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityTF2Sentry3.class, m -> new SentryRenderers.Sentry(m, new ModelTF2Sentry3(), true));
        RenderingRegistry.registerEntityRenderingHandler(EntityTF2Sentry2.class, m -> new SentryRenderers.Sentry(m, new ModelTF2Sentry2(), true));
        RenderingRegistry.registerEntityRenderingHandler(EntityTF2Sentry.class, m -> new SentryRenderers.Sentry(m, new ModelTF2Sentry(), false));
        RenderingRegistry.registerEntityRenderingHandler(EntityTF2Bullet.class, m -> new SentryRenderers.Projectile(m, "TF2Bullet"));
        RenderingRegistry.registerEntityRenderingHandler(EntityTF2Rocket.class, m -> new SentryRenderers.Projectile(m, "TF2Rocket"));
    }

    @Override
    public void openMonitorGUI() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiSentryMonitor());
    }

    @Override
    public void openStatusGUI(EntityTF2Sentry sentry) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiSentryStatus(sentry));
    }
}
