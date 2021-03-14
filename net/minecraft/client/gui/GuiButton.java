package net.minecraft.client.gui;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButton extends Gui {
  protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
  
  public int width;
  
  public int height;
  
  public int xPosition;
  
  public int yPosition;
  
  public String displayString;
  
  public int id;
  
  public boolean enabled;
  
  public boolean visible;
  
  protected boolean hovered;
  
  public GuiButton(int buttonId, int x, int y, String buttonText) {
    this(buttonId, x, y, 200, 20, buttonText);
  }
  
  public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
    this.width = 200;
    this.height = 20;
    this.enabled = true;
    this.visible = true;
    this.id = buttonId;
    this.xPosition = x;
    this.yPosition = y;
    this.width = widthIn;
    this.height = heightIn;
    this.displayString = buttonText;
  }
  
  protected int getHoverState(boolean mouseOver) {
    int i = 1;
    if (!this.enabled) {
      i = 0;
    } else if (mouseOver) {
      i = 2;
    } 
    return i;
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      FontRenderer fontrenderer = mc.fontRendererObj;
      mc.getTextureManager().bindTexture(buttonTextures);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
      int i = getHoverState(this.hovered);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.blendFunc(770, 771);
      GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, this.enabled ? (this.hovered ? (new Color(255, 255, 255, 100))
          .getRGB() : (new Color(255, 255, 255, 75)).getRGB()) : (new Color(255, 255, 255, 40)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, 2.0F, this.enabled ? (this.hovered ? (new Color(255, 255, 255, 150))
          .getRGB() : (new Color(255, 255, 255, 125)).getRGB()) : (new Color(255, 255, 255, 100)).getRGB());
      mouseDragged(mc, mouseX, mouseY);
      int j = 14737632;
      if (!this.enabled) {
        j = 10526880;
      } else if (this.hovered) {
        j = 16777120;
      } 
      drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
    } 
  }
  
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {}
  
  public void mouseReleased(int mouseX, int mouseY) {}
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    return (this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
  }
  
  public boolean isMouseOver() {
    return this.hovered;
  }
  
  public void drawButtonForegroundLayer(int mouseX, int mouseY) {}
  
  public void playPressSound(SoundHandler soundHandlerIn) {
    soundHandlerIn.playSound((ISound)PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
  }
  
  public int getButtonWidth() {
    return this.width;
  }
  
  public void setWidth(int width) {
    this.width = width;
  }
}
