package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiDropdownMenu<V> extends AbstractGuiDropdownMenu<V, GuiDropdownMenu<V>> {
  public GuiDropdownMenu() {}
  
  public GuiDropdownMenu(GuiContainer container) {
    super(container);
  }
  
  protected GuiDropdownMenu<V> getThis() {
    return this;
  }
}
