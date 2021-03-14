package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.util.ResourceLocation;

public class RenderGiantZombie extends RenderLiving<EntityGiantZombie> {
  private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
  
  private float scale;
  
  public RenderGiantZombie(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn, float scaleIn) {
    super(renderManagerIn, modelBaseIn, shadowSizeIn * scaleIn);
    this.scale = scaleIn;
    addLayer(new LayerHeldItem(this));
    addLayer(new LayerBipedArmor(this) {
          protected void initArmor() {
            this.modelLeggings = (ModelBase)new ModelZombie(0.5F, true);
            this.modelArmor = (ModelBase)new ModelZombie(1.0F, true);
          }
        });
  }
  
  public void transformHeldFull3DItemLayer() {
    GlStateManager.translate(0.0F, 0.1875F, 0.0F);
  }
  
  protected void preRenderCallback(EntityGiantZombie entitylivingbaseIn, float partialTickTime) {
    GlStateManager.scale(this.scale, this.scale, this.scale);
  }
  
  protected ResourceLocation getEntityTexture(EntityGiantZombie entity) {
    return zombieTextures;
  }
}
