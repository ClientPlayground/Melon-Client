package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.ResourceLocation;

public class RenderWitherSkull extends Render<EntityWitherSkull> {
  private static final ResourceLocation invulnerableWitherTextures = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
  
  private static final ResourceLocation witherTextures = new ResourceLocation("textures/entity/wither/wither.png");
  
  private final ModelSkeletonHead skeletonHeadModel = new ModelSkeletonHead();
  
  public RenderWitherSkull(RenderManager renderManagerIn) {
    super(renderManagerIn);
  }
  
  private float func_82400_a(float p_82400_1_, float p_82400_2_, float p_82400_3_) {
    float f;
    for (f = p_82400_2_ - p_82400_1_; f < -180.0F; f += 360.0F);
    while (f >= 180.0F)
      f -= 360.0F; 
    return p_82400_1_ + p_82400_3_ * f;
  }
  
  public void doRender(EntityWitherSkull entity, double x, double y, double z, float entityYaw, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.disableCull();
    float f = func_82400_a(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
    float f1 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
    GlStateManager.translate((float)x, (float)y, (float)z);
    float f2 = 0.0625F;
    GlStateManager.enableRescaleNormal();
    GlStateManager.scale(-1.0F, -1.0F, 1.0F);
    GlStateManager.enableAlpha();
    bindEntityTexture((Entity)entity);
    this.skeletonHeadModel.render((Entity)entity, 0.0F, 0.0F, 0.0F, f, f1, f2);
    GlStateManager.popMatrix();
    super.doRender((Entity)entity, x, y, z, entityYaw, partialTicks);
  }
  
  protected ResourceLocation getEntityTexture(EntityWitherSkull entity) {
    return entity.isInvulnerable() ? invulnerableWitherTextures : witherTextures;
  }
}
