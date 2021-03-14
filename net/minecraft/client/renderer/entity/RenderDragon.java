package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonDeath;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonEyes;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderDragon extends RenderLiving<EntityDragon> {
  private static final ResourceLocation enderDragonCrystalBeamTextures = new ResourceLocation("textures/entity/endercrystal/endercrystal_beam.png");
  
  private static final ResourceLocation enderDragonExplodingTextures = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
  
  private static final ResourceLocation enderDragonTextures = new ResourceLocation("textures/entity/enderdragon/dragon.png");
  
  protected ModelDragon modelDragon;
  
  public RenderDragon(RenderManager renderManagerIn) {
    super(renderManagerIn, (ModelBase)new ModelDragon(0.0F), 0.5F);
    this.modelDragon = (ModelDragon)this.mainModel;
    addLayer(new LayerEnderDragonEyes(this));
    addLayer(new LayerEnderDragonDeath());
  }
  
  protected void rotateCorpse(EntityDragon bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
    float f = (float)bat.getMovementOffsets(7, partialTicks)[0];
    float f1 = (float)(bat.getMovementOffsets(5, partialTicks)[1] - bat.getMovementOffsets(10, partialTicks)[1]);
    GlStateManager.rotate(-f, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(f1 * 10.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.translate(0.0F, 0.0F, 1.0F);
    if (bat.deathTime > 0) {
      float f2 = (bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
      f2 = MathHelper.sqrt_float(f2);
      if (f2 > 1.0F)
        f2 = 1.0F; 
      GlStateManager.rotate(f2 * getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
    } 
  }
  
  protected void renderModel(EntityDragon entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor) {
    if (entitylivingbaseIn.deathTicks > 0) {
      float f = entitylivingbaseIn.deathTicks / 200.0F;
      GlStateManager.depthFunc(515);
      GlStateManager.enableAlpha();
      GlStateManager.alphaFunc(516, f);
      bindTexture(enderDragonExplodingTextures);
      this.mainModel.render((Entity)entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.depthFunc(514);
    } 
    bindEntityTexture(entitylivingbaseIn);
    this.mainModel.render((Entity)entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
    if (entitylivingbaseIn.hurtTime > 0) {
      GlStateManager.depthFunc(514);
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.color(1.0F, 0.0F, 0.0F, 0.5F);
      this.mainModel.render((Entity)entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.depthFunc(515);
    } 
  }
  
  public void doRender(EntityDragon entity, double x, double y, double z, float entityYaw, float partialTicks) {
    BossStatus.setBossStatus((IBossDisplayData)entity, false);
    super.doRender(entity, x, y, z, entityYaw, partialTicks);
    if (entity.healingEnderCrystal != null)
      drawRechargeRay(entity, x, y, z, partialTicks); 
  }
  
  protected void drawRechargeRay(EntityDragon dragon, double p_180574_2_, double p_180574_4_, double p_180574_6_, float p_180574_8_) {
    float f = dragon.healingEnderCrystal.innerRotation + p_180574_8_;
    float f1 = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
    f1 = (f1 * f1 + f1) * 0.2F;
    float f2 = (float)(dragon.healingEnderCrystal.posX - dragon.posX - (dragon.prevPosX - dragon.posX) * (1.0F - p_180574_8_));
    float f3 = (float)(f1 + dragon.healingEnderCrystal.posY - 1.0D - dragon.posY - (dragon.prevPosY - dragon.posY) * (1.0F - p_180574_8_));
    float f4 = (float)(dragon.healingEnderCrystal.posZ - dragon.posZ - (dragon.prevPosZ - dragon.posZ) * (1.0F - p_180574_8_));
    float f5 = MathHelper.sqrt_float(f2 * f2 + f4 * f4);
    float f6 = MathHelper.sqrt_float(f2 * f2 + f3 * f3 + f4 * f4);
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)p_180574_2_, (float)p_180574_4_ + 2.0F, (float)p_180574_6_);
    GlStateManager.rotate((float)-Math.atan2(f4, f2) * 180.0F / 3.1415927F - 90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate((float)-Math.atan2(f5, f3) * 180.0F / 3.1415927F - 90.0F, 1.0F, 0.0F, 0.0F);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableCull();
    bindTexture(enderDragonCrystalBeamTextures);
    GlStateManager.shadeModel(7425);
    float f7 = 0.0F - (dragon.ticksExisted + p_180574_8_) * 0.01F;
    float f8 = MathHelper.sqrt_float(f2 * f2 + f3 * f3 + f4 * f4) / 32.0F - (dragon.ticksExisted + p_180574_8_) * 0.01F;
    worldrenderer.begin(5, DefaultVertexFormats.POSITION_TEX_COLOR);
    int i = 8;
    for (int j = 0; j <= 8; j++) {
      float f9 = MathHelper.sin((j % 8) * 3.1415927F * 2.0F / 8.0F) * 0.75F;
      float f10 = MathHelper.cos((j % 8) * 3.1415927F * 2.0F / 8.0F) * 0.75F;
      float f11 = (j % 8) * 1.0F / 8.0F;
      worldrenderer.pos((f9 * 0.2F), (f10 * 0.2F), 0.0D).tex(f11, f8).color(0, 0, 0, 255).endVertex();
      worldrenderer.pos(f9, f10, f6).tex(f11, f7).color(255, 255, 255, 255).endVertex();
    } 
    tessellator.draw();
    GlStateManager.enableCull();
    GlStateManager.shadeModel(7424);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.popMatrix();
  }
  
  protected ResourceLocation getEntityTexture(EntityDragon entity) {
    return enderDragonTextures;
  }
}
