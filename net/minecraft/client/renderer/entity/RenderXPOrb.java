package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;

public class RenderXPOrb extends Render<EntityXPOrb> {
  private static final ResourceLocation experienceOrbTextures = new ResourceLocation("textures/entity/experience_orb.png");
  
  public RenderXPOrb(RenderManager renderManagerIn) {
    super(renderManagerIn);
    this.shadowSize = 0.15F;
    this.shadowOpaque = 0.75F;
  }
  
  public void doRender(EntityXPOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)x, (float)y, (float)z);
    bindEntityTexture(entity);
    int i = entity.getTextureByXP();
    float f = (i % 4 * 16 + 0) / 64.0F;
    float f1 = (i % 4 * 16 + 16) / 64.0F;
    float f2 = (i / 4 * 16 + 0) / 64.0F;
    float f3 = (i / 4 * 16 + 16) / 64.0F;
    float f4 = 1.0F;
    float f5 = 0.5F;
    float f6 = 0.25F;
    int j = entity.getBrightnessForRender(partialTicks);
    int k = j % 65536;
    int l = j / 65536;
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    float f7 = 255.0F;
    float f8 = (entity.xpColor + partialTicks) / 2.0F;
    if (Config.isCustomColors())
      f8 = CustomColors.getXpOrbTimer(f8); 
    l = (int)((MathHelper.sin(f8 + 0.0F) + 1.0F) * 0.5F * 255.0F);
    int i1 = 255;
    int j1 = (int)((MathHelper.sin(f8 + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
    GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
    float f9 = 0.3F;
    GlStateManager.scale(0.3F, 0.3F, 0.3F);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    int k1 = l;
    int l1 = 255;
    int i2 = j1;
    if (Config.isCustomColors()) {
      int j2 = CustomColors.getXpOrbColor(f8);
      if (j2 >= 0) {
        k1 = j2 >> 16 & 0xFF;
        l1 = j2 >> 8 & 0xFF;
        i2 = j2 >> 0 & 0xFF;
      } 
    } 
    worldrenderer.pos((0.0F - f5), (0.0F - f6), 0.0D).tex(f, f3).color(k1, l1, i2, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
    worldrenderer.pos((f4 - f5), (0.0F - f6), 0.0D).tex(f1, f3).color(k1, l1, i2, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
    worldrenderer.pos((f4 - f5), (1.0F - f6), 0.0D).tex(f1, f2).color(k1, l1, i2, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
    worldrenderer.pos((0.0F - f5), (1.0F - f6), 0.0D).tex(f, f2).color(k1, l1, i2, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
    tessellator.draw();
    GlStateManager.disableBlend();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    super.doRender(entity, x, y, z, entityYaw, partialTicks);
  }
  
  protected ResourceLocation getEntityTexture(EntityXPOrb entity) {
    return experienceOrbTextures;
  }
}
