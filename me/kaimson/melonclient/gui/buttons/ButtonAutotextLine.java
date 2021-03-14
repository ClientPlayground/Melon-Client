package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ButtonAutotextLine extends GuiButton {
  public ButtonAutotextLine(int x, int y, int width, int height, String text) {
    super(-1, x, y, width, height, text);
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, (new Color(255, 255, 255, 50)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, 2.0F, (new Color(255, 255, 255, 100)).getRGB());
      String s = mc.fontRendererObj.trimStringToWidth(this.displayString, this.width);
      GuiUtils.drawString(s, this.xPosition + 2, this.yPosition + (this.height - 8) / 2, true);
    } 
  }
}
