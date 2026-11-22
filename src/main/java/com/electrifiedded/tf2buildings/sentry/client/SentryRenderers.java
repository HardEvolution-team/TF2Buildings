package com.electrifiedded.tf2buildings.sentry.client;

import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Projectile;
import com.electrifiedded.tf2buildings.sentry.entity.EntityTF2Sentry;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public final class SentryRenderers {
    private SentryRenderers() {}

    private static ResourceLocation tex(String path) {
        return new ResourceLocation("tf2sentry", "textures/model/" + path + ".png");
    }

    public static class Sentry extends RenderLiving<EntityTF2Sentry> {
        private static final ResourceLocation[] RED = {tex("TF2SentryRed"), tex("TF2Sentry2Red"), tex("TF2Sentry3Red")};
        private static final ResourceLocation[] BLUE = {tex("TF2SentryBlue"), tex("TF2Sentry2Blue"), tex("TF2Sentry3Blue")};
        private final boolean customAngles;

        public Sentry(RenderManager m, ModelBase model, boolean customAngles) {
            super(m, model, 0.0F);
            this.customAngles = customAngles;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityTF2Sentry e) {
            int i = Math.max(0, Math.min(2, e.getState() - 1));
            return e.isBlue() ? BLUE[i] : RED[i];
        }

        @Override
        protected boolean canRenderName(EntityTF2Sentry entity) {
            return entity.hasCustomName() && super.canRenderName(entity);
        }
    }

    /** the 1.7 arrow-style quad projectile (bullet and rocket share the shape, different textures) */
    public static class Projectile extends Render<EntityTF2Projectile> {
        private final ResourceLocation texture;

        public Projectile(RenderManager m, String texture) {
            super(m);
            this.texture = tex(texture);
        }

        @Override
        public void doRender(EntityTF2Projectile e, double x, double y, double z, float yaw, float pt) {
            bindEntityTexture(e);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.rotate(e.prevRotationYaw + (e.rotationYaw - e.prevRotationYaw) * pt - 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(e.prevRotationPitch + (e.rotationPitch - e.prevRotationPitch) * pt, 0.0F, 0.0F, 1.0F);
            Tessellator t = Tessellator.getInstance();
            BufferBuilder b = t.getBuffer();
            float u0 = 0.0F, u1 = 0.5F, v0 = 0 / 32.0F, v1 = 5 / 32.0F;
            float eu0 = 0.0F, eu1 = 0.15625F, ev0 = 5 / 32.0F, ev1 = 10 / 32.0F;
            float s = 0.05625F;
            GlStateManager.enableRescaleNormal();
            GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(s, s, s);
            GlStateManager.translate(-4.0F, 0.0F, 0.0F);
            GlStateManager.glNormal3f(s, 0.0F, 0.0F);
            b.begin(7, DefaultVertexFormats.POSITION_TEX);
            b.pos(-7, -2, -2).tex(eu0, ev0).endVertex();
            b.pos(-7, -2, 2).tex(eu1, ev0).endVertex();
            b.pos(-7, 2, 2).tex(eu1, ev1).endVertex();
            b.pos(-7, 2, -2).tex(eu0, ev1).endVertex();
            t.draw();
            GlStateManager.glNormal3f(-s, 0.0F, 0.0F);
            b.begin(7, DefaultVertexFormats.POSITION_TEX);
            b.pos(-7, 2, -2).tex(eu0, ev0).endVertex();
            b.pos(-7, 2, 2).tex(eu1, ev0).endVertex();
            b.pos(-7, -2, 2).tex(eu1, ev1).endVertex();
            b.pos(-7, -2, -2).tex(eu0, ev1).endVertex();
            t.draw();
            for (int j = 0; j < 4; j++) {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.glNormal3f(0.0F, 0.0F, s);
                b.begin(7, DefaultVertexFormats.POSITION_TEX);
                b.pos(-8, -2, 0).tex(u0, v0).endVertex();
                b.pos(8, -2, 0).tex(u1, v0).endVertex();
                b.pos(8, 2, 0).tex(u1, v1).endVertex();
                b.pos(-8, 2, 0).tex(u0, v1).endVertex();
                t.draw();
            }
            GlStateManager.disableRescaleNormal();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
            super.doRender(e, x, y, z, yaw, pt);
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityTF2Projectile e) {
            return texture;
        }
    }
}
