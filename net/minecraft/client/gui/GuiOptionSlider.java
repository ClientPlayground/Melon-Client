package net.minecraft.client.gui;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;

public class GuiOptionSlider extends GuiButton {
  private float sliderValue;
  
  public boolean dragging;
  
  private GameSettings.Options options;
  
  private final float field_146132_r;
  
  private final float field_146131_s;
  
  public GuiOptionSlider(int p_i45016_1_, int p_i45016_2_, int p_i45016_3_, GameSettings.Options p_i45016_4_) {
    this(p_i45016_1_, p_i45016_2_, p_i45016_3_, p_i45016_4_, 0.0F, 1.0F);
  }
  
  public GuiOptionSlider(int p_i45017_1_, int p_i45017_2_, int p_i45017_3_, GameSettings.Options p_i45017_4_, float p_i45017_5_, float p_i45017_6_) {
    super(p_i45017_1_, p_i45017_2_, p_i45017_3_, 150, 20, "");
    this.sliderValue = 1.0F;
    this.options = p_i45017_4_;
    this.field_146132_r = p_i45017_5_;
    this.field_146131_s = p_i45017_6_;
    Minecraft minecraft = Minecraft.getMinecraft();
    this.sliderValue = p_i45017_4_.normalizeValue(minecraft.gameSettings.getOptionFloatValue(p_i45017_4_));
    this.displayString = minecraft.gameSettings.getKeyBinding(p_i45017_4_);
  }
  
  protected int getHoverState(boolean mouseOver) {
    return 0;
  }
  
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      if (this.dragging) {
        this.sliderValue = (mouseX - this.xPosition + 4) / (this.width - 8);
        this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
        float f = this.options.denormalizeValue(this.sliderValue);
        mc.gameSettings.setOptionFloatValue(this.options, f);
        this.sliderValue = this.options.normalizeValue(f);
        this.displayString = mc.gameSettings.getKeyBinding(this.options);
      } 
      mc.getTextureManager().bindTexture(buttonTextures);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 8, this.yPosition + this.height, 2.0F, 2.0F, (new Color(205, 205, 205, 100)).getRGB());
      GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 8, this.yPosition + this.height, 2.0F, (new Color(255, 255, 255, 50)).getRGB());
      GuiUtils.drawRoundedRect(this.xPosition + (int)(this.sliderValue * (this.width - 8)), this.yPosition, this.xPosition + (int)(this.sliderValue * (this.width - 8)) + 8, this.yPosition + this.height, 2.0F, (new Color(255, 255, 255, 100))
          .getRGB());
    } 
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (super.mousePressed(mc, mouseX, mouseY)) {
      this.sliderValue = (mouseX - this.xPosition + 4) / (this.width - 8);
      this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
      mc.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
      this.displayString = mc.gameSettings.getKeyBinding(this.options);
      this.dragging = true;
      return true;
    } 
    return false;
  }
  
  public void mouseReleased(int mouseX, int mouseY) {
    this.dragging = false;
  }
}
