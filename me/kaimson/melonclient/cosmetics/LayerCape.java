package me.kaimson.melonclient.cosmetics;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class LayerCape implements LayerRenderer<EntityLivingBase> {
  private static Cape model;
  
  public LayerCape(RenderPlayer rp) {
    model = new Cape(rp);
  }
  
  public void doRenderLayer(EntityLivingBase entityIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
    AbstractClientPlayer entity = (AbstractClientPlayer)entityIn;
    if (entity.hasPlayerInfo() && !entity.isInvisible())
      model.render((Entity)entity, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, partialTicks, scale); 
  }
  
  public boolean shouldCombineTextures() {
    return false;
  }
}
