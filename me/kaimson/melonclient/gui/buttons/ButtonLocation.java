package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiHudEditor;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import me.kaimson.melonclient.util.BoxUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonLocation extends GuiButton {
  private final IngameDisplay display;
  
  private int lastMouseX;
  
  private int lastMouseY;
  
  private final float scale;
  
  public IngameDisplay getDisplay() {
    return this.display;
  }
  
  public int getLastMouseX() {
    return this.lastMouseX;
  }
  
  public int getLastMouseY() {
    return this.lastMouseY;
  }
  
  public ButtonLocation(IngameDisplay display) {
    super(-1, 0, 0, null);
    this.display = display;
    this.scale = display.getScale();
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    this.lastMouseX = mouseX;
    this.lastMouseY = mouseY;
    int x = (int)Client.config.getActualX(this.display);
    int y = (int)Client.config.getActualY(this.display);
    int boxX = BoxUtils.getBoxOffX(this.display, (int)(x / this.scale), this.display.getWidth());
    int boxY = BoxUtils.getBoxOffY(this.display, (int)(y / this.scale), this.display.getHeight());
    int boxX2 = boxX + this.display.getWidth();
    int boxY2 = boxY + this.display.getHeight();
    this.hovered = BoxUtils.checkHovered(boxX, boxY, boxX2, boxY2, this.lastMouseX, this.lastMouseY, this.scale);
    if (this.hovered)
      ((GuiHudEditor)mc.currentScreen).setLastHovered(this.display); 
    this.display.render(x, y);
    GlStateManager.pushMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.scale(this.scale, this.scale, 1.0F);
    drawRect(boxX, boxY, boxX2, boxY2, this.hovered ? (new Color(255, 255, 255, 100)).getRGB() : (new Color(255, 255, 255, 50)).getRGB());
    GuiUtils.drawRectOutline(boxX, boxY, boxX2, boxY2, this.hovered ? (new Color(255, 255, 255, 150)).getRGB() : (new Color(255, 255, 255, 100)).getRGB());
    GlStateManager.scale(Math.pow(this.scale, -1.0D), Math.pow(this.scale, -1.0D), 1.0D);
    GlStateManager.popMatrix();
  }
  
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    return (this.enabled && this.visible && this.hovered);
  }
  
  public boolean isHovered() {
    return this.hovered;
  }
}
