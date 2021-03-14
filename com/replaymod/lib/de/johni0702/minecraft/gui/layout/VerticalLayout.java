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

public class VerticalLayout implements Layout {
  private static final Data DEFAULT_DATA = new Data(0.0D);
  
  private final Alignment alignment;
  
  private int spacing;
  
  public int getSpacing() {
    return this.spacing;
  }
  
  public VerticalLayout setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }
  
  public VerticalLayout() {
    this(Alignment.TOP);
  }
  
  public VerticalLayout(Alignment alignment) {
    this.alignment = alignment;
  }
  
  public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container, ReadableDimension size) {
    int y = 0;
    int spacing = 0;
    Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> map = new LinkedHashMap<>();
    for (Map.Entry<GuiElement, LayoutData> entry : (Iterable<Map.Entry<GuiElement, LayoutData>>)container.getElements().entrySet()) {
      y += spacing;
      spacing = this.spacing;
      GuiElement element = entry.getKey();
      Data data = (entry.getValue() instanceof Data) ? (Data)entry.getValue() : DEFAULT_DATA;
      Dimension elementSize = new Dimension(element.getMinSize());
      ReadableDimension elementMaxSize = element.getMaxSize();
      elementSize.setHeight(Math.min(size.getHeight() - y, Math.min(elementSize.getHeight(), elementMaxSize.getHeight())));
      elementSize.setWidth(Math.min(size.getWidth(), (data.maximizeWidth ? elementMaxSize : elementSize).getWidth()));
      int remainingWidth = size.getWidth() - elementSize.getWidth();
      int x = (int)(data.alignment * remainingWidth);
      map.put(element, Pair.of(new Point(x, y), elementSize));
      y += elementSize.getHeight();
    } 
    if (this.alignment != Alignment.TOP) {
      int remaining = size.getHeight() - y;
      if (this.alignment == Alignment.CENTER)
        remaining /= 2; 
      for (Pair<ReadablePoint, ReadableDimension> pair : map.values())
        ((Point)pair.getLeft()).translate(0, remaining); 
    } 
    return map;
  }
  
  public ReadableDimension calcMinSize(GuiContainer<?> container) {
    int maxWidth = 0;
    int height = 0;
    int spacing = 0;
    for (Map.Entry<GuiElement, LayoutData> entry : (Iterable<Map.Entry<GuiElement, LayoutData>>)container.getElements().entrySet()) {
      height += spacing;
      spacing = this.spacing;
      GuiElement element = entry.getKey();
      ReadableDimension minSize = element.getMinSize();
      int width = minSize.getWidth();
      if (width > maxWidth)
        maxWidth = width; 
      height += minSize.getHeight();
    } 
    return (ReadableDimension)new Dimension(maxWidth, height);
  }
  
  public static class Data implements LayoutData {
    private double alignment;
    
    private boolean maximizeWidth;
    
    public void setAlignment(double alignment) {
      this.alignment = alignment;
    }
    
    public void setMaximizeWidth(boolean maximizeWidth) {
      this.maximizeWidth = maximizeWidth;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof Data))
        return false; 
      Data other = (Data)o;
      return !other.canEqual(this) ? false : ((Double.compare(getAlignment(), other.getAlignment()) != 0) ? false : (!(isMaximizeWidth() != other.isMaximizeWidth())));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof Data;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      long $alignment = Double.doubleToLongBits(getAlignment());
      result = result * 59 + (int)($alignment >>> 32L ^ $alignment);
      return result * 59 + (isMaximizeWidth() ? 79 : 97);
    }
    
    public String toString() {
      return "VerticalLayout.Data(alignment=" + getAlignment() + ", maximizeWidth=" + isMaximizeWidth() + ")";
    }
    
    public Data(double alignment, boolean maximizeWidth) {
      this.alignment = alignment;
      this.maximizeWidth = maximizeWidth;
    }
    
    public double getAlignment() {
      return this.alignment;
    }
    
    public boolean isMaximizeWidth() {
      return this.maximizeWidth;
    }
    
    public Data() {
      this(0.0D);
    }
    
    public Data(double alignment) {
      this(alignment, true);
    }
  }
  
  public enum Alignment {
    TOP, BOTTOM, CENTER;
  }
}
