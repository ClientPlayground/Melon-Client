package me.kaimson.melonclient.ingames.utils.keystrokes;

import java.awt.Color;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.util.GLColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class KeyUtils {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final Keystrokes keystrokes;
  
  public static KeyUtils instance;
  
  public KeyUtils(Keystrokes keystrokes) {
    this.keystrokes = keystrokes;
    instance = this;
  }
  
  public void drawKeyText(String text, double width, double height, boolean pressed) {
    GuiUtils.drawString(text, (int)(width - this.mc.fontRendererObj.getStringWidth(text)) / 2 + 1, (int)(height - this.mc.fontRendererObj.FONT_HEIGHT) / 2 + 1, this.keystrokes.getColor((width - this.mc.fontRendererObj.getStringWidth(text)) / 2.0D + 1.0D, pressed));
  }
  
  public void drawSpacebar(double width, double height, boolean pressed) {
    drawColoredRect(width * 0.25D, height / 2.0D - 1.0D, width * 0.75D, height / 2.0D + 1.0D, pressed);
  }
  
  private void drawColoredRect(double x1, double y1, double x2, double y2, boolean invertColor) {
    GlStateManager.pushMatrix();
    GL11.glPushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GL11.glShadeModel(7425);
    GL11.glBegin(7);
    GLColor.setGlColor(this.keystrokes.getColor(x1, invertColor));
    GL11.glVertex3d(x1, y2, 0.0D);
    GLColor.setGlColor(this.keystrokes.getColor(x2, invertColor));
    GL11.glVertex3d(x2, y2, 0.0D);
    GL11.glVertex3d(x2, y1, 0.0D);
    GLColor.setGlColor(this.keystrokes.getColor(x1, invertColor));
    GL11.glVertex3d(x1, y1, 0.0D);
    GL11.glEnd();
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
    GLColor.setGlColor((new Color(255, 255, 255, 255)).getRGB());
    GL11.glPopMatrix();
    GlStateManager.popMatrix();
  }
}
