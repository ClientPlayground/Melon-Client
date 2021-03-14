package me.kaimson.melonclient.ingames.utils.itemphysics;

import java.util.Random;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ClientSide {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final Random random = new Random();
  
  public long tick;
  
  public void doRender(Entity entity, double x, double y, double z) {
    int i;
    float rotationSpeed = ((Float)IngameDisplay.ITEM_PHYSICS_ROTATION_SPEED.getOrDefault(Float.valueOf(1.0F))).floatValue();
    double rotation = (System.nanoTime() - this.tick) / 2500000.0D * rotationSpeed;
    if (!this.mc.inGameHasFocus)
      rotation = 0.0D; 
    EntityItem item = (EntityItem)entity;
    ItemStack itemStack = item.getEntityItem();
    if (itemStack != null && itemStack.getItem() != null) {
      i = Item.getIdFromItem(itemStack.getItem()) + itemStack.getMetadata();
    } else {
      i = 187;
    } 
    this.random.setSeed(i);
    this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
    GlStateManager.enableRescaleNormal();
    GlStateManager.alphaFunc(516, 0.1F);
    GlStateManager.enableBlend();
    RenderHelper.enableStandardItemLighting();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.pushMatrix();
    IBakedModel iBakedModel = this.mc.getRenderItem().getItemModelMesher().getItemModel(itemStack);
    boolean is3D = iBakedModel.isGui3d();
    int j = getModelCount(itemStack);
    GlStateManager.translate(x, y, z);
    if (is3D)
      GlStateManager.scale(0.5F, 0.5F, 0.5F); 
    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
    GL11.glRotatef(item.rotationYaw, 0.0F, 0.0F, 1.0F);
    if (is3D) {
      GlStateManager.translate(0.0D, 0.0D, -0.08D);
    } else {
      GlStateManager.translate(0.0D, 0.0D, -0.04D);
    } 
    if (is3D || (this.mc.getRenderManager()).options != null) {
      if (is3D) {
        if (!item.onGround)
          item.rotationPitch = (float)(item.rotationPitch + rotation * 2.0D); 
      } else if (!Double.isNaN(item.posX) && !Double.isNaN(item.posY) && !Double.isNaN(item.posZ) && item.worldObj != null) {
        if (item.onGround) {
          item.rotationPitch = 0.0F;
        } else {
          item.rotationPitch = (float)(item.rotationPitch + rotation * 2.0D);
        } 
      } 
      GlStateManager.rotate(item.rotationPitch, 1.0F, 0.0F, 0.0F);
    } 
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    for (int k = 0; k < j; k++) {
      if (is3D) {
        GlStateManager.pushMatrix();
        if (k > 0) {
          float f7 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
          float f9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
          float f6 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
          GlStateManager.translate(f7, f9, f6);
        } 
        this.mc.getRenderItem().renderItem(itemStack, iBakedModel);
        GlStateManager.popMatrix();
      } else {
        GlStateManager.pushMatrix();
        this.mc.getRenderItem().renderItem(itemStack, iBakedModel);
        GlStateManager.popMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.05375F);
      } 
    } 
    GlStateManager.popMatrix();
    GlStateManager.disableRescaleNormal();
    GlStateManager.disableBlend();
    this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
  }
  
  private int getModelCount(ItemStack stack) {
    int i = 1;
    if (stack.stackSize > 40) {
      i = 5;
    } else if (stack.stackSize > 32) {
      i = 4;
    } else if (stack.stackSize > 16) {
      i = 3;
    } else if (stack.stackSize > 1) {
      i = 2;
    } 
    return i;
  }
}
