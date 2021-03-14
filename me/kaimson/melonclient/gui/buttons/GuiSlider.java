package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import java.math.BigDecimal;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class GuiSlider extends GuiButton {
  protected float min;
  
  protected float max;
  
  protected float step;
  
  protected float current;
  
  public float sliderValue;
  
  protected boolean dragging;
  
  public String display;
  
  public boolean hovering;
  
  public GuiSlider(int buttonID, double x, double y, int width, int height, float min, float max, float step, float current, String display) {
    super(buttonID, (int)x, (int)y, width, height, "");
    this.sliderValue = current;
    this.min = min;
    this.max = max;
    this.step = step;
    this.current = current;
    this.display = "";
    this.displayString = String.valueOf(current);
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
  
  public void render(Minecraft mc, int yPosition, int mouseX, int mouseY) {
    this.hovered = (mouseX >= this.xPosition && mouseY >= yPosition && mouseX < this.xPosition + this.width && mouseY < yPosition + this.height);
    this.hovering = this.hovered;
    float scale = 0.7F;
    GuiUtils.drawRoundedRect(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 2.1F, (new Color(120, 120, 120, 50)).getRGB());
    GuiUtils.drawRoundedRect(this.xPosition, yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 4, yPosition + this.height, 2.1F, (new Color(0, 200, 225, 255)).getRGB());
    mouseDragged(mc, mouseX, mouseY);
    if (mouseX >= this.xPosition + (int)(this.sliderValue * (this.width - 8) + 1.0F) && mouseX <= this.xPosition + (int)(this.sliderValue * (this.width - 8) + 8.0F) && this.hovered)
      GuiUtils.drawPartialCircle((this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 4), yPosition + this.height / 2.0F, 3.2F, 0, 360, 1.0F, new Color(255, 255, 255, 255), true); 
    GuiUtils.drawPartialCircle((this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 4), yPosition + this.height / 2.0F, 1.6F, 0, 360, 8.0F, new Color(0, 240, 255, 255), true);
    GlStateManager.scale(scale, scale, 1.0F);
    GuiUtils.drawCenteredString(this.displayString, (int)((this.xPosition + this.width / 2) / scale), (int)((yPosition - (this.height - 8) / 2 - 7) / scale), 14737632, false);
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
  }
  
  protected int getHoverState(boolean mouseOver) {
    return 0;
  }
  
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      if (this.dragging) {
        this.sliderValue = (mouseX - this.xPosition + 4) / (this.width - 8);
        this.sliderValue = MathHelper.clamp_float(this.sliderValue, this.min, this.max);
        float scaleValue = MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step);
        this.sliderValue = MathUtil.normalizeValue(scaleValue, this.min, this.max, this.step);
        this.displayString = String.valueOf(getRoundedValue(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step)));
      } 
      mc.getTextureManager().bindTexture(buttonTextures);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      drawRect(this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 1, this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 7, this.yPosition + 20, 16777215);
    } 
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (super.mousePressed(mc, mouseX, mouseY)) {
      this.sliderValue = (mouseX - this.xPosition + 4) / (this.width - 8);
      this.sliderValue = MathHelper.clamp_float(this.sliderValue, this.min, this.max);
      this.displayString = String.valueOf(getRoundedValue(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step)));
      this.dragging = true;
      return true;
    } 
    return false;
  }
  
  public void mouseReleased(int mouseX, int mouseY) {
    this.dragging = false;
  }
  
  protected float getRoundedValue(float value) {
    return (new BigDecimal(String.valueOf(value))).setScale(2, 4).floatValue();
  }
  
  public float getSliderValue() {
    return getRoundedValue(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step));
  }
}
