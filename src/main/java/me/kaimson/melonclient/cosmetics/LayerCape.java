package me.kaimson.melonclient.cosmetics;

import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.model.*;
import net.minecraft.client.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.*;
import net.minecraft.util.*;

public class LayerCape extends ModelBase implements LayerRenderer<EntityLivingBase> {
    private final ModelRenderer bipedCape;

    public LayerCape() {
        (this.bipedCape = new ModelRenderer(this, 0, 0).setTextureSize(22, 17).setTextureOffset(0, 0)).addBox(-5.0f, 0.0f, -1.0f, 10, 16, 1);
    }

    public void doRenderLayer(final EntityLivingBase entitylivingbaseIn, final float p_177141_2_, final float p_177141_3_, final float partialTicks, final float p_177141_5_, final float p_177141_6_, final float p_177141_7_, final float scale) {
        final AbstractClientPlayer player = (AbstractClientPlayer) entitylivingbaseIn;
        if (player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(EnumPlayerModelParts.CAPE) && CosmeticManager.hasCape(player.getUniqueID().toString())) {
            this.render(entitylivingbaseIn, p_177141_2_, p_177141_3_, 0.0f, p_177141_5_, partialTicks, scale);
        }
    }

    public void render(final Entity entityIn, final float p_78088_2_, final float p_78088_3_, final float p_78088_4_, final float p_78088_5_, final float partialTicks, final float scale) {
        final AbstractClientPlayer entitylivingbaseIn = (AbstractClientPlayer) entityIn;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(CosmeticManager.cosmetics.get(entitylivingbaseIn.getUniqueID().toString()).getCapeTexture());
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.125F);
        double d0 = entitylivingbaseIn.prevChasingPosX
                + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double) partialTicks
                - (entitylivingbaseIn.prevPosX
                + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double) partialTicks);
        double d1 = entitylivingbaseIn.prevChasingPosY
                + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double) partialTicks
                - (entitylivingbaseIn.prevPosY
                + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double) partialTicks);
        double d2 = entitylivingbaseIn.prevChasingPosZ
                + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double) partialTicks
                - (entitylivingbaseIn.prevPosZ
                + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double) partialTicks);
        float f = entitylivingbaseIn.prevRenderYawOffset
                + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
        double d3 = (double) MathHelper.sin(f * (float) Math.PI / 180.0F);
        double d4 = (double) (-MathHelper.cos(f * (float) Math.PI / 180.0F));
        float f1 = (float) d1 * 10.0F;
        f1 = MathHelper.clamp_float(f1, -3.0F, 12.0F);
        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        float f4 = entitylivingbaseIn.prevCameraYaw
                + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
        f1 = f1 + MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified
                + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified)
                * partialTicks)
                * 6.0F) * 32.0F * f4;

        if (entitylivingbaseIn.isSneaking()) {

            f1 += 35.0F;
            GlStateManager.translate(0.0, 0.07, -0.2);
        }

        GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        this.bipedCape.render(scale);

        GlStateManager.popMatrix();
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(0.0f, 0.0f, 0.125f);
//        double d0 = entitylivingbaseIn.prevChasingPosX
//                + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double) partialTicks
//                - (entitylivingbaseIn.prevPosX
//                + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double) partialTicks);
//        double d1 = entitylivingbaseIn.prevChasingPosY
//                + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double) partialTicks
//                - (entitylivingbaseIn.prevPosY
//                + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double) partialTicks);
//        double d2 = entitylivingbaseIn.prevChasingPosZ
//                + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double) partialTicks
//                - (entitylivingbaseIn.prevPosZ
//                + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double) partialTicks);
//        float f = entitylivingbaseIn.prevRenderYawOffset
//                + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
//        final double d4 = MathHelper.sin(f * 3.1415927f / 180.0f);
//        final double d5 = -MathHelper.cos(f * 3.1415927f / 180.0f);
//        float f2 = (float) d2 * 10.0f;
//        f2 = MathHelper.clamp_float(f2, -6.0f, 32.0f);
//        float f3 = (float) (d0 * d4 + d3 * d5) * 100.0f;
//        final float f4 = (float) (d0 * d5 - d3 * d4) * 100.0f;
//        if (f3 < 0.0f) {
//            f3 = 0.0f;
//        }
//        if (f3 > 165.0f) {
//            f3 = 165.0f;
//        }
//        if (f2 < -5.0f) {
//            f2 = -5.0f;
//        }
//        final float f5 = entitylivingbaseIn.prevCameraYaw
//                + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
//        f2 += MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified
//                + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified)
//                * partialTicks)
//                * 6.0F) * 32.0f * f5;
//        if (entitylivingbaseIn.isSneaking()) {
//            f2 += 25.0f;
//            GlStateManager.translate(0.0f, 0.142f, -0.0178f);
//        }
//        GlStateManager.rotate(6.0f + f3 / 2.0f + f2, 1.0f, 0.0f, 0.0f);
//        GlStateManager.rotate(f4 / 2.0f, 0.0f, 0.0f, 1.0f);
//        GlStateManager.rotate(-f4 / 2.0f, 0.0f, 1.0f, 0.0f);
//        GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
//        this.bipedCape.render(scale);
//        GlStateManager.popMatrix();
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}
