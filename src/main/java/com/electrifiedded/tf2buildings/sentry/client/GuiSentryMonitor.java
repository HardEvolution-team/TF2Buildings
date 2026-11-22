package com.electrifiedded.tf2buildings.sentry.client;

import com.electrifiedded.tf2buildings.sentry.SentryTargets;
import com.electrifiedded.tf2buildings.sentry.TF2SentryMod;
import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Sentry;
import com.electrifiedded.tf2buildings.sentry.net.MsgTargets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sentry Monitor: tick the mob types sentries should shoot (server-wide list). */
public class GuiSentryMonitor extends GuiScreen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("tf2sentry", "textures/gui/GuiMonitor.png");
    private static final List<String> MOBS = new ArrayList<>();
    private final int xs = 176, ys = 166;
    private int listHeight, scrollY, scrollHeight = 139;
    private boolean scrolling;

    public GuiSentryMonitor() {
        if (MOBS.isEmpty()) {
            for (EntityEntry e : ForgeRegistries.ENTITIES.getValuesCollection()) {
                Class<?> c = e.getEntityClass();
                if (c == null || !EntityLiving.class.isAssignableFrom(c) || EntityTF2Sentry.class.isAssignableFrom(c)
                        || Modifier.isAbstract(c.getModifiers()) || c.getName().contains("Familiar")) continue;
                if (!MOBS.contains(e.getName())) MOBS.add(e.getName());
            }
            Collections.sort(MOBS);
        }
        if (!MOBS.isEmpty()) {
            listHeight = 14 * ((MOBS.size() + 1) / 2) - 140;
            scrollHeight = (int) (139.0 / (listHeight + 139) * 139.0);
            if (scrollHeight <= 0 || scrollHeight > 139) scrollHeight = 139;
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) throws IOException {
        super.mouseClicked(mx, my, button);
        if (button == 1) {
            mc.displayGuiScreen(null);
            return;
        }
        int x = mx - ((width - xs) >> 1), y = my - ((height - ys) >> 1);
        if (button == 0 && x >= 10 && x < 165 && y >= 20 && y < 159) {
            for (int i = 0; i < MOBS.size(); i++) {
                if (inRadio(x, y, i)) {
                    String m = MOBS.get(i);
                    if (!SentryTargets.CLIENT.remove(m)) SentryTargets.CLIENT.add(m);
                }
            }
        }
    }

    private boolean inRadio(int x, int y, int i) {
        int bx = 10 + ((i & 1) != 0 ? 80 : 0);
        int by = 14 * (i >> 1) + 20 - scrollY;
        return x >= bx - 1 && x < bx + 60 && y >= by - 1 && y < by + 10;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        int left = (width - xs) >> 1, top = (height - ys) >> 1;
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(left, top, 0, 0, xs, ys);
        for (int i = 0; i < MOBS.size(); i++) {
            int bx = left + 10 + ((i & 1) != 0 ? 80 : 0);
            int by = top + 14 * (i >> 1) + 20 - scrollY;
            if (by >= top + 20 && by + 9 < top + 159 && SentryTargets.CLIENT.contains(MOBS.get(i)))
                drawTexturedModalRect(bx, by, 184, 0, 8, 9);
        }
        if (scrollHeight != 139) {
            int sx = left + 163;
            int sy = top + 19 + scrollY * (139 - scrollHeight) / Math.max(1, listHeight);
            drawTexturedModalRect(sx, sy, 176, 9, 5, 1);
            int k;
            for (k = sy + 1; k < sy + scrollHeight - 1; k++) drawTexturedModalRect(sx, k, 176, 10, 5, 1);
            drawTexturedModalRect(sx, k, 176, 11, 5, 1);
        }
        fontRenderer.drawString("Sentry Monitor", left + 50, top + 8, 0);
        for (int i = 0; i < MOBS.size(); i++) {
            int tx = left + ((i & 1) != 0 ? 80 : 0) + 19;
            int ty = 14 * (i >> 1) + 20 - scrollY;
            if (ty >= 20 && ty + 9 < 159) {
                String name = MOBS.get(i);
                fontRenderer.drawString(fontRenderer.trimStringToWidth(name.substring(name.lastIndexOf('.') + 1), 60), tx, top + ty, 0);
            }
        }
        if (scrollHeight != 139) {
            int x = mx - left, y = my - top;
            if (Mouse.isButtonDown(0)) {
                if (x >= 165 && x < 170 && y >= 20 && y < 159) scrolling = true;
            } else scrolling = false;
            if (scrolling) scrollY = clamp((y - 20) * listHeight / (139 - (scrollHeight >> 1)));
        }
        super.drawScreen(mx, my, pt);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int w = Mouse.getEventDWheel();
        if (scrollHeight != 139 && w != 0) scrollY = clamp(scrollY + (w < 0 ? 14 : -14));
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(listHeight, v));
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void onGuiClosed() {
        TF2SentryMod.NET.sendToServer(new MsgTargets(SentryTargets.CLIENT));
    }
}
