package com.electrifiedded.tf2buildings.sentry.client;

import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Sentry;
import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Sentry2;
import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Sentry3;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/** Wrench right-click: health, ammo, upgrade/rocket bars and the kill counter. */
public class GuiSentryStatus extends GuiScreen {
    private static final ResourceLocation RED = new ResourceLocation("tf2sentry", "textures/gui/GuiStatus.png");
    private static final ResourceLocation BLUE = new ResourceLocation("tf2sentry", "textures/gui/GuiStatusBlue.png");
    private final int xs = 175, ys = 91;
    private final EntityTF2Sentry sentry;

    public GuiSentryStatus(EntityTF2Sentry sentry) {
        this.sentry = sentry;
    }

    @Override
    public void updateScreen() {
        if (sentry.isDead) mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        super.drawScreen(mx, my, pt);
        GlStateManager.disableLighting();
        mc.getTextureManager().bindTexture(sentry.isBlue() ? BLUE : RED);
        GlStateManager.color(1, 1, 1, 1);
        int x = (width - xs) / 2, y = (height - ys) / 2;
        drawTexturedModalRect(x, y, 0, 0, xs, ys);
        if (!(sentry instanceof EntityTF2Sentry3)) {
            float u = 96.0F / sentry.upgradesNeeded;
            drawTexturedModalRect(x + 76, y + 65, 11, 98, Math.min(96, (int) (u * sentry.get(EntityTF2Sentry.UPGRADES))), 18);
        } else {
            float u = 96.0F / sentry.getMaxRockets();
            drawTexturedModalRect(x + 76, y + 65, 11, 98, (int) (u * sentry.get(EntityTF2Sentry.ROCKETS)), 18);
        }
        float a = 96.0F / sentry.getMaxAmmo();
        drawTexturedModalRect(x + 76, y + 37, 11, 98, (int) (a * sentry.get(EntityTF2Sentry.AMMO)), 18);
        int hv = sentry.getHealth() < sentry.getMaxHealth() / 2.0F ? 5 : 14;
        int bars = (int) (sentry.getHealth() / sentry.getMaxHealth() * 10.0F);
        for (int i = 0; i < bars; i++) drawTexturedModalRect(x + 24, y + 79 - i * 8, 210, hv, 18, 6);
        if (sentry instanceof EntityTF2Sentry3) {
            drawTexturedModalRect(x + 144, y + 9, 182, 36, 26, 26);
            drawTexturedModalRect(x + 47, y + 61, 179, 2, 26, 26);
        } else if (sentry instanceof EntityTF2Sentry2) {
            drawTexturedModalRect(x + 144, y + 9, 206, 36, 20, 20);
        }
        String kills = "" + sentry.get(EntityTF2Sentry.KILLS);
        fontRenderer.drawString(kills, width / 2 - fontRenderer.getStringWidth(kills) / 2, height / 2 - 33, 16049856);
        GlStateManager.enableLighting();
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) throws IOException {
        super.mouseClicked(mx, my, button);
        if (button == 1) mc.displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
