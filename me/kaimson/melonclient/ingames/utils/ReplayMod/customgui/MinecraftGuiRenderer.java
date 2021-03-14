package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MinecraftGuiRenderer implements GuiRenderer {
  private final Gui gui = new Gui() {
    
    };
  
  @NonNull
  private final ScaledResolution size;
  
  public MinecraftGuiRenderer(ScaledResolution size) {
    this.size = size;
  }
  
  public ReadablePoint getOpenGlOffset() {
    return (ReadablePoint)new Point(0, 0);
  }
  
  public ReadableDimension getSize() {
    return new ReadableDimension() {
        public int getWidth() {
          return MinecraftGuiRenderer.this.size.getScaledWidth();
        }
        
        public int getHeight() {
          return MinecraftGuiRenderer.this.size.getScaledHeight();
        }
        
        public void getSize(WritableDimension dest) {
          dest.setSize(getWidth(), getHeight());
        }
      };
  }
  
  public void setDrawingArea(int x, int y, int width, int height) {
    y = this.size.getScaledHeight() - y - height;
    int f = this.size.getScaleFactor();
    GL11.glScissor(x * f, y * f, width * f, height * f);
  }
  
  public void bindTexture(ResourceLocation location) {
    Minecraft.getMinecraft().getTextureManager().bindTexture(location);
  }
  
  public void bindTexture(int glId) {
    GlStateManager.bindTexture(glId);
  }
  
  public void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
    this.gui.drawTexturedModalRect(x, y, u, v, width, height);
  }
  
  public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
    color(1, 1, 1);
    Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, textureWidth, textureHeight);
  }
  
  public void drawRect(int x, int y, int width, int height, int color) {
    Gui.drawRect(x, y, x + width, y + height, color);
    color(1, 1, 1);
    GlStateManager.enableBlend();
  }
  
  public void drawRect(int x, int y, int width, int height, ReadableColor color) {
    drawRect(x, y, width, height, color(color));
  }
  
  public void drawRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
    drawRect(x, y, width, height, color(topLeftColor), color(topRightColor), color(bottomLeftColor), color(bottomRightColor));
  }
  
  public void drawRect(int x, int y, int width, int height, ReadableColor tl, ReadableColor tr, ReadableColor bl, ReadableColor br) {}
  
  public int drawString(int x, int y, int color, String text) {
    return drawString(x, y, color, text, false);
  }
  
  public int drawString(int x, int y, ReadableColor color, String text) {
    return drawString(x, y, color(color), text);
  }
  
  public int drawCenteredString(int x, int y, int color, String text) {
    return drawCenteredString(x, y, color, text, false);
  }
  
  public int drawCenteredString(int x, int y, ReadableColor color, String text) {
    return drawCenteredString(x, y, color(color), text);
  }
  
  public int drawString(int x, int y, int color, String text, boolean shadow) {
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    int ret = shadow ? fontRenderer.drawString(text, x, y, color) : fontRenderer.drawString(text, x, y, color);
    color(1, 1, 1);
    return ret;
  }
  
  public int drawString(int x, int y, ReadableColor color, String text, boolean shadow) {
    return drawString(x, y, color(color), text, shadow);
  }
  
  public int drawCenteredString(int x, int y, int color, String text, boolean shadow) {
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    x -= fontRenderer.getStringWidth(text) / 2;
    return drawString(x, y, color, text, shadow);
  }
  
  public int drawCenteredString(int x, int y, ReadableColor color, String text, boolean shadow) {
    return drawCenteredString(x, y, color(color), text, shadow);
  }
  
  private int color(ReadableColor color) {
    return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
  }
  
  private ReadableColor color(int color) {
    return (ReadableColor)new Color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
  }
  
  private void color(int r, int g, int b) {
    GlStateManager.color(r, g, b);
  }
}
