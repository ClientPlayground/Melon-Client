package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;

public class ButtonFade extends GuiButton {
  public ButtonFade(String text) {
    super(text);
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
  
  public void render(int mouseX, int mouseY) {
    if (this.visible) {
      hoverCheck(mouseX, mouseY);
      GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.5F, this.hovered ? (new Color(255, 255, 255, 100))
          .getRGB() : (new Color(255, 255, 255, 50)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.5F, 2.0F, this.hovered ? (new Color(255, 255, 255, 150))
          .getRGB() : (new Color(255, 255, 255, 100)).getRGB());
      GuiUtils.drawCenteredString(this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2);
    } 
  }
}
