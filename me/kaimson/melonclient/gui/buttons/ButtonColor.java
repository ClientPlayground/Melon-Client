package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import java.util.function.Consumer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiScreen;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class ButtonColor extends GuiButton {
  private final IngameDisplay display;
  
  public IngameDisplay getDisplay() {
    return this.display;
  }
  
  public ButtonColor(int x, int y, int width, int height, String text, GuiScreen parentScreen, IngameDisplay display) {
    super(-1, x, y, width, height, text, button -> Minecraft.getMinecraft().displayGuiScreen((GuiScreen)parentScreen));
    this.display = display;
  }
  
  public ButtonColor(int x, int y, int width, int height, String text, GuiScreen parentScreen, IngameDisplay display, Consumer<GuiButton> runnable) {
    super(-1, x, y, width, height, text, runnable);
    this.display = display;
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      hoverCheck(mouseX, mouseY);
      if (Client.config.getCustoms().get(this.display) != null)
        drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, ((Integer)Client.config.getCustoms().get(this.display)).intValue()); 
      int lineColor = isEnabled() ? (this.hovered ? (new Color(255, 255, 255, 150)).getRGB() : (new Color(255, 255, 255, 100)).getRGB()) : (new Color(150, 150, 150, 100)).getRGB();
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, 2.0F, lineColor);
    } 
  }
}
