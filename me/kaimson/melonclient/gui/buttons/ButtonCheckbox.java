package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import java.util.function.Consumer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ButtonCheckbox extends GuiButton {
  private final IngameDisplay display;
  
  public IngameDisplay getDisplay() {
    return this.display;
  }
  
  public ButtonCheckbox(int id, int x, int y, int width, int height, IngameDisplay display) {
    super(id, x, y, width, height, "", button -> Client.config.setEnabled(display));
    this.display = display;
  }
  
  public ButtonCheckbox(int id, int x, int y, int width, int height, IngameDisplay display, Consumer<GuiButton> runnable) {
    super(id, x, y, width, height, "", runnable);
    this.display = display;
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {}
  
  public void render(int yPosition, int mouseX, int mouseY) {
    if (this.visible) {
      hoverCheck(this.xPosition, yPosition, mouseX, mouseY);
      GuiUtils.drawRoundedRect(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 2.0F, (new Color(255, 255, 255, 100)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, yPosition, this.xPosition + this.width, yPosition + this.height, 1.6F, 2.0F, (new Color(255, 255, 255, 150)).getRGB());
      if (this.display.isEnabled())
        renderCheckbox(yPosition); 
    } 
  }
  
  protected void renderCheckbox(int yPosition) {
    GlStateManager.pushMatrix();
    GlStateManager.disableTexture2D();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glPushMatrix();
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glBegin(1);
    GL11.glVertex2i(this.xPosition + this.width / 3, yPosition + this.height / 2);
    GL11.glVertex2i(this.xPosition + this.width / 2, yPosition + this.height * 3 / 4);
    GL11.glVertex2i(this.xPosition + this.width / 2, yPosition + this.height * 3 / 4);
    GL11.glVertex2i(this.xPosition + this.width - 1, yPosition + 3);
    GL11.glEnd();
    GL11.glPopMatrix();
    GlStateManager.enableTexture2D();
    GlStateManager.popMatrix();
  }
}
