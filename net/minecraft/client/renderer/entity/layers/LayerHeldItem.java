package net.minecraft.client.renderer.entity.layers;

import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LayerHeldItem implements LayerRenderer<EntityLivingBase> {
  private final RendererLivingEntity<?> livingEntityRenderer;
  
  public LayerHeldItem(RendererLivingEntity<?> livingEntityRendererIn) {
    this.livingEntityRenderer = livingEntityRendererIn;
  }
  
  public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
    ItemStack itemstack = entitylivingbaseIn.getHeldItem();
    if (itemstack != null) {
      GlStateManager.pushMatrix();
      if ((this.livingEntityRenderer.getMainModel()).isChild) {
        float f = 0.5F;
        GlStateManager.translate(0.0F, 0.625F, 0.0F);
        GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
        GlStateManager.scale(f, f, f);
      } 
      if (entitylivingbaseIn instanceof EntityPlayer) {
        if (IngameDisplay.OLD_ANIMATIONS.isEnabled() && IngameDisplay.OLD_ANIMATIONS_SWORD.isEnabled()) {
          if (((EntityPlayer)entitylivingbaseIn).isBlocking()) {
            ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
            GlStateManager.scale(1.05F, 1.05F, 1.05F);
            if (entitylivingbaseIn.isSneaking()) {
              GlStateManager.translate(-0.58F, 0.32F, -0.07F);
            } else {
              GlStateManager.translate(-0.45F, 0.25F, -0.07F);
            } 
            GlStateManager.rotate(-24405.0F, 137290.0F, -2009900.0F, -2654900.0F);
          } else {
            ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
          } 
        } else {
          ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
        } 
        GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
        if (((EntityPlayer)entitylivingbaseIn).fishEntity != null)
          itemstack = new ItemStack((Item)Items.fishing_rod, 0); 
      } else {
        ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
        GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
      } 
      Item item = itemstack.getItem();
      Minecraft minecraft = Minecraft.getMinecraft();
      if (item instanceof net.minecraft.item.ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
        GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f1 = 0.375F;
        GlStateManager.scale(-f1, -f1, f1);
      } 
      if (entitylivingbaseIn.isSneaking())
        GlStateManager.translate(0.0F, 0.203125F, 0.0F); 
      minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
      GlStateManager.popMatrix();
    } 
  }
  
  public boolean shouldCombineTextures() {
    return false;
  }
}
