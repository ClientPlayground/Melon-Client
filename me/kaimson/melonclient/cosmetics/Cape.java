package me.kaimson.melonclient.cosmetics;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class Cape extends ModelBase {
  private final RenderPlayer rp;
  
  private ModelRenderer bipedCape;
  
  private ResourceLocation resourceLocation;
  
  public Cape(RenderPlayer rp) {
    this.rp = rp;
    this.bipedCape = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 32).setTextureOffset(0, 0);
    this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1);
  }
  
  public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float partialTicks, float scale) {
    this.bipedCape = (new ModelRenderer(this, 0, 0)).setTextureSize(22, 17).setTextureOffset(0, 0);
    this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1);
    this.resourceLocation = new ResourceLocation("melonclient/capes/The_god_of_all_capes.png");
    AbstractClientPlayer entitylivingbaseIn = (AbstractClientPlayer)entityIn;
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.rp.bindTexture(this.resourceLocation);
    GlStateManager.pushMatrix();
    GlStateManager.translate(0.0F, 0.0F, 0.125F);
    double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * partialTicks - entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * partialTicks;
    double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * partialTicks - entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * partialTicks;
    double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * partialTicks - entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * partialTicks;
    float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
    double d3 = MathHelper.sin(f * 3.1415927F / 180.0F);
    double d4 = -MathHelper.cos(f * 3.1415927F / 180.0F);
    float f1 = (float)d1 * 10.0F;
    f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
    float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
    float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
    if (f2 < 0.0F)
      f2 = 0.0F; 
    if (f2 > 165.0F)
      f2 = 165.0F; 
    if (f1 < -5.0F)
      f1 = -5.0F; 
    float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
    f1 += MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;
    if (entitylivingbaseIn.isSneaking()) {
      f1 += 25.0F;
      GlStateManager.translate(0.0F, 0.142F, -0.0178F);
    } 
    GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
    this.bipedCape.render(scale);
    GlStateManager.popMatrix();
  }
}
