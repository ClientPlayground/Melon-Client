package me.kaimson.melonclient.gui.buttons.slider;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.buttons.GuiSlider;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class ButtonSlider extends GuiSlider {
  private IngameDisplay ingame;
  
  public IngameDisplay getIngame() {
    return this.ingame;
  }
  
  public ButtonSlider(int id, int x, int y, int width, int height, float min, float max, float step, float current, IngameDisplay ingame) {
    super(id, x, y, width, height, min, max, step, current, "");
    this.ingame = ingame;
    this.sliderValue = MathUtil.normalizeValue(((Float)Client.config.getCustoms().getOrDefault(ingame, Float.valueOf(current))).floatValue(), min, max, step);
    this.displayString = this.display + MathUtil.denormalizeValue(this.sliderValue, min, max, step);
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
    Client.config.getCustoms().put(this.ingame, Float.valueOf(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step)));
    Client.config.saveConfig();
  }
}
