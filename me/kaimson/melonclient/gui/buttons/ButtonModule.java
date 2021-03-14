package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import java.util.function.Consumer;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonModule extends GuiButton {
  private final IngameDisplay display;
  
  public IngameDisplay getDisplay() {
    return this.display;
  }
  
  public ButtonModule(int id, int x, int y, int buttonWidth, String displayString, IngameDisplay display, Consumer<GuiButton> runnable) {
    super(id, x, y, buttonWidth, 50, displayString, runnable);
    this.display = display;
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
  
  public void render(Minecraft mc, int xPosition, int yPosition, int mouseX, int mouseY) {
    hoverCheck(xPosition, yPosition, mouseX, mouseY);
    drawRect(xPosition, yPosition, xPosition + this.width, yPosition + this.height, (new Color(30, 30, 30, 200)).getRGB());
    GuiUtils.drawRoundedOutline(xPosition, yPosition, xPosition + this.width, yPosition + this.height, 2.0F, 1.5F, this.display.isEnabled() ? (new Color(70, 255, 80, 200)).getRGB() : (new Color(255, 0, 40, 200)).getRGB());
    float scale = 1.0F;
    scale = rescale(scale);
    GlStateManager.pushMatrix();
    GlStateManager.scale(scale, scale, 1.0F);
    GuiUtils.drawCenteredString(this.displayString, (int)((xPosition + this.width / 2) / scale), (int)((yPosition + this.height / 4) / scale), 16777215);
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    GlStateManager.popMatrix();
  }
  
  public float rescale(float scale) {
    return ((this.width - 2) / scale <= (Minecraft.getMinecraft()).fontRendererObj.getStringWidth(this.displayString)) ? rescale(scale - 0.1F) : scale;
  }
}
