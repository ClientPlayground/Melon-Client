package net.minecraft.client.renderer.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;

public class RenderPlayer extends RendererLivingEntity<AbstractClientPlayer> {
  private boolean smallArms;
  
  public RenderPlayer(RenderManager renderManager) {
    this(renderManager, false);
  }
  
  public RenderPlayer(RenderManager renderManager, boolean useSmallArms) {
    super(renderManager, (ModelBase)new ModelPlayer(0.0F, useSmallArms), 0.5F);
    this.smallArms = useSmallArms;
    addLayer(new LayerBipedArmor(this));
    addLayer(new LayerHeldItem(this));
    addLayer(new LayerArrow(this));
    addLayer(new LayerDeadmau5Head(this));
    addLayer(new LayerCape(this));
    addLayer(new LayerCustomHead((getMainModel()).bipedHead));
  }
  
  public ModelPlayer getMainModel() {
    return (ModelPlayer)super.getMainModel();
  }
  
  public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
    if (!entity.isUser() || this.renderManager.livingPlayer == entity) {
      double d0 = y;
      if (entity.isSneaking() && !(entity instanceof net.minecraft.client.entity.EntityPlayerSP))
        d0 = y - 0.125D; 
      setModelVisibilities(entity);
      super.doRender(entity, x, d0, z, entityYaw, partialTicks);
    } 
  }
  
  private void setModelVisibilities(AbstractClientPlayer clientPlayer) {
    ModelPlayer modelplayer = getMainModel();
    if (clientPlayer.isSpectator()) {
      modelplayer.setInvisible(false);
      modelplayer.bipedHead.showModel = true;
      modelplayer.bipedHeadwear.showModel = true;
    } else {
      ItemStack itemstack = clientPlayer.inventory.getCurrentItem();
      modelplayer.setInvisible(true);
      modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
      modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
      modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
      modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
      modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
      modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
      modelplayer.heldItemLeft = 0;
      modelplayer.aimedBow = false;
      modelplayer.isSneak = clientPlayer.isSneaking();
      if (itemstack == null) {
        modelplayer.heldItemRight = 0;
      } else {
        modelplayer.heldItemRight = 1;
        if (clientPlayer.getItemInUseCount() > 0) {
          EnumAction enumaction = itemstack.getItemUseAction();
          if (enumaction == EnumAction.BLOCK) {
            modelplayer.heldItemRight = 3;
          } else if (enumaction == EnumAction.BOW) {
            modelplayer.aimedBow = true;
          } 
        } 
      } 
    } 
  }
  
  protected ResourceLocation getEntityTexture(AbstractClientPlayer entity) {
    return entity.getLocationSkin();
  }
  
  public void transformHeldFull3DItemLayer() {
    GlStateManager.translate(0.0F, 0.1875F, 0.0F);
  }
  
  protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime) {
    float f = 0.9375F;
    GlStateManager.scale(f, f, f);
  }
  
  protected void renderOffsetLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
    if (p_177069_10_ < 100.0D) {
      Scoreboard scoreboard = entityIn.getWorldScoreboard();
      ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);
      if (scoreobjective != null) {
        Score score = scoreboard.getValueFromObjective(entityIn.getCommandSenderName(), scoreobjective);
        renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
        y += ((getFontRendererFromRenderManager()).FONT_HEIGHT * 1.15F * p_177069_9_);
      } 
    } 
    super.renderOffsetLivingLabel(entityIn, x, y, z, str, p_177069_9_, p_177069_10_);
  }
  
  public void renderRightArm(AbstractClientPlayer clientPlayer) {
    float f = 1.0F;
    GlStateManager.color(f, f, f);
    ModelPlayer modelplayer = getMainModel();
    setModelVisibilities(clientPlayer);
    modelplayer.swingProgress = 0.0F;
    modelplayer.isSneak = false;
    modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, (Entity)clientPlayer);
    modelplayer.renderRightArm();
  }
  
  public void renderLeftArm(AbstractClientPlayer clientPlayer) {
    float f = 1.0F;
    GlStateManager.color(f, f, f);
    ModelPlayer modelplayer = getMainModel();
    setModelVisibilities(clientPlayer);
    modelplayer.isSneak = false;
    modelplayer.swingProgress = 0.0F;
    modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, (Entity)clientPlayer);
    modelplayer.renderLeftArm();
  }
  
  protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z) {
    if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping()) {
      super.renderLivingAt(entityLivingBaseIn, x + entityLivingBaseIn.renderOffsetX, y + entityLivingBaseIn.renderOffsetY, z + entityLivingBaseIn.renderOffsetZ);
    } else {
      super.renderLivingAt(entityLivingBaseIn, x, y, z);
    } 
  }
  
  protected void rotateCorpse(AbstractClientPlayer bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
    if (bat.isEntityAlive() && bat.isPlayerSleeping()) {
      GlStateManager.rotate(bat.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
    } else {
      super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
    } 
  }
}
