package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.org.apache.commons.lang3.tuple.Pair;
import java.util.LinkedHashMap;
import java.util.Map;

public class HorizontalLayout implements Layout {
  private static final Data DEFAULT_DATA = new Data(0.0D);
  
  private final Alignment alignment;
  
  private int spacing;
  
  public int getSpacing() {
    return this.spacing;
  }
  
  public HorizontalLayout setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }
  
  public HorizontalLayout() {
    this(Alignment.LEFT);
  }
  
  public HorizontalLayout(Alignment alignment) {
    this.alignment = alignment;
  }
  
  public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container, ReadableDimension size) {
    int x = 0;
    int spacing = 0;
    Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> map = new LinkedHashMap<>();
    for (Map.Entry<GuiElement, LayoutData> entry : (Iterable<Map.Entry<GuiElement, LayoutData>>)container.getElements().entrySet()) {
      x += spacing;
      spacing = this.spacing;
      GuiElement element = entry.getKey();
      Data data = (entry.getValue() instanceof Data) ? (Data)entry.getValue() : DEFAULT_DATA;
      Dimension elementSize = new Dimension(element.getMinSize());
      ReadableDimension elementMaxSize = element.getMaxSize();
      elementSize.setWidth(Math.min(size.getWidth() - x, Math.min(elementSize.getWidth(), elementMaxSize.getWidth())));
      elementSize.setHeight(Math.min(size.getHeight(), elementMaxSize.getHeight()));
      int remainingHeight = size.getHeight() - elementSize.getHeight();
      int y = (int)(data.alignment * remainingHeight);
      map.put(element, Pair.of(new Point(x, y), elementSize));
      x += elementSize.getWidth();
    } 
    if (this.alignment != Alignment.LEFT) {
      int remaining = size.getWidth() - x;
      if (this.alignment == Alignment.CENTER)
        remaining /= 2; 
      for (Pair<ReadablePoint, ReadableDimension> pair : map.values())
        ((Point)pair.getLeft()).translate(remaining, 0); 
    } 
    return map;
  }
  
  public ReadableDimension calcMinSize(GuiContainer<?> container) {
    int maxHeight = 0;
    int width = 0;
    int spacing = 0;
    for (Map.Entry<GuiElement, LayoutData> entry : (Iterable<Map.Entry<GuiElement, LayoutData>>)container.getElements().entrySet()) {
      width += spacing;
      spacing = this.spacing;
      GuiElement element = entry.getKey();
      ReadableDimension minSize = element.getMinSize();
      int height = minSize.getHeight();
      if (height > maxHeight)
        maxHeight = height; 
      width += minSize.getWidth();
    } 
    return (ReadableDimension)new Dimension(width, maxHeight);
  }
  
  public static class Data implements LayoutData {
    private double alignment;
    
    public void setAlignment(double alignment) {
      this.alignment = alignment;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof Data))
        return false; 
      Data other = (Data)o;
      return !other.canEqual(this) ? false : (!(Double.compare(getAlignment(), other.getAlignment()) != 0));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof Data;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      long $alignment = Double.doubleToLongBits(getAlignment());
      return result * 59 + (int)($alignment >>> 32L ^ $alignment);
    }
    
    public String toString() {
      return "HorizontalLayout.Data(alignment=" + getAlignment() + ")";
    }
    
    public Data(double alignment) {
      this.alignment = alignment;
    }
    
    public double getAlignment() {
      return this.alignment;
    }
    
    public Data() {
      this(0.0D);
    }
  }
  
  public enum Alignment {
    LEFT, RIGHT, CENTER;
  }
}
