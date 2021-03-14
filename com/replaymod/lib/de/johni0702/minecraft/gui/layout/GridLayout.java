package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.org.apache.commons.lang3.tuple.Pair;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GridLayout implements Layout {
  private static final Data DEFAULT_DATA = new Data();
  
  private int columns;
  
  private int spacingX;
  
  private int spacingY;
  
  public int getColumns() {
    return this.columns;
  }
  
  public GridLayout setColumns(int columns) {
    this.columns = columns;
    return this;
  }
  
  public int getSpacingX() {
    return this.spacingX;
  }
  
  public int getSpacingY() {
    return this.spacingY;
  }
  
  public GridLayout setSpacingX(int spacingX) {
    this.spacingX = spacingX;
    return this;
  }
  
  public GridLayout setSpacingY(int spacingY) {
    this.spacingY = spacingY;
    return this;
  }
  
  private boolean cellsEqualSize = true;
  
  public boolean isCellsEqualSize() {
    return this.cellsEqualSize;
  }
  
  public GridLayout setCellsEqualSize(boolean cellsEqualSize) {
    this.cellsEqualSize = cellsEqualSize;
    return this;
  }
  
  public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer<?> container, ReadableDimension size) {
    Preconditions.checkState((this.columns != 0), "Columns may not be 0.");
    int elements = container.getElements().size();
    int rows = (elements - 1 + this.columns) / this.columns;
    if (rows < 1)
      return Collections.emptyMap(); 
    int cellWidth = (size.getWidth() + this.spacingX) / this.columns - this.spacingX;
    int cellHeight = (size.getHeight() + this.spacingY) / rows - this.spacingY;
    Pair<int[], int[]> maxCellSize = null;
    if (!this.cellsEqualSize)
      maxCellSize = calcNeededCellSize(container); 
    Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> map = new LinkedHashMap<>();
    Iterator<Map.Entry<GuiElement, LayoutData>> iter = container.getElements().entrySet().iterator();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < this.columns; j++) {
        if (!iter.hasNext())
          return map; 
        int x = j * (cellWidth + this.spacingX);
        int y = i * (cellHeight + this.spacingY);
        if (maxCellSize != null) {
          cellWidth = ((int[])maxCellSize.getLeft())[j];
          cellHeight = ((int[])maxCellSize.getRight())[i];
          x = 0;
          for (int x1 = 0; x1 < j; x1++) {
            x += ((int[])maxCellSize.getLeft())[x1];
            x += this.spacingX;
          } 
          y = 0;
          for (int y1 = 0; y1 < i; y1++) {
            y += ((int[])maxCellSize.getRight())[y1];
            y += this.spacingY;
          } 
        } 
        Map.Entry<GuiElement, LayoutData> entry = iter.next();
        GuiElement element = entry.getKey();
        Data data = (entry.getValue() instanceof Data) ? (Data)entry.getValue() : DEFAULT_DATA;
        Dimension elementSize = new Dimension(element.getMinSize());
        ReadableDimension elementMaxSize = element.getMaxSize();
        elementSize.setWidth(Math.min(cellWidth, elementMaxSize.getWidth()));
        elementSize.setHeight(Math.min(cellHeight, elementMaxSize.getHeight()));
        int remainingWidth = cellWidth - elementSize.getWidth();
        int remainingHeight = cellHeight - elementSize.getHeight();
        x += (int)(data.alignmentX * remainingWidth);
        y += (int)(data.alignmentY * remainingHeight);
        map.put(element, Pair.of(new Point(x, y), elementSize));
      } 
    } 
    return map;
  }
  
  public ReadableDimension calcMinSize(GuiContainer<?> container) {
    Preconditions.checkState((this.columns != 0), "Columns may not be 0.");
    int maxWidth = 0, maxHeight = 0;
    int elements = 0;
    for (Map.Entry<GuiElement, LayoutData> entry : (Iterable<Map.Entry<GuiElement, LayoutData>>)container.getElements().entrySet()) {
      GuiElement element = entry.getKey();
      ReadableDimension minSize = element.getMinSize();
      int width = minSize.getWidth();
      if (width > maxWidth)
        maxWidth = width; 
      int height = minSize.getHeight();
      if (height > maxHeight)
        maxHeight = height; 
      elements++;
    } 
    int rows = (elements - 1 + this.columns) / this.columns;
    int totalWidth = maxWidth * this.columns;
    int totalHeight = maxHeight * rows;
    if (!this.cellsEqualSize) {
      Pair<int[], int[]> maxCellSize = calcNeededCellSize(container);
      totalWidth = 0;
      for (int w : (int[])maxCellSize.getLeft())
        totalWidth += w; 
      totalHeight = 0;
      for (int h : (int[])maxCellSize.getRight())
        totalHeight += h; 
    } 
    if (elements > 0)
      totalWidth += this.spacingX * (this.columns - 1); 
    if (elements > this.columns)
      totalHeight += this.spacingY * (rows - 1); 
    return (ReadableDimension)new Dimension(totalWidth, totalHeight);
  }
  
  private Pair<int[], int[]> calcNeededCellSize(GuiContainer<?> container) {
    int[] columnMaxWidth = new int[this.columns];
    int[] rowMaxHeight = new int[(container.getElements().size() - 1 + this.columns) / this.columns];
    int elements = 0;
    for (Map.Entry<GuiElement, LayoutData> entry : (Iterable<Map.Entry<GuiElement, LayoutData>>)container.getElements().entrySet()) {
      int column = elements % this.columns;
      int row = elements / this.columns;
      GuiElement element = entry.getKey();
      ReadableDimension minSize = element.getMinSize();
      int width = minSize.getWidth();
      if (width > columnMaxWidth[column])
        columnMaxWidth[column] = width; 
      int height = minSize.getHeight();
      if (height > rowMaxHeight[row])
        rowMaxHeight[row] = height; 
      elements++;
    } 
    return Pair.of(columnMaxWidth, rowMaxHeight);
  }
  
  public static class Data implements LayoutData {
    private double alignmentX;
    
    private double alignmentY;
    
    public void setAlignmentX(double alignmentX) {
      this.alignmentX = alignmentX;
    }
    
    public void setAlignmentY(double alignmentY) {
      this.alignmentY = alignmentY;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof Data))
        return false; 
      Data other = (Data)o;
      return !other.canEqual(this) ? false : ((Double.compare(getAlignmentX(), other.getAlignmentX()) != 0) ? false : (!(Double.compare(getAlignmentY(), other.getAlignmentY()) != 0)));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof Data;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      long $alignmentX = Double.doubleToLongBits(getAlignmentX());
      result = result * 59 + (int)($alignmentX >>> 32L ^ $alignmentX);
      long $alignmentY = Double.doubleToLongBits(getAlignmentY());
      return result * 59 + (int)($alignmentY >>> 32L ^ $alignmentY);
    }
    
    public String toString() {
      return "GridLayout.Data(alignmentX=" + getAlignmentX() + ", alignmentY=" + getAlignmentY() + ")";
    }
    
    public Data(double alignmentX, double alignmentY) {
      this.alignmentX = alignmentX;
      this.alignmentY = alignmentY;
    }
    
    public double getAlignmentX() {
      return this.alignmentX;
    }
    
    public double getAlignmentY() {
      return this.alignmentY;
    }
    
    public Data() {
      this(0.0D, 0.0D);
    }
  }
}
