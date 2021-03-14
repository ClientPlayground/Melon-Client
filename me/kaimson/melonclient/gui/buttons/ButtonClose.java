package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ButtonClose extends GuiButton {
  public ButtonClose(int x, int y, int width, int height) {
    super(x, y, width, height, "");
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      hoverCheck(mouseX, mouseY);
      GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, this.hovered ? (new Color(255, 255, 255, 100)).getRGB() : (new Color(255, 255, 255, 50)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, 2.0F, this.hovered ? (new Color(255, 255, 255, 125)).getRGB() : (new Color(255, 255, 255, 100)).getRGB());
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GL11.glPushMatrix();
      GL11.glLineWidth(2.0F);
      GL11.glBegin(1);
      GL11.glVertex2i(this.xPosition + 2, this.yPosition + 2);
      GL11.glVertex2i(this.xPosition + this.width - 2, this.yPosition + this.height - 2);
      GL11.glEnd();
      GL11.glBegin(1);
      GL11.glVertex2i(this.xPosition + this.width - 2, this.yPosition + 2);
      GL11.glVertex2i(this.xPosition + 2, this.yPosition + this.height - 2);
      GL11.glEnd();
      GL11.glPopMatrix();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
    } 
  }
}
