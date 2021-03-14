package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import com.replaymod.lib.org.apache.commons.lang3.tuple.Pair;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CustomLayout<T extends GuiContainer<T>> implements Layout {
  private final Layout parent;
  
  private final Map<GuiElement, Pair<Point, Dimension>> result = new LinkedHashMap<>();
  
  public CustomLayout() {
    this(null);
  }
  
  public CustomLayout(Layout parent) {
    this.parent = parent;
  }
  
  public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container, ReadableDimension size) {
    this.result.clear();
    if (this.parent == null) {
      Collection<GuiElement> elements = container.getChildren();
      for (GuiElement element : elements)
        this.result.put(element, Pair.of(new Point(0, 0), new Dimension(element.getMinSize()))); 
    } else {
      Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> elements = this.parent.layOut(container, size);
      for (Map.Entry<GuiElement, Pair<ReadablePoint, ReadableDimension>> entry : elements.entrySet()) {
        Pair<ReadablePoint, ReadableDimension> pair = entry.getValue();
        this.result.put(entry.getKey(), Pair.of(new Point((ReadablePoint)pair.getLeft()), new Dimension((ReadableDimension)pair.getRight())));
      } 
    } 
    layout((T)container, size.getWidth(), size.getHeight());
    return (Map)this.result;
  }
  
  private Pair<Point, Dimension> entry(GuiElement element) {
    return this.result.get(element);
  }
  
  protected void set(GuiElement element, int x, int y, int width, int height) {
    Pair<Point, Dimension> entry = entry(element);
    ((Point)entry.getLeft()).setLocation(x, y);
    ((Dimension)entry.getRight()).setSize(width, height);
  }
  
  protected void pos(GuiElement element, int x, int y) {
    ((Point)entry(element).getLeft()).setLocation(x, y);
  }
  
  protected void size(GuiElement element, ReadableDimension size) {
    size.getSize((WritableDimension)entry(element).getRight());
  }
  
  protected void size(GuiElement element, int width, int height) {
    ((Dimension)entry(element).getRight()).setSize(width, height);
  }
  
  protected void x(GuiElement element, int x) {
    ((Point)entry(element).getLeft()).setX(x);
  }
  
  protected void y(GuiElement element, int y) {
    ((Point)entry(element).getLeft()).setY(y);
  }
  
  protected void width(GuiElement element, int width) {
    ((Dimension)entry(element).getRight()).setWidth(width);
  }
  
  protected void height(GuiElement element, int height) {
    ((Dimension)entry(element).getRight()).setHeight(height);
  }
  
  protected int x(GuiElement element) {
    return ((Point)entry(element).getLeft()).getX();
  }
  
  protected int y(GuiElement element) {
    return ((Point)entry(element).getLeft()).getY();
  }
  
  protected int width(GuiElement element) {
    return ((Dimension)entry(element).getRight()).getWidth();
  }
  
  protected int height(GuiElement element) {
    return ((Dimension)entry(element).getRight()).getHeight();
  }
  
  protected abstract void layout(T paramT, int paramInt1, int paramInt2);
  
  public ReadableDimension calcMinSize(GuiContainer<?> container) {
    return (ReadableDimension)new Dimension(0, 0);
  }
}
