package me.kaimson.melonclient.gui.buttons.reflection;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.gui.buttons.ButtonCheckbox;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import me.kaimson.melonclient.ingames.IngameDisplay;

public class ButtonCheckboxReflection extends ButtonCheckbox {
  private final IngameDisplay setting;
  
  public ButtonCheckboxReflection(int x, int y, int width, int height, IngameDisplay display, IngameDisplay setting) {
    super(-1, x, y, width, height, display, button -> Client.config.setReflectedObject(setting.name().toLowerCase(), Boolean.valueOf(!((Boolean)Client.config.getReflectedObject(setting.name().toLowerCase(), display)).booleanValue()), display));
    this.setting = setting;
  }
  
  public void render(int yPosition, int mouseX, int mouseY) {
    if (this.visible) {
      hoverCheck(this.xPosition, yPosition, mouseX, mouseY);
      GuiUtils.drawRoundedRect(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 2.0F, (new Color(255, 255, 255, 100)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 1.6F, 2.0F, (new Color(255, 255, 255, 150)).getRGB());
      if (((Boolean)Client.config.getReflectedObject(this.setting.name().toLowerCase(), getDisplay())).booleanValue())
        renderCheckbox(yPosition); 
    } 
  }
}
