package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public abstract class AbstractGuiClickable<T extends AbstractGuiClickable<T>> extends AbstractGuiElement<T> implements Clickable, IGuiClickable<T> {
  private Runnable onClick;
  
  public AbstractGuiClickable() {}
  
  public AbstractGuiClickable(GuiContainer container) {
    super(container);
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    Point pos = new Point(position);
    if (getContainer() != null)
      getContainer().convertFor(this, pos); 
    if (isMouseHovering((ReadablePoint)pos) && isEnabled()) {
      onClick();
      return true;
    } 
    return false;
  }
  
  protected boolean isMouseHovering(ReadablePoint pos) {
    return (pos.getX() > 0 && pos.getY() > 0 && pos
      .getX() < getLastSize().getWidth() && pos.getY() < getLastSize().getHeight());
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    super.draw(renderer, size, renderInfo);
  }
  
  protected void onClick() {
    if (this.onClick != null)
      this.onClick.run(); 
  }
  
  public T onClick(Runnable onClick) {
    this.onClick = onClick;
    return getThis();
  }
}
