package me.kaimson.melonclient.gui;

import java.awt.Color;
import me.kaimson.melonclient.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class GuiUtils extends Gui {
  public static GuiUtils instance;
  
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  public GuiUtils() {
    instance = this;
  }
  
  public static int drawString(String text, float x, float y, int color, boolean shadow) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    return mc.fontRendererObj.drawString(text, x, y, color, shadow);
  }
  
  public static int drawString(String text, int x, int y) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    return drawString(text, x, y, 16777215);
  }
  
  public static int drawString(String text, int x, int y, boolean shadow) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    return drawString(text, x, y, 16777215, shadow);
  }
  
  public static int drawString(String text, int x, int y, int color) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    return drawString(text, x, y, color, false);
  }
  
  public static int drawString(String text, int x, int y, int color, boolean shadow) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    String[] lines = text.split("\n");
    if (lines.length > 1) {
      int j = 0;
      for (int i = 0; i < lines.length; i++)
        j += mc.fontRendererObj.drawString(lines[i], x, (y + i * (mc.fontRendererObj.FONT_HEIGHT + 2)), color, shadow); 
      return j;
    } 
    return mc.fontRendererObj.drawString(text, x, y, color, shadow);
  }
  
  public static int drawScaledString(String text, int x, int y, boolean shadow, float scale) {
    GlStateManager.pushMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.scale(scale, scale, 1.0F);
    int i = drawString(text, (int)(x / scale), (int)(y / scale), shadow);
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    GlStateManager.popMatrix();
    return i;
  }
  
  public static void drawChromaString(String textIn, int xIn, int y, boolean shadow) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    for (int j = 0; j < (textIn.split("\n")).length; j++) {
      int x = xIn;
      for (char c : textIn.split("\n")[j].toCharArray()) {
        long l = System.currentTimeMillis() - x * 10L - (y - j * 10L) * 10L;
        float speed = 2000.0F;
        int color = Color.HSBtoRGB((float)(l % (int)speed) / speed, 0.8F, 0.8F);
        drawString(String.valueOf(c), x, y + j * (mc.fontRendererObj.FONT_HEIGHT + 2), color, shadow);
        x += mc.fontRendererObj.getStringWidth(String.valueOf(c));
      } 
    } 
  }
  
  public static int drawCenteredString(String text, int x, int y) {
    return drawString(text, x - mc.fontRendererObj.getStringWidth(text) / 2, y);
  }
  
  public static int drawCenteredString(String text, int x, int y, int color) {
    return drawString(text, x - mc.fontRendererObj.getStringWidth(text) / 2, y, color);
  }
  
  public static int drawCenteredString(String text, int x, int y, int color, boolean shadow) {
    return drawString(text, x - mc.fontRendererObj.getStringWidth(text) / 2, y, color, shadow);
  }
  
  public static int drawScaledCenteredString(String text, int x, int y, boolean shadow, float scale) {
    return drawScaledString(text, x - mc.fontRendererObj.getStringWidth(text) / 2, y, shadow, scale);
  }
  
  public static void drawRectOutline(int left, int top, int right, int bottom, int color) {
    drawRect(left - 1, top - 1, right + 1, top, color);
    drawRect(right, top, right + 1, bottom, color);
    drawRect(left - 1, bottom, right + 1, bottom + 1, color);
    drawRect(left - 1, top, left, bottom, color);
  }
  
  public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
    float f = (startColor >> 24 & 0xFF) / 255.0F;
    float f1 = (startColor >> 16 & 0xFF) / 255.0F;
    float f2 = (startColor >> 8 & 0xFF) / 255.0F;
    float f3 = (startColor & 0xFF) / 255.0F;
    float f4 = (endColor >> 24 & 0xFF) / 255.0F;
    float f5 = (endColor >> 16 & 0xFF) / 255.0F;
    float f6 = (endColor >> 8 & 0xFF) / 255.0F;
    float f7 = (endColor & 0xFF) / 255.0F;
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    worldrenderer.pos(right, top, this.zLevel).color(f1, f2, f3, f).endVertex();
    worldrenderer.pos(left, top, this.zLevel).color(f1, f2, f3, f).endVertex();
    worldrenderer.pos(left, bottom, this.zLevel).color(f5, f6, f7, f4).endVertex();
    worldrenderer.pos(right, bottom, this.zLevel).color(f5, f6, f7, f4).endVertex();
    tessellator.draw();
    GlStateManager.shadeModel(7424);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
  }
  
  public static void drawRoundedRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, float radius, int color) {
    float f1 = (color >> 24 & 0xFF) / 255.0F;
    float f2 = (color >> 16 & 0xFF) / 255.0F;
    float f3 = (color >> 8 & 0xFF) / 255.0F;
    float f4 = (color & 0xFF) / 255.0F;
    GlStateManager.color(f2, f3, f4, f1);
    drawRoundedRect(paramInt1, paramInt2, paramInt3, paramInt4, radius);
  }
  
  public static void drawRoundedRect(float paramInt1, float paramInt2, float paramInt3, float paramInt4, float radius, int color) {
    float f1 = (color >> 24 & 0xFF) / 255.0F;
    float f2 = (color >> 16 & 0xFF) / 255.0F;
    float f3 = (color >> 8 & 0xFF) / 255.0F;
    float f4 = (color & 0xFF) / 255.0F;
    GlStateManager.color(f2, f3, f4, f1);
    drawRoundedRect(paramInt1, paramInt2, paramInt3, paramInt4, radius);
  }
  
  private static void drawRoundedRect(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5) {
    int i = 18;
    float f1 = 90.0F / i;
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableCull();
    GlStateManager.enableColorMaterial();
    GlStateManager.blendFunc(770, 771);
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GL11.glBegin(5);
    GL11.glVertex2f(paramFloat1 + paramFloat5, paramFloat2);
    GL11.glVertex2f(paramFloat1 + paramFloat5, paramFloat4);
    GL11.glVertex2f(paramFloat3 - paramFloat5, paramFloat2);
    GL11.glVertex2f(paramFloat3 - paramFloat5, paramFloat4);
    GL11.glEnd();
    GL11.glBegin(5);
    GL11.glVertex2f(paramFloat1, paramFloat2 + paramFloat5);
    GL11.glVertex2f(paramFloat1 + paramFloat5, paramFloat2 + paramFloat5);
    GL11.glVertex2f(paramFloat1, paramFloat4 - paramFloat5);
    GL11.glVertex2f(paramFloat1 + paramFloat5, paramFloat4 - paramFloat5);
    GL11.glEnd();
    GL11.glBegin(5);
    GL11.glVertex2f(paramFloat3, paramFloat2 + paramFloat5);
    GL11.glVertex2f(paramFloat3 - paramFloat5, paramFloat2 + paramFloat5);
    GL11.glVertex2f(paramFloat3, paramFloat4 - paramFloat5);
    GL11.glVertex2f(paramFloat3 - paramFloat5, paramFloat4 - paramFloat5);
    GL11.glEnd();
    GL11.glBegin(6);
    float f2 = paramFloat3 - paramFloat5;
    float f3 = paramFloat2 + paramFloat5;
    GL11.glVertex2f(f2, f3);
    int j;
    for (j = 0; j <= i; j++) {
      float f4 = j * f1;
      GL11.glVertex2f((float)(f2 + paramFloat5 * Math.cos(Math.toRadians(f4))), (float)(f3 - paramFloat5 * Math.sin(Math.toRadians(f4))));
    } 
    GL11.glEnd();
    GL11.glBegin(6);
    f2 = paramFloat1 + paramFloat5;
    f3 = paramFloat2 + paramFloat5;
    GL11.glVertex2f(f2, f3);
    for (j = 0; j <= i; j++) {
      float f4 = j * f1;
      GL11.glVertex2f((float)(f2 - paramFloat5 * Math.cos(Math.toRadians(f4))), (float)(f3 - paramFloat5 * Math.sin(Math.toRadians(f4))));
    } 
    GL11.glEnd();
    GL11.glBegin(6);
    f2 = paramFloat1 + paramFloat5;
    f3 = paramFloat4 - paramFloat5;
    GL11.glVertex2f(f2, f3);
    for (j = 0; j <= i; j++) {
      float f4 = j * f1;
      GL11.glVertex2f((float)(f2 - paramFloat5 * Math.cos(Math.toRadians(f4))), (float)(f3 + paramFloat5 * Math.sin(Math.toRadians(f4))));
    } 
    GL11.glEnd();
    GL11.glBegin(6);
    f2 = paramFloat3 - paramFloat5;
    f3 = paramFloat4 - paramFloat5;
    GL11.glVertex2f(f2, f3);
    for (j = 0; j <= i; j++) {
      float f4 = j * f1;
      GL11.glVertex2f((float)(f2 + paramFloat5 * Math.cos(Math.toRadians(f4))), (float)(f3 + paramFloat5 * Math.sin(Math.toRadians(f4))));
    } 
    GL11.glEnd();
    GlStateManager.enableCull();
    GlStateManager.disableBlend();
    GlStateManager.disableColorMaterial();
    GlStateManager.enableTexture2D();
  }
  
  public static void drawRoundedOutline(int x, int y, int x2, int y2, float radius, float width, int color) {
    float f1 = (color >> 24 & 0xFF) / 255.0F;
    float f2 = (color >> 16 & 0xFF) / 255.0F;
    float f3 = (color >> 8 & 0xFF) / 255.0F;
    float f4 = (color & 0xFF) / 255.0F;
    GlStateManager.color(f2, f3, f4, f1);
    drawRoundedOutline(x, y, x2, y2, radius, width);
  }
  
  private static void drawRoundedOutline(float x, float y, float x2, float y2, float radius, float width) {
    int i = 18;
    int j = 90 / i;
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableCull();
    GlStateManager.enableColorMaterial();
    GlStateManager.blendFunc(770, 771);
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    if (width != 1.0F)
      GL11.glLineWidth(width); 
    GL11.glBegin(3);
    GL11.glVertex2f(x + radius, y);
    GL11.glVertex2f(x2 - radius, y);
    GL11.glEnd();
    GL11.glBegin(3);
    GL11.glVertex2f(x2, y + radius);
    GL11.glVertex2f(x2, y2 - radius);
    GL11.glEnd();
    GL11.glBegin(3);
    GL11.glVertex2f(x2 - radius, y2 - 0.1F);
    GL11.glVertex2f(x + radius, y2 - 0.1F);
    GL11.glEnd();
    GL11.glBegin(3);
    GL11.glVertex2f(x + 0.1F, y2 - radius);
    GL11.glVertex2f(x + 0.1F, y + radius);
    GL11.glEnd();
    float f1 = x2 - radius;
    float f2 = y + radius;
    GL11.glBegin(3);
    int k;
    for (k = 0; k <= i; k++) {
      int m = 90 - k * j;
      GL11.glVertex2f((float)(f1 + radius * MathUtil.getRightAngle(m)), (float)(f2 - radius * MathUtil.getAngle(m)));
    } 
    GL11.glEnd();
    f1 = x2 - radius;
    f2 = y2 - radius;
    GL11.glBegin(3);
    for (k = 0; k <= i; k++) {
      int m = k * j + 270;
      GL11.glVertex2f((float)(f1 + radius * MathUtil.getRightAngle(m)), (float)(f2 - radius * MathUtil.getAngle(m)));
    } 
    GL11.glEnd();
    GL11.glBegin(3);
    f1 = x + radius;
    f2 = y2 - radius;
    for (k = 0; k <= i; k++) {
      int m = k * j + 90;
      GL11.glVertex2f((float)(f1 + radius * MathUtil.getRightAngle(m)), (float)(f2 + radius * MathUtil.getAngle(m)));
    } 
    GL11.glEnd();
    GL11.glBegin(3);
    f1 = x + radius;
    f2 = y + radius;
    for (k = 0; k <= i; k++) {
      int m = 270 - k * j;
      GL11.glVertex2f((float)(f1 + radius * MathUtil.getRightAngle(m)), (float)(f2 + radius * MathUtil.getAngle(m)));
    } 
    GL11.glEnd();
    if (width != 1.0F)
      GL11.glLineWidth(1.0F); 
    GlStateManager.enableCull();
    GlStateManager.disableBlend();
    GlStateManager.disableColorMaterial();
    GlStateManager.enableTexture2D();
  }
  
  public static void drawCircle(float x, float y, float radius, float thickness, Color color, boolean smooth) {
    drawPartialCircle(x, y, radius, 0, 360, thickness, color, smooth);
  }
  
  public static void drawPartialCircle(int x, int y, float radius, int startAngle, int endAngle, float thickness, Color color, boolean smooth) {
    drawPartialCircle(x, y, radius, startAngle, endAngle, thickness, color, smooth);
  }
  
  public static void drawPartialCircle(float x, float y, float radius, int startAngle, int endAngle, float thickness, Color colour, boolean smooth) {
    GL11.glDisable(3553);
    GL11.glBlendFunc(770, 771);
    if (startAngle > endAngle) {
      int temp = startAngle;
      startAngle = endAngle;
      endAngle = temp;
    } 
    if (startAngle < 0)
      startAngle = 0; 
    if (endAngle > 360)
      endAngle = 360; 
    if (smooth) {
      GL11.glEnable(2848);
    } else {
      GL11.glDisable(2848);
    } 
    GL11.glLineWidth(thickness);
    GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
    GL11.glBegin(3);
    float ratio = 0.017453292F;
    for (int i = startAngle; i <= endAngle; i++) {
      float radians = (i - 90) * ratio;
      GL11.glVertex2f(x + (float)Math.cos(radians) * radius, y + (float)Math.sin(radians) * radius);
    } 
    GL11.glEnd();
    GL11.glEnable(3553);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public static void drawFilledRect(float x1, float y1, float x2, float y2, int colour, boolean smooth) {
    drawFilledShape(new float[] { x1, y1, x1, y2, x2, y2, x2, y1 }, new Color(colour, true), smooth);
  }
  
  public static void drawFilledShape(float[] points, Color colour, boolean smooth) {
    GL11.glPushMatrix();
    GL11.glDisable(3553);
    GL11.glBlendFunc(770, 771);
    if (smooth) {
      GL11.glEnable(2848);
    } else {
      GL11.glDisable(2848);
    } 
    GL11.glLineWidth(1.0F);
    GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
    GL11.glBegin(9);
    for (int i = 0; i < points.length; i += 2)
      GL11.glVertex2f(points[i], points[i + 1]); 
    GL11.glEnd();
    GL11.glEnable(3553);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glPopMatrix();
  }
  
  public static void drawLine(float x, float x1, float y, float thickness, int colour, boolean smooth) {
    drawLines(new float[] { x, y, x1, y }, thickness, new Color(colour, true), smooth);
  }
  
  public static void drawVerticalLine(float x, float y, float y1, float thickness, int colour, boolean smooth) {
    drawLines(new float[] { x, y, x, y1 }, thickness, new Color(colour, true), smooth);
  }
  
  public static void drawLines(float[] points, float thickness, Color colour, boolean smooth) {
    GL11.glPushMatrix();
    GL11.glDisable(3553);
    GL11.glBlendFunc(770, 771);
    if (smooth) {
      GL11.glEnable(2848);
    } else {
      GL11.glDisable(2848);
    } 
    GL11.glLineWidth(thickness);
    GL11.glColor4f(colour.getRed() / 255.0F, colour.getGreen() / 255.0F, colour.getBlue() / 255.0F, colour.getAlpha() / 255.0F);
    GL11.glBegin(1);
    for (int i = 0; i < points.length; i += 2)
      GL11.glVertex2f(points[i], points[i + 1]); 
    GL11.glEnd();
    GL11.glEnable(2848);
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glEnable(3553);
    GL11.glPopMatrix();
  }
}
