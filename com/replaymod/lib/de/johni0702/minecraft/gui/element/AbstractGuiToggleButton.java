package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

public abstract class AbstractGuiToggleButton<V, T extends AbstractGuiToggleButton<V, T>> extends AbstractGuiButton<T> implements IGuiToggleButton<V, T> {
  private int selected;
  
  private V[] values;
  
  public int getSelected() {
    return this.selected;
  }
  
  public V[] getValues() {
    return this.values;
  }
  
  public AbstractGuiToggleButton() {}
  
  public AbstractGuiToggleButton(GuiContainer container) {
    super(container);
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    String orgLabel = getLabel();
    setLabel(orgLabel + ": " + this.values[this.selected]);
    super.draw(renderer, size, renderInfo);
    setLabel(orgLabel);
  }
  
  public void onClick() {
    this.selected = (this.selected + 1) % this.values.length;
    super.onClick();
  }
  
  public T setValues(V... values) {
    this.values = values;
    return getThis();
  }
  
  public T setSelected(int selected) {
    this.selected = selected;
    return getThis();
  }
  
  public V getSelectedValue() {
    return this.values[this.selected];
  }
}
