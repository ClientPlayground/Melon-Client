package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import lombok.NonNull;
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
          return MinecraftGuiRenderer.this.size.func_78326_a();
        }
        
        public int getHeight() {
          return MinecraftGuiRenderer.this.size.func_78328_b();
        }
        
        public void getSize(WritableDimension dest) {
          dest.setSize(getWidth(), getHeight());
        }
      };
  }
  
  public void setDrawingArea(int x, int y, int width, int height) {
    y = this.size.func_78328_b() - y - height;
    int f = this.size.func_78325_e();
    GL11.glScissor(x * f, y * f, width * f, height * f);
  }
  
  public void bindTexture(ResourceLocation location) {
    MCVer.getMinecraft().func_110434_K().func_110577_a(location);
  }
  
  public void bindTexture(int glId) {
    GlStateManager.func_179144_i(glId);
  }
  
  public void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
    this.gui.func_73729_b(x, y, u, v, width, height);
  }
  
  public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
    color(1, 1, 1);
    Gui.func_152125_a(x, y, u, v, uWidth, vHeight, width, height, textureWidth, textureHeight);
  }
  
  public void drawRect(int x, int y, int width, int height, int color) {
    Gui.func_73734_a(x, y, x + width, y + height, color);
    color(1, 1, 1);
    GlStateManager.func_179147_l();
  }
  
  public void drawRect(int x, int y, int width, int height, ReadableColor color) {
    drawRect(x, y, width, height, color(color));
  }
  
  public void drawRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
    drawRect(x, y, width, height, color(topLeftColor), color(topRightColor), color(bottomLeftColor), color(bottomRightColor));
  }
  
  public void drawRect(int x, int y, int width, int height, ReadableColor tl, ReadableColor tr, ReadableColor bl, ReadableColor br) {
    GlStateManager.func_179090_x();
    GlStateManager.func_179147_l();
    GlStateManager.func_179118_c();
    GlStateManager.func_179120_a(770, 771, 1, 0);
    GlStateManager.func_179103_j(7425);
    MCVer.drawRect(x, y, width, height, tl, tr, bl, br);
    GlStateManager.func_179103_j(7424);
    GlStateManager.func_179141_d();
    GlStateManager.func_179098_w();
  }
  
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
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int ret = shadow ? fontRenderer.func_175063_a(text, x, y, color) : fontRenderer.func_78276_b(text, x, y, color);
    color(1, 1, 1);
    return ret;
  }
  
  public int drawString(int x, int y, ReadableColor color, String text, boolean shadow) {
    return drawString(x, y, color(color), text, shadow);
  }
  
  public int drawCenteredString(int x, int y, int color, String text, boolean shadow) {
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    x -= fontRenderer.func_78256_a(text) / 2;
    return drawString(x, y, color, text, shadow);
  }
  
  public int drawCenteredString(int x, int y, ReadableColor color, String text, boolean shadow) {
    return drawCenteredString(x, y, color(color), text, shadow);
  }
  
  private int color(ReadableColor color) {
    return color.getAlpha() << 24 | color
      .getRed() << 16 | color
      .getGreen() << 8 | color
      .getBlue();
  }
  
  private ReadableColor color(int color) {
    return (ReadableColor)new Color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF);
  }
  
  private void color(int r, int g, int b) {
    GlStateManager.func_179124_c(r, g, b);
  }
}
