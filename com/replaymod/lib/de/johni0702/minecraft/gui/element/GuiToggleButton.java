package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;

public class GuiToggleButton<V> extends AbstractGuiToggleButton<V, GuiToggleButton<V>> {
  public GuiToggleButton() {}
  
  public GuiToggleButton(GuiContainer container) {
    super(container);
  }
  
  protected GuiToggleButton<V> getThis() {
    return this;
  }
}
