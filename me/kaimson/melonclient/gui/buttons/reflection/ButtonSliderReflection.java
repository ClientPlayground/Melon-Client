package me.kaimson.melonclient.gui.buttons.reflection;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.buttons.slider.ButtonSlider;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.MathUtil;

public class ButtonSliderReflection extends ButtonSlider {
  private IngameDisplay ingame;
  
  private IngameDisplay setting;
  
  public IngameDisplay getIngame() {
    return this.ingame;
  }
  
  public ButtonSliderReflection(int id, int x, int y, int width, int height, float min, float max, float step, float current, IngameDisplay ingame, IngameDisplay setting) {
    super(id, x, y, width, height, min, max, step, current, ingame);
    this.ingame = ingame;
    this.setting = setting;
    this.sliderValue = MathUtil.normalizeValue(((Float)Client.config.getReflectedObject(setting.name().toLowerCase(), this.display)).floatValue(), min, max, step);
    this.displayString = this.display + MathUtil.denormalizeValue(this.sliderValue, min, max, step);
  }
  
  public void mouseReleased(int mouseX, int mouseY) {
    super.mouseReleased(mouseX, mouseY);
    Client.config.setReflectedObject(this.setting.name().toLowerCase(), Float.valueOf(MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step)), this.ingame);
    Client.config.saveConfig();
  }
}
