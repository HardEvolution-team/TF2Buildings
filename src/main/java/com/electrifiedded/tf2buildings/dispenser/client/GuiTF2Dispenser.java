package com.electrifiedded.tf2buildings.dispenser.client;

import com.electrifiedded.tf2buildings.dispenser.TF2DispenserMod;
import com.electrifiedded.tf2buildings.dispenser.inventory.ContainerTF2Dispenser;
import com.electrifiedded.tf2buildings.dispenser.net.MsgOptions;
import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiTF2Dispenser extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("tf2dispenser", "textures/gui/GuiTF2Dispenser.png");
    private static final String[] KEYS = {"heal", "feed", "repair", "neutralize"};
    private final TileEntityTF2Dispenser dispenser;

    public GuiTF2Dispenser(InventoryPlayer inv, TileEntityTF2Dispenser te) {
        super(new ContainerTF2Dispenser(inv, te));
        dispenser = te;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        fontRenderer.drawString(I18n.format("gui.tf2dispenser.title"), 60, 6, 4210752);
        for (int i = 0; i < 4; i++) {
            String s = I18n.format("gui.tf2dispenser." + KEYS[i]);
            fontRenderer.drawString(s, 160 - fontRenderer.getStringWidth(s), 25 + 15 * i, 4210752);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TEXTURE);
        int j = (width - xSize) / 2, k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
        for (int i = 0; i < 4; i++)
            if ((dispenser.options & (1 << i)) != 0) drawTexturedModalRect(162 + j, 25 + 15 * i + k, 184, 0, 8, 9);
        int h = dispenser.getMaxEnergy() != 0 ? 60 * dispenser.energy / dispenser.getMaxEnergy() : 0;
        drawTexturedModalRect(j + 28, k + 74 - h, 177, 16, 25, h);
    }

    private int getButton(int x, int y) {
        if (x >= 120 && x < 160 && y >= 25 && y < 35) return 1;
        if (x >= 120 && x < 160 && y >= 40 && y < 50) return 2;
        if (x >= 120 && x < 160 && y >= 55 && y < 65) return 4;
        return x >= 65 && x < 170 && y >= 70 && y < 80 ? 8 : 0;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        int option = getButton(x - (width - xSize) / 2, y - (height - ySize) / 2);
        if (option != 0 && button == 0) {
            dispenser.options = (byte) (dispenser.options ^ option);
            TF2DispenserMod.NET.sendToServer(new MsgOptions(dispenser.getPos(), dispenser.options));
        }
    }
}
