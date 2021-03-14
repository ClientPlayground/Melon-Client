package net.minecraft.client.renderer;

import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.optifine.DynamicLights;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class ItemRenderer {
  private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
  
  private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
  
  private final Minecraft mc;
  
  private ItemStack itemToRender;
  
  private float equippedProgress;
  
  private float prevEquippedProgress;
  
  private final RenderManager renderManager;
  
  private final RenderItem itemRenderer;
  
  private int equippedItemSlot = -1;
  
  public ItemRenderer(Minecraft mcIn) {
    this.mc = mcIn;
    this.renderManager = mcIn.getRenderManager();
    this.itemRenderer = mcIn.getRenderItem();
  }
  
  public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {
    if (heldStack != null) {
      Item item = heldStack.getItem();
      Block block = Block.getBlockFromItem(item);
      GlStateManager.pushMatrix();
      if (this.itemRenderer.shouldRenderItemIn3D(heldStack)) {
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        if (isBlockTranslucent(block) && (!Config.isShaders() || !Shaders.renderItemKeepDepthMask))
          GlStateManager.depthMask(false); 
      } 
      this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);
      if (isBlockTranslucent(block))
        GlStateManager.depthMask(true); 
      GlStateManager.popMatrix();
    } 
  }
  
  private boolean isBlockTranslucent(Block blockIn) {
    return (blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT);
  }
  
  private void rotateArroundXAndY(float angle, float angleY) {
    GlStateManager.pushMatrix();
    GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(angleY, 0.0F, 1.0F, 0.0F);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.popMatrix();
  }
  
  private void setLightMapFromPlayer(AbstractClientPlayer clientPlayer) {
    int i = this.mc.theWorld.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);
    if (Config.isDynamicLights())
      i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i); 
    float f = (i & 0xFFFF);
    float f1 = (i >> 16);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
  }
  
  private void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks) {
    float f = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
    float f1 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
    GlStateManager.rotate((entityplayerspIn.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate((entityplayerspIn.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
  }
  
  private float getMapAngleFromPitch(float pitch) {
    float f = 1.0F - pitch / 45.0F + 0.1F;
    f = MathHelper.clamp_float(f, 0.0F, 1.0F);
    f = -MathHelper.cos(f * 3.1415927F) * 0.5F + 0.5F;
    return f;
  }
  
  private void renderRightArm(RenderPlayer renderPlayerIn) {
    GlStateManager.pushMatrix();
    GlStateManager.rotate(54.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(64.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.translate(0.25F, -0.85F, 0.75F);
    renderPlayerIn.renderRightArm((AbstractClientPlayer)this.mc.thePlayer);
    GlStateManager.popMatrix();
  }
  
  private void renderLeftArm(RenderPlayer renderPlayerIn) {
    GlStateManager.pushMatrix();
    GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.translate(-0.3F, -1.1F, 0.45F);
    renderPlayerIn.renderLeftArm((AbstractClientPlayer)this.mc.thePlayer);
    GlStateManager.popMatrix();
  }
  
  private void renderPlayerArms(AbstractClientPlayer clientPlayer) {
    this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
    Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject((Entity)this.mc.thePlayer);
    RenderPlayer renderplayer = (RenderPlayer)render;
    if (!clientPlayer.isInvisible()) {
      GlStateManager.disableCull();
      renderRightArm(renderplayer);
      renderLeftArm(renderplayer);
      GlStateManager.enableCull();
    } 
  }
  
  private void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress) {
    float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
    float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
    float f2 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
    GlStateManager.translate(f, f1, f2);
    float f3 = getMapAngleFromPitch(pitch);
    GlStateManager.translate(0.0F, 0.04F, -0.72F);
    GlStateManager.translate(0.0F, equipmentProgress * -1.2F, 0.0F);
    GlStateManager.translate(0.0F, f3 * -0.5F, 0.0F);
    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(f3 * -85.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
    renderPlayerArms(clientPlayer);
    float f4 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
    float f5 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
    GlStateManager.rotate(f4 * -20.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(f5 * -20.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(f5 * -80.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.scale(0.38F, 0.38F, 0.38F);
    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.translate(-1.0F, -1.0F, 0.0F);
    GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
    this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    GL11.glNormal3f(0.0F, 0.0F, -1.0F);
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
    worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
    worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
    worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
    tessellator.draw();
    MapData mapdata = Items.filled_map.getMapData(this.itemToRender, (World)this.mc.theWorld);
    if (mapdata != null)
      this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false); 
  }
  
  private void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress) {
    float f = -0.3F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
    float f1 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
    float f2 = -0.4F * MathHelper.sin(swingProgress * 3.1415927F);
    GlStateManager.translate(f, f1, f2);
    GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
    GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
    float f3 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
    float f4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
    GlStateManager.rotate(f4 * 70.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
    GlStateManager.translate(-1.0F, 3.6F, 3.5F);
    GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.scale(1.0F, 1.0F, 1.0F);
    GlStateManager.translate(5.6F, 0.0F, 0.0F);
    Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject((Entity)this.mc.thePlayer);
    GlStateManager.disableCull();
    RenderPlayer renderplayer = (RenderPlayer)render;
    renderplayer.renderRightArm((AbstractClientPlayer)this.mc.thePlayer);
    GlStateManager.enableCull();
  }
  
  private void doItemUsedTransformations(float swingProgress) {
    float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
    float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
    float f2 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
    GlStateManager.translate(f, f1, f2);
  }
  
  private void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks) {
    float f = clientPlayer.getItemInUseCount() - partialTicks + 1.0F;
    float f1 = f / this.itemToRender.getMaxItemUseDuration();
    float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.1F);
    if (f1 >= 0.8F)
      f2 = 0.0F; 
    GlStateManager.translate(0.0F, f2, 0.0F);
    float f3 = 1.0F - (float)Math.pow(f1, 27.0D);
    GlStateManager.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
    GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
  }
  
  private void transformFirstPersonItem(float equipProgress, float swingProgress) {
    if (IngameDisplay.OLD_ANIMATIONS.isEnabled()) {
      if (IngameDisplay.OLD_ANIMATIONS_BOW.isEnabled() && 
        this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.getItemInUse() != null && this.mc.thePlayer.getItemInUse().getItem() != null && Item.getIdFromItem(this.mc.thePlayer.getItemInUse().getItem()) == 261)
        GlStateManager.translate(-0.01F, 0.05F, -0.06F); 
      if (IngameDisplay.OLD_ANIMATIONS_ROD.isEnabled() && 
        this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.getCurrentEquippedItem() != null && this.mc.thePlayer.getCurrentEquippedItem().getItem() != null && (
        Item.getIdFromItem(this.mc.thePlayer.getCurrentEquippedItem().getItem()) == 346 || Item.getIdFromItem(this.mc.thePlayer.getCurrentEquippedItem().getItem()) == 398)) {
        GlStateManager.translate(0.08F, -0.027F, -0.33F);
        GlStateManager.scale(0.93F, 1.0F, 1.0F);
      } 
      if (IngameDisplay.OLD_ANIMATIONS_SWING.isEnabled() && 
        this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.isSwingInProgress && this.mc.thePlayer.getCurrentEquippedItem() != null && !this.mc.thePlayer.isEating() && !this.mc.thePlayer.isBlocking()) {
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
        GlStateManager.translate(-0.078F, 0.003F, 0.05F);
      } 
    } 
    GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
    GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
    float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
    float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
    GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.scale(0.4F, 0.4F, 0.4F);
  }
  
  private void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer) {
    GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
    GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.translate(-0.9F, 0.2F, 0.0F);
    float f = this.itemToRender.getMaxItemUseDuration() - clientPlayer.getItemInUseCount() - partialTicks + 1.0F;
    float f1 = f / 20.0F;
    f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
    if (f1 > 1.0F)
      f1 = 1.0F; 
    if (f1 > 0.1F) {
      float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
      float f3 = f1 - 0.1F;
      float f4 = f2 * f3;
      GlStateManager.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
    } 
    GlStateManager.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
    GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
  }
  
  private void doBlockTransformations() {
    GlStateManager.translate(-0.5F, 0.2F, 0.0F);
    GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
  }
  
  public void renderItemInFirstPerson(float partialTicks) {
    if (!Config.isShaders() || !Shaders.isSkipRenderHand()) {
      float f = 1.0F - this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks;
      EntityPlayerSP entityPlayerSP = this.mc.thePlayer;
      float f1 = entityPlayerSP.getSwingProgress(partialTicks);
      float f2 = ((AbstractClientPlayer)entityPlayerSP).prevRotationPitch + (((AbstractClientPlayer)entityPlayerSP).rotationPitch - ((AbstractClientPlayer)entityPlayerSP).prevRotationPitch) * partialTicks;
      float f3 = ((AbstractClientPlayer)entityPlayerSP).prevRotationYaw + (((AbstractClientPlayer)entityPlayerSP).rotationYaw - ((AbstractClientPlayer)entityPlayerSP).prevRotationYaw) * partialTicks;
      rotateArroundXAndY(f2, f3);
      setLightMapFromPlayer((AbstractClientPlayer)entityPlayerSP);
      rotateWithPlayerRotations(entityPlayerSP, partialTicks);
      GlStateManager.enableRescaleNormal();
      GlStateManager.pushMatrix();
      if (this.itemToRender != null) {
        if (this.itemToRender.getItem() instanceof net.minecraft.item.ItemMap) {
          renderItemMap((AbstractClientPlayer)entityPlayerSP, f2, f, f1);
        } else if (entityPlayerSP.getItemInUseCount() > 0) {
          EnumAction enumaction = this.itemToRender.getItemUseAction();
          float var10 = 0.0F;
          if (IngameDisplay.OLD_ANIMATIONS.isEnabled() && IngameDisplay.OLD_ANIMATIONS_BLOCKHIT.isEnabled())
            var10 = f1; 
          switch (enumaction) {
            case NONE:
              transformFirstPersonItem(f, 0.0F);
              break;
            case EAT:
            case DRINK:
              performDrinking((AbstractClientPlayer)entityPlayerSP, partialTicks);
              transformFirstPersonItem(f, var10);
              if (IngameDisplay.OLD_ANIMATIONS.isEnabled() && 
                IngameDisplay.OLD_ANIMATIONS_EAT.isEnabled()) {
                GlStateManager.scale(0.8F, 1.0F, 1.0F);
                GL11.glTranslatef(-0.2F, -0.1F, 0.0F);
              } 
              break;
            case BLOCK:
              transformFirstPersonItem(f, var10);
              doBlockTransformations();
              if (IngameDisplay.OLD_ANIMATIONS.isEnabled() && 
                IngameDisplay.OLD_ANIMATIONS_BLOCKHIT.isEnabled()) {
                GlStateManager.scale(0.83F, 0.88F, 0.85F);
                GL11.glTranslatef(-0.3F, 0.1F, 0.0F);
              } 
              break;
            case BOW:
              transformFirstPersonItem(f, var10);
              doBowTransformations(partialTicks, (AbstractClientPlayer)entityPlayerSP);
              break;
          } 
        } else {
          doItemUsedTransformations(f1);
          transformFirstPersonItem(f, f1);
        } 
        renderItem((EntityLivingBase)entityPlayerSP, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
      } else if (!entityPlayerSP.isInvisible()) {
        renderPlayerArm((AbstractClientPlayer)entityPlayerSP, f, f1);
      } 
      GlStateManager.popMatrix();
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
    } 
  }
  
  public void renderOverlays(float partialTicks) {
    GlStateManager.disableAlpha();
    if (this.mc.thePlayer.isEntityInsideOpaqueBlock()) {
      IBlockState iblockstate = this.mc.theWorld.getBlockState(new BlockPos((Entity)this.mc.thePlayer));
      BlockPos blockpos = new BlockPos((Entity)this.mc.thePlayer);
      EntityPlayerSP entityPlayerSP = this.mc.thePlayer;
      for (int i = 0; i < 8; i++) {
        double d0 = ((EntityPlayer)entityPlayerSP).posX + ((((i >> 0) % 2) - 0.5F) * ((EntityPlayer)entityPlayerSP).width * 0.8F);
        double d1 = ((EntityPlayer)entityPlayerSP).posY + ((((i >> 1) % 2) - 0.5F) * 0.1F);
        double d2 = ((EntityPlayer)entityPlayerSP).posZ + ((((i >> 2) % 2) - 0.5F) * ((EntityPlayer)entityPlayerSP).width * 0.8F);
        BlockPos blockpos1 = new BlockPos(d0, d1 + entityPlayerSP.getEyeHeight(), d2);
        IBlockState iblockstate1 = this.mc.theWorld.getBlockState(blockpos1);
        if (iblockstate1.getBlock().isVisuallyOpaque()) {
          iblockstate = iblockstate1;
          blockpos = blockpos1;
        } 
      } 
      if (iblockstate.getBlock().getRenderType() != -1) {
        Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);
        if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, new Object[] { this.mc.thePlayer, Float.valueOf(partialTicks), object, iblockstate, blockpos }))
          renderBlockInHand(partialTicks, this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate)); 
      } 
    } 
    if (!this.mc.thePlayer.isSpectator()) {
      if (this.mc.thePlayer.isInsideOfMaterial(Material.water) && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderWaterOverlay, new Object[] { this.mc.thePlayer, Float.valueOf(partialTicks) }))
        renderWaterOverlayTexture(partialTicks); 
      if (this.mc.thePlayer.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, new Object[] { this.mc.thePlayer, Float.valueOf(partialTicks) }))
        renderFireInFirstPerson(partialTicks); 
    } 
    GlStateManager.enableAlpha();
  }
  
  private void renderBlockInHand(float partialTicks, TextureAtlasSprite atlas) {
    this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    float f = 0.1F;
    GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
    GlStateManager.pushMatrix();
    float f1 = -1.0F;
    float f2 = 1.0F;
    float f3 = -1.0F;
    float f4 = 1.0F;
    float f5 = -0.5F;
    float f6 = atlas.getMinU();
    float f7 = atlas.getMaxU();
    float f8 = atlas.getMinV();
    float f9 = atlas.getMaxV();
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex(f7, f9).endVertex();
    worldrenderer.pos(1.0D, -1.0D, -0.5D).tex(f6, f9).endVertex();
    worldrenderer.pos(1.0D, 1.0D, -0.5D).tex(f6, f8).endVertex();
    worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex(f7, f8).endVertex();
    tessellator.draw();
    GlStateManager.popMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
  }
  
  private void renderWaterOverlayTexture(float partialTicks) {
    if (!Config.isShaders() || Shaders.isUnderwaterOverlay()) {
      this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      float f = this.mc.thePlayer.getBrightness(partialTicks);
      GlStateManager.color(f, f, f, 0.5F);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.pushMatrix();
      float f1 = 4.0F;
      float f2 = -1.0F;
      float f3 = 1.0F;
      float f4 = -1.0F;
      float f5 = 1.0F;
      float f6 = -0.5F;
      float f7 = -this.mc.thePlayer.rotationYaw / 64.0F;
      float f8 = this.mc.thePlayer.rotationPitch / 64.0F;
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex((4.0F + f7), (4.0F + f8)).endVertex();
      worldrenderer.pos(1.0D, -1.0D, -0.5D).tex((0.0F + f7), (4.0F + f8)).endVertex();
      worldrenderer.pos(1.0D, 1.0D, -0.5D).tex((0.0F + f7), (0.0F + f8)).endVertex();
      worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex((4.0F + f7), (0.0F + f8)).endVertex();
      tessellator.draw();
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
    } 
  }
  
  private void renderFireInFirstPerson(float partialTicks) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
    GlStateManager.depthFunc(519);
    GlStateManager.depthMask(false);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    float f = 1.0F;
    for (int i = 0; i < 2; i++) {
      GlStateManager.pushMatrix();
      TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
      this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
      float f1 = textureatlassprite.getMinU();
      float f2 = textureatlassprite.getMaxU();
      float f3 = textureatlassprite.getMinV();
      float f4 = textureatlassprite.getMaxV();
      float f5 = (0.0F - f) / 2.0F;
      float f6 = f5 + f;
      float f7 = 0.0F - f / 2.0F;
      float f8 = f7 + f;
      float f9 = -0.5F;
      GlStateManager.translate(-(i * 2 - 1) * 0.24F, -0.3F, 0.0F);
      GlStateManager.rotate((i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.setSprite(textureatlassprite);
      worldrenderer.pos(f5, f7, f9).tex(f2, f4).endVertex();
      worldrenderer.pos(f6, f7, f9).tex(f1, f4).endVertex();
      worldrenderer.pos(f6, f8, f9).tex(f1, f3).endVertex();
      worldrenderer.pos(f5, f8, f9).tex(f2, f3).endVertex();
      tessellator.draw();
      GlStateManager.popMatrix();
    } 
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.disableBlend();
    GlStateManager.depthMask(true);
    GlStateManager.depthFunc(515);
  }
  
  public void updateEquippedItem() {
    this.prevEquippedProgress = this.equippedProgress;
    EntityPlayerSP entityPlayerSP = this.mc.thePlayer;
    ItemStack itemstack = ((EntityPlayer)entityPlayerSP).inventory.getCurrentItem();
    boolean flag = false;
    if (this.itemToRender != null && itemstack != null) {
      if (!this.itemToRender.getIsItemStackEqual(itemstack)) {
        if (Reflector.ForgeItem_shouldCauseReequipAnimation.exists()) {
          boolean flag1 = Reflector.callBoolean(this.itemToRender.getItem(), Reflector.ForgeItem_shouldCauseReequipAnimation, new Object[] { this.itemToRender, itemstack, Boolean.valueOf((this.equippedItemSlot != ((EntityPlayer)entityPlayerSP).inventory.currentItem)) });
          if (!flag1) {
            this.itemToRender = itemstack;
            this.equippedItemSlot = ((EntityPlayer)entityPlayerSP).inventory.currentItem;
            return;
          } 
        } 
        flag = true;
      } 
    } else if (this.itemToRender == null && itemstack == null) {
      flag = false;
    } else {
      flag = true;
    } 
    float f2 = 0.4F;
    float f = flag ? 0.0F : 1.0F;
    float f1 = MathHelper.clamp_float(f - this.equippedProgress, -f2, f2);
    this.equippedProgress += f1;
    if (this.equippedProgress < 0.1F) {
      this.itemToRender = itemstack;
      this.equippedItemSlot = ((EntityPlayer)entityPlayerSP).inventory.currentItem;
      if (Config.isShaders())
        Shaders.setItemToRenderMain(itemstack); 
    } 
  }
  
  public void resetEquippedProgress() {
    this.equippedProgress = 0.0F;
  }
  
  public void resetEquippedProgress2() {
    this.equippedProgress = 0.0F;
  }
}
