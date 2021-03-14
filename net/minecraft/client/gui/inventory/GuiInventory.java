package net.minecraft.client.gui.inventory;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class GuiInventory extends InventoryEffectRenderer {
  private float oldMouseX;
  
  private float oldMouseY;
  
  public GuiInventory(EntityPlayer p_i1094_1_) {
    super(p_i1094_1_.inventoryContainer);
    this.allowUserInput = true;
  }
  
  public void updateScreen() {
    if (this.mc.playerController.isInCreativeMode())
      this.mc.displayGuiScreen((GuiScreen)new GuiContainerCreative((EntityPlayer)this.mc.thePlayer)); 
    updateActivePotionEffects();
  }
  
  public void initGui() {
    this.buttonList.clear();
    if (this.mc.playerController.isInCreativeMode()) {
      this.mc.displayGuiScreen((GuiScreen)new GuiContainerCreative((EntityPlayer)this.mc.thePlayer));
    } else {
      super.initGui();
    } 
  }
  
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 86, 16, 4210752);
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    this.oldMouseX = mouseX;
    this.oldMouseY = mouseY;
  }
  
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(inventoryBackground);
    int i = this.guiLeft;
    int j = this.guiTop;
    drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    drawEntityOnScreen(i + 51, j + 75, 30, (i + 51) - this.oldMouseX, (j + 75 - 50) - this.oldMouseY, (EntityLivingBase)this.mc.thePlayer);
  }
  
  public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
    GlStateManager.enableColorMaterial();
    GlStateManager.pushMatrix();
    GlStateManager.translate(posX, posY, 50.0F);
    GlStateManager.scale(-scale, scale, scale);
    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
    float f = ent.renderYawOffset;
    float f1 = ent.rotationYaw;
    float f2 = ent.rotationPitch;
    float f3 = ent.prevRotationYawHead;
    float f4 = ent.rotationYawHead;
    GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-((float)Math.atan((mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
    ent.renderYawOffset = (float)Math.atan((mouseX / 40.0F)) * 20.0F;
    ent.rotationYaw = (float)Math.atan((mouseX / 40.0F)) * 40.0F;
    ent.rotationPitch = -((float)Math.atan((mouseY / 40.0F))) * 20.0F;
    ent.rotationYawHead = ent.rotationYaw;
    ent.prevRotationYawHead = ent.rotationYaw;
    GlStateManager.translate(0.0F, 0.0F, 0.0F);
    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
    rendermanager.setPlayerViewY(180.0F);
    rendermanager.setRenderShadow(false);
    rendermanager.renderEntityWithPosYaw((Entity)ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
    rendermanager.setRenderShadow(true);
    ent.renderYawOffset = f;
    ent.rotationYaw = f1;
    ent.rotationPitch = f2;
    ent.prevRotationYawHead = f3;
    ent.rotationYawHead = f4;
    GlStateManager.popMatrix();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();
    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
    GlStateManager.disableTexture2D();
    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button.id == 0)
      this.mc.displayGuiScreen((GuiScreen)new GuiAchievements((GuiScreen)this, this.mc.thePlayer.getStatFileWriter())); 
    if (button.id == 1)
      this.mc.displayGuiScreen((GuiScreen)new GuiStats((GuiScreen)this, this.mc.thePlayer.getStatFileWriter())); 
  }
}
