package me.kaimson.melonclient.gui.buttons;

import java.awt.Color;
import java.util.function.Consumer;
import me.kaimson.melonclient.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ButtonBack extends GuiButton {
  public ButtonBack(int x, int y, int width, int height, Consumer<GuiButton> onPress) {
    super(-1, x, y, width, height, "", onPress);
  }
  
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      this.hovered = (mouseX >= this.xPosition && mouseX <= this.xPosition + this.width && mouseY >= this.yPosition && mouseY <= this.yPosition + this.height);
      GuiUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, this.hovered ? (new Color(255, 255, 255, 100)).getRGB() : (new Color(255, 255, 255, 50)).getRGB());
      GuiUtils.drawRoundedOutline(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.0F, 2.0F, (new Color(255, 255, 255, 100)).getRGB());
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GL11.glPushMatrix();
      GL11.glLineWidth(2.0F);
      GL11.glBegin(1);
      GL11.glVertex2f((this.xPosition + 2), (this.yPosition + this.height / 2));
      GL11.glVertex2f((this.xPosition + this.width - 2), (this.yPosition + this.height / 2));
      GL11.glEnd();
      GL11.glBegin(1);
      GL11.glVertex2f((this.xPosition + 2), (this.yPosition + this.height / 2));
      GL11.glVertex2f((this.xPosition + this.width / 2), (this.yPosition + this.height / 4 - 1));
      GL11.glEnd();
      GL11.glBegin(1);
      GL11.glVertex2f((this.xPosition + 2), (this.yPosition + this.height / 2));
      GL11.glVertex2f((this.xPosition + this.width / 2), (this.yPosition + this.height - this.height / 4 + 1));
      GL11.glEnd();
      GL11.glPopMatrix();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
    } 
  }
}
