package me.kaimson.melonclient.gui.buttons;

import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.buttons.slider.ButtonSlider;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.MathUtil;

public class ButtonScale extends ButtonSlider {
  public ButtonScale(int id, int x, int y, int width, int height, float min, float max, float step, float current, IngameDisplay ingame) {
    super(id, x, y, width, height, min, max, step, current, ingame);
    this.sliderValue = MathUtil.normalizeValue(Client.config.getScale(ingame), min, max, step);
    this.displayString = this.display + MathUtil.denormalizeValue(this.sliderValue, min, max, step);
  }
  
  public void mouseReleased(int mouseX, int mouseY) {
    super.mouseReleased(mouseX, mouseY);
    Client.config.setScale(getIngame(), MathUtil.denormalizeValue(this.sliderValue, this.min, this.max, this.step));
    Client.config.saveConfig();
  }
}
