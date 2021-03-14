package me.kaimson.melonclient.gui.buttons.reflection;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiScreen;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.gui.buttons.ButtonColor;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class ButtonColorReflection extends ButtonColor {
  private final IngameDisplay setting;
  
  public ButtonColorReflection(int x, int y, int width, int height, GuiScreen parentScreen, IngameDisplay display, IngameDisplay setting) {
    super(x, y, width, height, "", parentScreen, display, button -> Minecraft.getMinecraft().displayGuiScreen((GuiScreen)parentScreen));
    this.setting = setting;
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      hoverCheck(mouseX, mouseY);
      if (((Integer)Client.config.getReflectedObject(this.setting.name().toLowerCase(), getDisplay())).intValue() != 0)
        drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, ((Integer)Client.config.getReflectedObject(this.setting.name().toLowerCase(), getDisplay())).intValue()); 
      int lineColor = isEnabled() ? (this.hovered ? (new Color(255, 255, 255, 150)).getRGB() : (new Color(255, 255, 255, 100)).getRGB()) : (new Color(150, 150, 150, 100)).getRGB();
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, 2.0F, lineColor);
    } 
  }
}
