package net.minecraft.client.renderer.entity.layers;

public interface LayerRenderer<E extends net.minecraft.entity.EntityLivingBase> {
  void doRenderLayer(E paramE, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7);
  
  boolean shouldCombineTextures();
}
