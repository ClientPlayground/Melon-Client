package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderBoat extends Render<EntityBoat> {
  private static final ResourceLocation boatTextures = new ResourceLocation("textures/entity/boat.png");
  
  protected ModelBase modelBoat = (ModelBase)new ModelBoat();
  
  public RenderBoat(RenderManager renderManagerIn) {
    super(renderManagerIn);
    this.shadowSize = 0.5F;
  }
  
  public void doRender(EntityBoat entity, double x, double y, double z, float entityYaw, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)x, (float)y + 0.25F, (float)z);
    GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
    float f = entity.getTimeSinceHit() - partialTicks;
    float f1 = entity.getDamageTaken() - partialTicks;
    if (f1 < 0.0F)
      f1 = 0.0F; 
    if (f > 0.0F)
      GlStateManager.rotate(MathHelper.sin(f) * f * f1 / 10.0F * entity.getForwardDirection(), 1.0F, 0.0F, 0.0F); 
    float f2 = 0.75F;
    GlStateManager.scale(f2, f2, f2);
    GlStateManager.scale(1.0F / f2, 1.0F / f2, 1.0F / f2);
    bindEntityTexture(entity);
    GlStateManager.scale(-1.0F, -1.0F, 1.0F);
    this.modelBoat.render((Entity)entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
    GlStateManager.popMatrix();
    super.doRender(entity, x, y, z, entityYaw, partialTicks);
  }
  
  protected ResourceLocation getEntityTexture(EntityBoat entity) {
    return boatTextures;
  }
}
