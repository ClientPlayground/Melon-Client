package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiScrollable extends AbstractGuiScrollable<GuiScrollable> {
  public GuiScrollable() {}
  
  public GuiScrollable(GuiContainer container) {
    super(container);
  }
  
  protected GuiScrollable getThis() {
    return this;
  }
}
