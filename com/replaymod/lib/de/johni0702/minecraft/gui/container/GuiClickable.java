package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;

public class GuiClickable extends AbstractGuiClickableContainer<GuiClickable> {
  public GuiClickable() {}
  
  public GuiClickable(GuiContainer container) {
    super(container);
  }
  
  protected GuiClickable getThis() {
    return this;
  }
}
