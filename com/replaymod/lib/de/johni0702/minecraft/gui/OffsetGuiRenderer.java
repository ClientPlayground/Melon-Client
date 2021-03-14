package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import lombok.NonNull;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class OffsetGuiRenderer implements GuiRenderer {
  @NonNull
  private final GuiRenderer renderer;
  
  @NonNull
  private final ReadablePoint position;
  
  @NonNull
  private final ReadableDimension size;
  
  private final boolean strict;
  
  public OffsetGuiRenderer(OffsetGuiRenderer renderer) {
    this(renderer.renderer, renderer.position, renderer.size, true);
  }
  
  public OffsetGuiRenderer(GuiRenderer renderer, ReadablePoint position, ReadableDimension size) {
    this(renderer, position, size, true);
  }
  
  public OffsetGuiRenderer(GuiRenderer renderer, ReadablePoint position, ReadableDimension size, boolean strict) {
    this.renderer = renderer;
    this.position = position;
    this.size = size;
    this.strict = strict;
  }
  
  public ReadablePoint getOpenGlOffset() {
    ReadablePoint parentOffset = this.renderer.getOpenGlOffset();
    return (ReadablePoint)new Point(parentOffset.getX() + this.position.getX(), parentOffset.getY() + this.position.getY());
  }
  
  public ReadableDimension getSize() {
    return this.size;
  }
  
  public void setDrawingArea(int x, int y, int width, int height) {
    if (!this.strict) {
      this.renderer.setDrawingArea(x + this.position.getX(), y + this.position.getY(), width, height);
      return;
    } 
    int x2 = x + width;
    int y2 = y + height;
    x = Math.max(0, x + this.position.getX());
    y = Math.max(0, y + this.position.getY());
    x2 = Math.min(x2, this.size.getWidth()) + this.position.getX();
    y2 = Math.min(y2, this.size.getHeight()) + this.position.getY();
    x2 = Math.max(x2, x);
    y2 = Math.max(y2, y);
    this.renderer.setDrawingArea(x, y, x2 - x, y2 - y);
  }
  
  public void startUsing() {
    GL11.glPushAttrib(524288);
    GL11.glEnable(3089);
    setDrawingArea(0, 0, this.size.getWidth(), this.size.getHeight());
  }
  
  public void stopUsing() {
    GL11.glPopAttrib();
  }
  
  public void bindTexture(ResourceLocation location) {
    this.renderer.bindTexture(location);
  }
  
  public void bindTexture(int glId) {
    this.renderer.bindTexture(glId);
  }
  
  public void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
    this.renderer.drawTexturedRect(x + this.position.getX(), y + this.position.getY(), u, v, width, height);
  }
  
  public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
    this.renderer.drawTexturedRect(x + this.position.getX(), y + this.position.getY(), u, v, width, height, uWidth, vHeight, textureWidth, textureHeight);
  }
  
  public void drawRect(int x, int y, int width, int height, int color) {
    this.renderer.drawRect(x + this.position.getX(), y + this.position.getY(), width, height, color);
  }
  
  public void drawRect(int x, int y, int width, int height, ReadableColor color) {
    this.renderer.drawRect(x + this.position.getX(), y + this.position.getY(), width, height, color);
  }
  
  public void drawRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
    this.renderer.drawRect(x + this.position.getX(), y + this.position.getY(), width, height, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor);
  }
  
  public void drawRect(int x, int y, int width, int height, ReadableColor topLeftColor, ReadableColor topRightColor, ReadableColor bottomLeftColor, ReadableColor bottomRightColor) {
    this.renderer.drawRect(x + this.position.getX(), y + this.position.getY(), width, height, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor);
  }
  
  public int drawString(int x, int y, int color, String text) {
    return this.renderer.drawString(x + this.position.getX(), y + this.position.getY(), color, text) - this.position.getX();
  }
  
  public int drawString(int x, int y, ReadableColor color, String text) {
    return this.renderer.drawString(x + this.position.getX(), y + this.position.getY(), color, text) - this.position.getX();
  }
  
  public int drawCenteredString(int x, int y, int color, String text) {
    return this.renderer.drawCenteredString(x + this.position.getX(), y + this.position.getY(), color, text) - this.position.getX();
  }
  
  public int drawCenteredString(int x, int y, ReadableColor color, String text) {
    return this.renderer.drawCenteredString(x + this.position.getX(), y + this.position.getY(), color, text) - this.position.getX();
  }
  
  public int drawString(int x, int y, int color, String text, boolean shadow) {
    return this.renderer.drawString(x + this.position.getX(), y + this.position.getY(), color, text, shadow) - this.position.getX();
  }
  
  public int drawString(int x, int y, ReadableColor color, String text, boolean shadow) {
    return this.renderer.drawString(x + this.position.getX(), y + this.position.getY(), color, text, shadow) - this.position.getX();
  }
  
  public int drawCenteredString(int x, int y, int color, String text, boolean shadow) {
    return this.renderer.drawCenteredString(x + this.position.getX(), y + this.position.getY(), color, text, shadow) - this.position.getX();
  }
  
  public int drawCenteredString(int x, int y, ReadableColor color, String text, boolean shadow) {
    return this.renderer.drawCenteredString(x + this.position.getX(), y + this.position.getY(), color, text, shadow) - this.position.getX();
  }
}
