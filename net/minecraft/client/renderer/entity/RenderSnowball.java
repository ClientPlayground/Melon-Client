package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderSnowball<T extends Entity> extends Render<T> {
  protected final Item field_177084_a;
  
  private final RenderItem field_177083_e;
  
  public RenderSnowball(RenderManager renderManagerIn, Item p_i46137_2_, RenderItem p_i46137_3_) {
    super(renderManagerIn);
    this.field_177084_a = p_i46137_2_;
    this.field_177083_e = p_i46137_3_;
  }
  
  public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)x, (float)y, (float)z);
    GlStateManager.enableRescaleNormal();
    GlStateManager.scale(0.5F, 0.5F, 0.5F);
    GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
    bindTexture(TextureMap.locationBlocksTexture);
    this.field_177083_e.renderItem(func_177082_d(entity), ItemCameraTransforms.TransformType.GROUND);
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    super.doRender(entity, x, y, z, entityYaw, partialTicks);
  }
  
  public ItemStack func_177082_d(T entityIn) {
    return new ItemStack(this.field_177084_a, 1, 0);
  }
  
  protected ResourceLocation getEntityTexture(Entity entity) {
    return TextureMap.locationBlocksTexture;
  }
}
