package com.electrifiedded.tf2buildings.dispenser.client;

import com.electrifiedded.tf2buildings.dispenser.tile.TileEntityTF2Dispenser;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileEntityTF2DispenserRenderer extends TileEntitySpecialRenderer<TileEntityTF2Dispenser> {
    private static final ResourceLocation RED = new ResourceLocation("tf2dispenser", "textures/model/dispenserRed.png");
    private static final ResourceLocation BLUE = new ResourceLocation("tf2dispenser", "textures/model/dispenserBlue.png");
    private final ModelTF2Dispenser model = new ModelTF2Dispenser();

    @Override
    public void render(TileEntityTF2Dispenser te, double x, double y, double z, float pt, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GlStateManager.scale(0.3F, 0.3F, 0.3F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        int rot;
        switch (te.getFacing()) {
            case NORTH: rot = 90; break;
            case SOUTH: rot = -90; break;
            case EAST: rot = -180; break;
            default: rot = 0;
        }
        GlStateManager.rotate(rot, 0.0F, 1.0F, 0.0F);
        bindTexture(te.isRed() ? RED : BLUE);
        GlStateManager.color(1, 1, 1, 1);
        RenderHelper.disableStandardItemLighting();
        model.render(te);
        if (te.isActive()) drawDisplay(0, 0, 145, 106, 89, 47);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void drawDisplay(int i, int j, int k, int l, int i1, int j1) {
        float s = 0.0625F / 3.0F;
        float f = 0.00390625F;
        double zl = 0.0;
        GlStateManager.scale(s, s, s);
        GlStateManager.translate(36.1F, -92.5F, -44.85F);
        quad(i, j, k, l, i1, j1, f, zl);
        GlStateManager.translate(-72.2F, -0.021621622F, 89.7F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        quad(i, j, k, l, i1, j1, f, zl);
    }

    private static void quad(int i, int j, int k, int l, int i1, int j1, float f, double zl) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_TEX);
        b.pos(i, j + j1, zl).tex(k * f, (l + j1) * f).endVertex();
        b.pos(zl, j + j1, i + i1).tex((k + i1) * f, (l + j1) * f).endVertex();
        b.pos(zl, j, i + i1).tex((k + i1) * f, l * f).endVertex();
        b.pos(i, j, zl).tex(k * f, l * f).endVertex();
        t.draw();
    }
}
