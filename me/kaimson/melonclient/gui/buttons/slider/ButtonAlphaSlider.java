package me.kaimson.melonclient.gui.buttons.slider;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.buttons.GuiSlider;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class ButtonAlphaSlider extends GuiSlider {
  private final IngameDisplay ingame;
  
  public IngameDisplay getIngame() {
    return this.ingame;
  }
  
  public ButtonAlphaSlider(int id, int x, int y, int width, int height, float min, float max, float step, float current, IngameDisplay ingame) {
    super(id, x, y, width, height, min, max, step, current, "Alpha: ");
    this.ingame = ingame;
    float value = (Client.config.getCustoms().get(ingame) != null) ? (new Color(((Integer)Client.config.getCustoms().get(ingame)).intValue(), true)).getAlpha() : current;
    this.sliderValue = MathUtil.normalizeValue(value, min, max, step);
    this.displayString = this.display + MathUtil.denormalizeValue(this.sliderValue, min, max, step);
  }
  
  public boolean isHovered() {
    return this.hovered;
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    if (super.mousePressed(mc, mouseX, mouseY)) {
      this.sliderValue = (mouseX - this.xPosition + 4) / (this.width - 8);
      this.sliderValue = MathHelper.clamp_float(this.sliderValue, this.min, this.max);
      this.displayString = this.display + getRoundedValue(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step));
      this.dragging = true;
      return super.mousePressed(mc, mouseX, mouseY);
    } 
    return super.mousePressed(mc, mouseX, mouseY);
  }
  
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible && 
      this.dragging) {
      this.sliderValue = (mouseX - this.xPosition + 4) / (this.width - 8);
      this.sliderValue = MathHelper.clamp_float(this.sliderValue, this.min, this.max);
      this.sliderValue = MathUtil.normalizeValue(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step), this.min, this.max, this.step);
      this.displayString = this.display + getRoundedValue(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step));
    } 
  }
  
  public void mouseReleased(int mouseX, int mouseY) {
    super.mouseReleased(mouseX, mouseY);
    Color c = new Color(((Integer)Client.config.getCustoms().get(this.ingame)).intValue());
    float alpha = MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step);
    Color newColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)alpha);
    Client.config.getCustoms().put(this.ingame, Integer.valueOf(newColor.getRGB()));
    Client.config.saveConfig();
  }
}
