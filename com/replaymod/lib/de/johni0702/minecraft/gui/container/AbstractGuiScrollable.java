package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;

public abstract class AbstractGuiScrollable<T extends AbstractGuiScrollable<T>> extends AbstractGuiContainer<T> implements Scrollable {
  private int offsetX;
  
  private int offsetY;
  
  private final ReadablePoint negativeOffset = new ReadablePoint() {
      public int getX() {
        return -AbstractGuiScrollable.this.offsetX;
      }
      
      public int getY() {
        return -AbstractGuiScrollable.this.offsetY;
      }
      
      public void getLocation(WritablePoint dest) {
        dest.setLocation(getX(), getY());
      }
    };
  
  private Direction scrollDirection = Direction.VERTICAL;
  
  protected ReadableDimension lastRenderSize;
  
  public AbstractGuiScrollable(GuiContainer container) {
    super(container);
  }
  
  public void convertFor(GuiElement element, Point point, int relativeLayer) {
    super.convertFor(element, point, relativeLayer);
    if (relativeLayer > 0 || (point.getX() > 0 && point.getX() < this.lastRenderSize.getWidth() && point
      .getY() > 0 && point.getY() < this.lastRenderSize.getHeight())) {
      point.translate(this.offsetX, this.offsetY);
    } else {
      point.setLocation(-2147483648, -2147483648);
    } 
  }
  
  public void layout(ReadableDimension size, RenderInfo renderInfo) {
    Dimension dimension;
    if (size != null) {
      int width = size.getWidth();
      int height = size.getHeight();
      this.lastRenderSize = size;
      size = super.calcMinSize();
      dimension = new Dimension(Math.max(width, size.getWidth()), Math.max(height, size.getHeight()));
      renderInfo = renderInfo.offsetMouse(-this.offsetX, -this.offsetY);
    } 
    super.layout((ReadableDimension)dimension, renderInfo);
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    int width = size.getWidth();
    int height = size.getHeight();
    size = super.calcMinSize();
    Dimension dimension = new Dimension(Math.max(width, size.getWidth()), Math.max(height, size.getHeight()));
    renderInfo = renderInfo.offsetMouse(-this.offsetX, -this.offsetY);
    OffsetGuiRenderer offsetRenderer = new OffsetGuiRenderer(renderer, this.negativeOffset, (ReadableDimension)dimension, (renderInfo.layer == 0));
    offsetRenderer.startUsing();
    super.draw((GuiRenderer)offsetRenderer, (ReadableDimension)dimension, renderInfo);
    offsetRenderer.stopUsing();
  }
  
  public ReadableDimension calcMinSize() {
    return (ReadableDimension)new Dimension(0, 0);
  }
  
  public boolean scroll(ReadablePoint mousePosition, int dWheel) {
    Point mouse = new Point(mousePosition);
    if (getContainer() != null)
      getContainer().convertFor((GuiElement)this, mouse); 
    if (mouse.getX() > 0 && mouse.getY() > 0 && mouse
      .getX() < this.lastRenderSize.getWidth() && mouse.getY() < this.lastRenderSize.getHeight()) {
      dWheel = (int)Math.copySign(Math.ceil(Math.abs(dWheel) / 4.0D), dWheel);
      if (this.scrollDirection == Direction.HORIZONTAL) {
        scrollX(dWheel);
      } else {
        scrollY(dWheel);
      } 
      return true;
    } 
    return false;
  }
  
  public int getOffsetX() {
    return this.offsetX;
  }
  
  public T setOffsetX(int offsetX) {
    this.offsetX = offsetX;
    return (T)getThis();
  }
  
  public int getOffsetY() {
    return this.offsetY;
  }
  
  public T setOffsetY(int offsetY) {
    this.offsetY = offsetY;
    return (T)getThis();
  }
  
  public Direction getScrollDirection() {
    return this.scrollDirection;
  }
  
  public T setScrollDirection(Direction scrollDirection) {
    this.scrollDirection = scrollDirection;
    return (T)getThis();
  }
  
  public T scrollX(int dPixel) {
    this.offsetX = Math.max(0, Math.min(super.calcMinSize().getWidth() - this.lastRenderSize.getWidth(), this.offsetX - dPixel));
    return (T)getThis();
  }
  
  public T scrollY(int dPixel) {
    this.offsetY = Math.max(0, Math.min(super.calcMinSize().getHeight() - this.lastRenderSize.getHeight(), this.offsetY - dPixel));
    return (T)getThis();
  }
  
  public AbstractGuiScrollable() {}
  
  public enum Direction {
    VERTICAL, HORIZONTAL;
  }
}
